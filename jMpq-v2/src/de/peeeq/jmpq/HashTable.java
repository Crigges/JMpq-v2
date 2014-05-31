package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;

import de.peeeq.jmpq.BlockTable.Block;


public class HashTable {
	private MpqCrypto c;
	private int hashPos;
	private int hashSize;
	private Entry[] content;
	private int vaildEntrys;

	public HashTable(byte[] arr, int hashPos, int hashSize) throws IOException{
		this.hashPos = hashPos;
		this.hashSize = hashSize;
		content = new Entry[hashSize];
		c = new MpqCrypto();
		ByteBuffer buf = ByteBuffer.wrap(arr, hashPos, 16*hashSize).order(ByteOrder.LITTLE_ENDIAN);
		byte[] decrypted = c.decryptBlock(buf, 16*hashSize, MpqCrypto.MPQ_KEY_HASH_TABLE);
		DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(decrypted)); 
		for(int i=0; i<hashSize; i++) {
			content[i] = new Entry(in);
			if(content[i].wPlatform == 0) {
				vaildEntrys++;
			}
		}
	}
	
	public static void writeNewHashTable(LinkedList<MpqFile> files, HashMap<MpqFile, Block> blockForFile, int size, FileOutputStream out, HashTable orginal) throws IOException, JMpqException{
		Entry[] content = new Entry[size];
		for(int i = 0; i < size; i++){
			content[i] = new Entry(-1, -1, -1, -1, -1);
		}
		MpqCrypto c = new MpqCrypto();
		for(MpqFile f : files){
			int index = c.hash(f.getName(), MpqCrypto.MPQ_HASH_TABLE_INDEX);
			int name1 = c.hash(f.getName(), MpqCrypto.MPQ_HASH_NAME_A);
			int name2 = c.hash(f.getName(), MpqCrypto.MPQ_HASH_NAME_B);
			int start = index & (size - 1);
			while(true){
				if(content[start].wPlatform == -1){
					content[start] = new Entry(name1, name2, 0, 0, f.getBlockIndex()); 
					break;
				}
				start++;
				start = start % size;
			}
		}
		byte[] temp = new byte[size * 4 * 4];
		int i = 0;
		for(Entry e : content){
			System.arraycopy(e.asByteArray(), 0, temp, i * 16, 16);
			i++;
		}
		temp = c.encryptMpqBlock(temp, temp.length, MpqCrypto.MPQ_KEY_HASH_TABLE);
		HashTable ht = new HashTable(temp, 0, size);
		out.write(temp);
	}
	
	public int getBlockIndexOfFile(String name) throws JMpqException{
		int index = c.hash(name, MpqCrypto.MPQ_HASH_TABLE_INDEX);
		int name1 = c.hash(name, MpqCrypto.MPQ_HASH_NAME_A);
		int name2 = c.hash(name, MpqCrypto.MPQ_HASH_NAME_B);
		int start = index & (hashSize - 1);
		for(int c = 0; c <= hashSize; c++){
			if(content[start].dwName1 == name1 && content[start].dwName2 == name2){
				return content[start].dwBlockIndex;
			}else if(content[start].wPlatform != 0){
				throw new JMpqException("File Not Found");
			}
			start %= hashSize;
			start++;
		}
		throw new JMpqException("File Not Found");
	}
	
	public static class Entry{
		private int dwName1;
		private int dwName2;
		private int lcLocale;
		private int wPlatform;
		private int dwBlockIndex;
		
		public Entry(int dwName1, int dwName2, int lcLocale, int wPlatform, int dwBlockIndex) {
			this.dwName1 = dwName1;
			this.dwName2 = dwName2;
			this.lcLocale = lcLocale;
			this.wPlatform = wPlatform;
			this.dwBlockIndex = dwBlockIndex;
		}

		public Entry(DataInput in) throws IOException {
			this.dwName1 = in.readInt();
			this.dwName2 = in.readInt();
			this.lcLocale = in.readUnsignedShort();
			this.wPlatform = in.readUnsignedShort();
			this.dwBlockIndex = in.readInt();
		}
		
		public byte[] asByteArray(){
			byte[] temp = new byte[16];
			ByteBuffer bb = ByteBuffer.allocate(16);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(dwName1);
			bb.putInt(dwName2);
			bb.putShort((short) lcLocale);
			bb.putShort((short) wPlatform);
			bb.putInt(dwBlockIndex);
			bb.position(0);
			bb.get(temp);
			return temp;
		}

		@Override
		public String toString() {
			return "Entry [dwName1=" + dwName1 + ",	dwName2=" + dwName2
					+ ",	lcLocale=" + lcLocale + ",	wPlatform=" + wPlatform
					+ ",	dwBlockIndex=" + dwBlockIndex + "]";
		}
	}
}
