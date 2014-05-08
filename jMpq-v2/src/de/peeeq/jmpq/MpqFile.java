package de.peeeq.jmpq;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;

import com.google.common.io.LittleEndianDataInputStream;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

import de.peeeq.jmpq.BlockTable.Block;

public class MpqFile {
	private static final int COMPRESSED = 0x00000200;
	private static final int ENCRYPTED = 0x00010000;
	private static final int SINGLEUNIT = 0x01000000;
	private static final int ADJUSTED_ENCRYPTED = 0x00020000;
	
	
	
	private Sector[] sectors;
	private int sectorSize;
	private Block info;
	private String name;
	
	public MpqFile(byte[] fileAsArray, Block b, int sectorSize, String name) throws IOException, JMpqException{
		this.info = b;
		this.sectorSize = sectorSize;
		this.name = name;
		int sectorCount = b.getNormalSize() / sectorSize + 2;
		MpqCrypto crypto = null;
		int baseKey = 0;
		if((b.getFlags() & ENCRYPTED) == ENCRYPTED){
			crypto = new MpqCrypto();
			baseKey = crypto.hash(name, MpqCrypto.MPQ_HASH_FILE_KEY);
			if((b.getFlags() & ADJUSTED_ENCRYPTED) == ADJUSTED_ENCRYPTED){
				baseKey = ((baseKey + b.getFilePos()) ^ b.getNormalSize());
			}
		}
		if((b.getFlags() & SINGLEUNIT) == SINGLEUNIT){
			System.out.println("single");
		}
		if((b.getFlags() & COMPRESSED) == COMPRESSED){
			DataInput in = null;
			if (crypto == null){
				in = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray, b.getFilePos(), fileAsArray.length));
			}else{
				byte[] sot = new byte[sectorCount * 4];
				System.arraycopy(fileAsArray, b.getFilePos(), sot, 0, sot.length);
				sot = crypto.decryptBlock(sot, baseKey - 1);
				in = new LittleEndianDataInputStream(new ByteArrayInputStream(sot));
			}
			sectors = new Sector[sectorCount - 1];
			int start = in.readInt();
			int end = in.readInt();
			int finalSize = 0;
			for(int i = 0; i < sectorCount - 1; i++){
				if(b.getNormalSize() - finalSize <= sectorSize){
					byte[] temp = new byte[end - start];
					System.arraycopy(fileAsArray, b.getFilePos() + start, temp, 0, temp.length);
					sectors[i] = new Sector(temp, end - start, b.getNormalSize() - finalSize, crypto, baseKey);
				}else{
					byte[] temp = new byte[end - start];
					System.arraycopy(fileAsArray, b.getFilePos() + start, temp, 0, temp.length);
					sectors[i] = new Sector(temp, end - start, sectorSize, crypto, baseKey);
				}
				finalSize += sectorSize;
				start = end;
				try{
					end = in.readInt();
				}catch(IOException e){
					break;
				}
			}
		}else{
			throw new JMpqException("Uncompressed File");
		}
	}
	
	public void extractToFile(File f) throws IOException{
		byte[] fullFile = new byte[info.getNormalSize()];
		int i = 0;
		for(Sector s : sectors){
			System.arraycopy(s.content, 0, fullFile, i, s.content.length);
			i += sectorSize;
		}
		FileOutputStream out = new FileOutputStream(f);
		out.write(fullFile);
		out.close();
	}
	             
	public class Sector{
		boolean isCompressed;
		byte compressionType;
		byte[] content;
		
		public Sector(byte[] in, int sectorSize, int uncomSectorSize, MpqCrypto crypto, int key) throws IOException, JMpqException{
			if(crypto != null){
				in = crypto.decryptBlock(in, key);
			}
			if(sectorSize == uncomSectorSize){
				content = new byte[uncomSectorSize];
				content = in;
			}else{
				isCompressed = true;
				compressionType = in[0];
				if(!((compressionType & 2) == 2)){
					throw new JMpqException("Unsupported compression algorithm: " + compressionType);
				}
				content = new byte[sectorSize];
				System.arraycopy(in, 1, content, 0, sectorSize - 1);
				content = JzLibHelper.inflate(content, uncomSectorSize);
			}
		}
	}
}
