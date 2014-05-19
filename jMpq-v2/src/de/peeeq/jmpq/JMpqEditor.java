package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.io.LittleEndianDataInputStream;

import de.peeeq.jmpq.BlockTable.Block;

/**
 * 
 */
public class JMpqEditor {
	private byte[] fileAsArray;
	private File mpq;
	//Header
	private int headerSize;
	private int archiveSize;
	private int formatVersion;
	private int discBlockSize;
	private int hashPos;
	private int blockPos;
	private int hashSize;
	private int blockSize;
	
	private HashTable hashTable;
	private BlockTable blockTable;
	private Listfile listFile;
	private HashMap<String, MpqFile> filesByName = new HashMap<>();
	
	public JMpqEditor(File mpq) throws JMpqException, IOException{
		this.mpq = mpq;
		try {
			fileAsArray = Files.readAllBytes(mpq.toPath());
		} catch (IOException e) {
			throw new JMpqException("The target file does not exists");
		}
		DataInput reader = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray, 512, 32));
		

		String startString = readString(reader, 4);
		if (!startString.equals("MPQ" + ((char) 0x1A))){
			throw new JMpqException("Invaild file format or damaged mpq");
		}
		//read header
		headerSize = reader.readInt();
		archiveSize = reader.readInt();
		formatVersion = reader.readUnsignedShort();
		discBlockSize = 512 * (1 << reader.readUnsignedShort()); //don't remove that peq, it is important :D 
		hashPos = reader.readInt();
		blockPos = reader.readInt();
		hashSize = reader.readInt();
		blockSize = reader.readInt();
		hashTable = new HashTable(fileAsArray, hashPos + 512, hashSize);
		blockTable = new BlockTable(fileAsArray, blockPos + 512, blockSize);
		File temp = File.createTempFile("list", "file");
		Block b = blockTable.getBlockAtPos(hashTable.getBlockIndexOfFile("(listfile)"));
		MpqFile f = new MpqFile(Arrays.copyOfRange(fileAsArray, 512, fileAsArray.length), b, discBlockSize, "(listfile)");
		f.extractToFile(temp);
		listFile = new Listfile(Files.readAllBytes(temp.toPath())); 
		for(String s : listFile.getFiles()){
			filesByName.put(s, new MpqFile(Arrays.copyOfRange(fileAsArray, 512, fileAsArray.length), blockTable.getBlockAtPos(hashTable.getBlockIndexOfFile(s)), discBlockSize, s));
		}
		listFile.addFile("(listfile)");
		filesByName.put("(listfile)", f);
	}
	
	/**
	 * If file already exists it will be replaced
	 * @param source 
	 * @param archiveName only use \ as file seperator / will fail
	 * @throws JMpqException 
	 */
	public void insertFile(File source, String archiveName) throws JMpqException{
		MpqFile f = new MpqFile(source, archiveName, discBlockSize);
		listFile.addFile(archiveName);
		filesByName.put(archiveName, f);
	}
	
	/**
	 * If file already exists it will be replaced
	 * @param source 
	 * @param archiveName only use \ as file seperator / will fail
	 * @throws JMpqException 
	 */
	public void insertFile(byte[] source, String archiveName) throws JMpqException{
		MpqFile f = new MpqFile(source, archiveName, discBlockSize);
		listFile.addFile(archiveName);
		filesByName.put(archiveName, f);
	}
	
	public void deleteFile(String name) throws JMpqException{
		MpqFile f = filesByName.get(name);
		if(f != null){
			listFile.removeFile(name);
			filesByName.remove(name);
		}else{
			throw new JMpqException("Could not find file: " + name);
		}
	}
	
	public void extractFile(String name, File dest) throws JMpqException{
		try {
			MpqFile f = filesByName.get(name);
			if(f != null){
				f.extractToFile(dest);
				
			}else{
				try {
					MpqFile fil = new MpqFile(Arrays.copyOfRange(fileAsArray, 512, fileAsArray.length), blockTable.getBlockAtPos(hashTable.getBlockIndexOfFile(name)), discBlockSize, name);
					fil.extractToFile(dest);
				} catch (Exception e) {
					throw new JMpqException("Could not find file: " + name);
				}
			}
		} catch (IOException e) {
			throw new JMpqException(e);
		}
	}

	private String readString(DataInput reader, int size) throws IOException {
		byte[] start = new byte[size];
		reader.readFully(start);
		String startString = new String(start);
		return startString;
	}
	
	private void build(boolean bestCompression) throws JMpqException{
		// Write start offset -> Caluclate header -> WriteFiles and save their offsets -> Generate Blocktable -> Generate Hastable  -> Write HashTable -> Write BlockTable
		boolean rebuild = bestCompression & discBlockSize != (512 * (1 << 10));
		File temp = null;
		FileOutputStream out = null;
		try {
			temp = File.createTempFile("war", "mpq");
			out =  new FileOutputStream(temp);
		} catch (IOException e) {
			throw new JMpqException("Could not create buildfile, reason: " + e.getCause());
		}
		try {
			//Write start offset
			out.write(fileAsArray, 0, 512);
			//Calculate Header
			//Get Hash and Block Table Size
			int lines = listFile.getFiles().size() + 1;
			double helper = Math.log10(lines) / Math.log10(2);
			int a = (int) (helper + 1);
			int b = (int) (helper);
			helper = Math.pow(2, a);
			a = (int) helper;
			helper = Math.pow(2, b);
			b = (int) helper;
			int ad = Math.abs(lines - a);
			int bd = Math.abs(lines - b);
			if(ad > bd){
				lines = b * 2;
			}else{
				lines = a * 2;
			}
			//Calculate Archive Size 
			filesByName.put("(listfile)", new MpqFile(listFile.asByteArray(), "(listfile)", discBlockSize));
			archiveSize = lines * 8 * 4 + 32 + 512 + lines * 2;
			LinkedList<MpqFile> files = new LinkedList<>();
			for(String s : listFile.getFiles()){
				MpqFile f = filesByName.get(s);
				files.add(filesByName.get(s));
			}
			if(rebuild){
				LinkedList<MpqFile> tempList = files;
				files = new LinkedList<>();
				for(MpqFile f : tempList){
					// 2^10
					files.add(new MpqFile(f.asFileArray(), f.getName(), 512 * (1 << 10)));
				}
			}
			int offsetHelper = 0;
			for(MpqFile f : files){
				archiveSize += f.getCompSize();
				f.setOffset(offsetHelper + 32);
				offsetHelper+= f.getCompSize();
			}
			ByteBuffer buf = ByteBuffer.allocate(32);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			buf.put(("MPQ" + ((char) 0x1A)).getBytes());
			buf.putInt(headerSize);
			buf.putInt(archiveSize);
			buf.putShort((short) formatVersion);
			if(files.getFirst().getSectorSize() == 512 * (1 << 10)){
				buf.putShort((short) 10);
			}else{
				buf.putShort((short) 3);
			}
			buf.putInt(offsetHelper + 32);
			buf.putInt(offsetHelper + 32 + lines * 16);
			buf.putInt(lines);
			buf.putInt(lines);
			buf.position(0);
			byte[] tempHeader = new byte[32];
			buf.get(tempHeader);
			//Write header
			out.write(tempHeader);
			//Write file data
			for(MpqFile f : files){
				byte[] arr = f.getFileAsByteArray((int) (out.getChannel().position() - 512));
				out.write(arr);
			}
			//Generate BlockTable
			BlockTable bt = new BlockTable(files, lines);
			HashTable.writeNewHashTable(files, bt.ht, lines, out, hashTable);
			bt.writeToFile(out);
			for(int i = 1; i <= 1000; i++){
				out.write(0);
			}
			
			
			
			
		} catch (IOException e) {
			throw new JMpqException("Could not write buildfile, reason: " + e.getCause());
		}
		try {
			out.close();
		} catch (IOException e) {
			throw new JMpqException("Could not finalize buildfile: " + e.getCause());
		}
		try {
			mpq.delete();
			Files.copy(temp.toPath(), new FileOutputStream(mpq));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the mpq and write the changes to the file
	 * @param bestCompression provides better compression when true, but takes more time
	 * @throws JMpqException if an error while writing occurs
	 */
	public void close(boolean bestCompression) throws JMpqException{
		build(bestCompression);
	}

	@Override
	public String toString() {
		return "JMpqEditor [headerSize=" + headerSize
				+ ", archiveSize=" + archiveSize + ", formatVersion="
				+ formatVersion + ", discBlockSize=" + discBlockSize
				+ ", hashPos=" + hashPos + ", blockPos=" + blockPos
				+ ", hashSize=" + hashSize + ", blockSize=" + blockSize
				+ ", hashMap=" + hashTable + "]";
	}
	
	
	
}
