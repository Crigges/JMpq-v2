package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Arrays;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * 
 */
public class JMpqEditor {
	private final static BigInteger hashkey = new BigInteger("C3AF3770", 16);
	
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
	
	private HashMap hashMap;
	
	public JMpqEditor(File mpq) throws JMpqException, IOException{
		try {
			fileAsArray = Files.readAllBytes(mpq.toPath());;
		} catch (IOException e) {
			throw new JMpqException("The target file does not exists");
		}
//		ByteReader reader = new ByteReader(Arrays.copyOfRange(fileAsArray, 512, 512 + 32));
		DataInput reader = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray, 512, 32));
		

		String startString = readString(reader, 4);
		if (!startString.equals("MPQ" + ((char) 0x1A))){
			throw new JMpqException("Invaild file format or damaged mpq");
		}
		//read header
		headerSize = reader.readInt();
		archiveSize = reader.readInt();
		formatVersion = reader.readUnsignedShort();
		discBlockSize = reader.readUnsignedShort();
		hashPos = reader.readInt();
		blockPos = reader.readInt();
		hashSize = reader.readInt();
		blockSize = reader.readInt();
		
		System.out.println("this = " +this);
		
		hashMap = new HashMap(fileAsArray, hashPos, hashSize);
	}

	private String readString(DataInput reader, int size) throws IOException {
		byte[] start = new byte[size];
		reader.readFully(start);
		String startString = new String(start);
		return startString;
	}
	
	private void prepareCryptTable(){
		//TODO implement
	}

	@Override
	public String toString() {
		return "JMpqEditor [headerSize=" + headerSize
				+ ", archiveSize=" + archiveSize + ", formatVersion="
				+ formatVersion + ", discBlockSize=" + discBlockSize
				+ ", hashPos=" + hashPos + ", blockPos=" + blockPos
				+ ", hashSize=" + hashSize + ", blockSize=" + blockSize
				+ ", hashMap=" + hashMap + "]";
	}
	
	
	
}
