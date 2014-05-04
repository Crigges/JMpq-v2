package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.Arrays;

public class ByteReader {
	private byte[] content;
	private int pos = 0;
	
	
	public ByteReader(byte[] arr){
		content = arr;
		
		
	}
	
	public byte[] readBytes(int amount){
		byte[] bytes = Arrays.copyOfRange(content, pos, pos + amount);
		pos += amount;
		return bytes;
	}
	
	public String readString(){
		byte b = readByte();
		String s = "";
		byte[] bytes = new byte[2000];
		int i = 0;
		while(b != 0){
			bytes[i] = b;
			i++;
			b = readByte();
		}
		return new String(bytes).trim();
	}
	
	public byte readByte(){
		pos++;
		return content[pos -1];
	}
	
	public long readLong(){
		byte[] bytes = readBytes(8);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getLong();
	}
	
	public int readInt(){
		byte[] bytes = readBytes(4);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt();
	}
	
	public short readShort(){
		byte[] bytes = readBytes(2);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort();
	}
	
	public float readFloat(){
		byte[] bytes = readBytes(4);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getFloat();	
	}
	
	public String readFourchar(){
		return new String(readBytes(4));
	}
}
