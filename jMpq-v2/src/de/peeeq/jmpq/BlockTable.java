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

import de.peeeq.jmpq.HashTable.Entry;

public class BlockTable {
	private Block[] content;
	
	public BlockTable(byte[] arr, int blockPos, int blockSize) throws IOException{
		
		MpqCrypto c = new MpqCrypto();
		
		ByteBuffer buf = ByteBuffer.wrap(arr, blockPos, 16*blockSize).order(ByteOrder.LITTLE_ENDIAN);
		
		Files.write(Arrays.copyOfRange(arr, blockPos, blockPos+16*blockSize), new File("test.data"));
		
		byte[] decrypted = c.decryptBlock(buf, 16*blockSize, MpqCrypto.MPQ_KEY_BLOCK_TABLE);
		
		Files.write(decrypted, new File("testD.data"));
		
		DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(decrypted));
		
		content = new Block[blockSize];
		
		for(int i=0; i<blockSize; i++) {
			content[i] = new Block(in);
		}
	}
	
	public Block getBlockAtPos(int pos) throws JMpqException{
		try {
			return content[pos];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JMpqException("Invaild block position");
		}
	}
	
	public class Block{
		private int filePos;
		private int compressedSize;
		private int normalSize;
		private long flags;

		public Block(DataInput in) throws IOException{
			filePos = in.readInt();
			compressedSize = in.readInt();
			normalSize = in.readInt();
			flags = in.readInt();
		}
		
		public int getFilePos() {
			return filePos;
		}

		public int getCompressedSize() {
			return compressedSize;
		}

		public int getNormalSize() {
			return normalSize;
		}

		public long getFlags() {
			return flags;
		}

		@Override
		public String toString() {
			return "Block [filePos=" + filePos + ", compressedSize="
					+ compressedSize + ", normalSize=" + normalSize
					+ ", flags=" + flags + "]";
		}
		
		
	}

}
