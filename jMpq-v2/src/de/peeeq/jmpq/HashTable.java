package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataInputStream;


public class HashTable {
	private MpqCrypto c;
	private int hashPos;
	private int hashSize;
	private Entry[] content;

	public HashTable(byte[] arr, int hashPos, int hashSize) throws IOException{
		this.hashPos = hashPos;
		this.hashSize = hashSize;
		content = new Entry[hashSize];
		c = new MpqCrypto();
		
		ByteBuffer buf = ByteBuffer.wrap(arr, hashPos, 16*hashSize).order(ByteOrder.LITTLE_ENDIAN);
		
		Files.write(Arrays.copyOfRange(arr, hashPos, hashPos+16*hashSize), new File("test.data"));
		
		byte[] decrypted = c.decryptBlock(buf, 16*hashSize, MpqCrypto.MPQ_KEY_HASH_TABLE);
		
		
		Files.write(decrypted, new File("testD.data"));
		
		DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(decrypted));
		
		for(int i=0; i<hashSize; i++) {
			content[i] = new Entry(in);
		}
	}
	
	public int getBlockIndexOfFile(String name) throws JMpqException{
		int index = c.hash(name, MpqCrypto.MPQ_HASH_TABLE_INDEX);
		int name1 = c.hash(name, MpqCrypto.MPQ_HASH_NAME_A);
		int name2 = c.hash(name, MpqCrypto.MPQ_HASH_NAME_B);
		int start = index & (hashSize - 1);
		for(int i = start; i < hashSize; i++){
			if(content[start].dwName1 == name1 && content[start].dwName2 == name2){
				return content[start].dwBlockIndex;
			}else if(content[start].wPlatform != 0){
				throw new JMpqException("File Not Found");
			}
		}
		throw new JMpqException("File Not Found");
	}
	
	public class Entry{
		private int dwName1;
		private int dwName2;
		private int lcLocale;
		private int wPlatform;
		private int dwBlockIndex;
		
		public Entry(DataInput in) throws IOException {
			this.dwName1 = in.readInt();
			this.dwName2 = in.readInt();
			this.lcLocale = in.readUnsignedShort();
			this.wPlatform = in.readUnsignedShort();
			this.dwBlockIndex = in.readInt();
		}

		@Override
		public String toString() {
			return "Entry [dwName1=" + dwName1 + ",	dwName2=" + dwName2
					+ ",	lcLocale=" + lcLocale + ",	wPlatform=" + wPlatform
					+ ",	dwBlockIndex=" + dwBlockIndex + "]";
		}
	}
}