package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * 
 */
public class JMpqEditor {
	private byte[] fileAsArray;
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
	
	public JMpqEditor(File mpq) throws JMpqException, IOException{
		try {
			fileAsArray = Files.readAllBytes(mpq.toPath());;
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
		
		System.out.println("this = " +this);
		
		hashTable = new HashTable(fileAsArray, hashPos + 512, hashSize);
		blockTable = new BlockTable(fileAsArray, blockPos + 512, blockSize);
		System.out.println(blockTable.getBlockAtPos(hashTable.getBlockIndexOfFile("war3map.j")));
		new MpqFile(Arrays.copyOfRange(fileAsArray, 512, fileAsArray.length), blockTable.getBlockAtPos(hashTable.getBlockIndexOfFile("war3map.j")), discBlockSize);
	}

	private String readString(DataInput reader, int size) throws IOException {
		byte[] start = new byte[size];
		reader.readFully(start);
		String startString = new String(start);
		return startString;
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
