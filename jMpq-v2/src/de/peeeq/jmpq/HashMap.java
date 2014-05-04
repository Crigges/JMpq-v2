package de.peeeq.jmpq;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.common.io.LittleEndianDataInputStream;


public class HashMap {

	public HashMap(byte[] arr, int hashPos, int hashSize) throws IOException{
		
		MpqCrypto c = new MpqCrypto();
		
		ByteBuffer buf = ByteBuffer.wrap(arr, hashPos, 16*hashSize);
		byte[] decrypted = c.decryptBlock(buf, 16*hashSize, MpqCrypto.MPQ_KEY_HASH_TABLE);
		
		System.out.println("offset = " + hashPos);
		System.out.println("size = " + hashSize);
		DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(decrypted));
		
		for(int i=0; i<hashSize; i++) {
			Entry e = new Entry(in);
			System.out.println(e);
		}
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