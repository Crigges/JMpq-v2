package de.peeeq.jmpq;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.io.LittleEndianDataInputStream;

public class BlockTable {
	Block[] content;
	HashMap<MpqFile, Block> ht = new HashMap<>();

	public BlockTable(byte[] arr, int blockPos, int blockSize)
			throws IOException {

		MpqCrypto c = new MpqCrypto();

		ByteBuffer buf = ByteBuffer.wrap(arr, blockPos, 16 * blockSize).order(
				ByteOrder.LITTLE_ENDIAN);

		byte[] decrypted = c.decryptBlock(buf, 16 * blockSize,
				MpqCrypto.MPQ_KEY_BLOCK_TABLE);

		DataInput in = new LittleEndianDataInputStream(
				new ByteArrayInputStream(decrypted));

		content = new Block[blockSize];

		for (int i = 0; i < blockSize; i++) {
			content[i] = new Block(in);
		}
	}

	public BlockTable(LinkedList<MpqFile> files, int size) {
		content = new Block[size];
		int c = 0;
		for (MpqFile f : files) {
			content[c] = new Block(f.getOffset(), f.getCompSize(),
					f.getNormalSize(), MpqFile.COMPRESSED | MpqFile.EXISTS);
			ht.put(f, content[c]);
			f.setBlockIndex(c);
			c++;
		}
		while (c < size) {
			content[c] = new Block(0, 0, 0, 0);
			c++;
		}
	}

	public void writeToFile(FileOutputStream out) throws IOException {
		byte[] temp = new byte[content.length * 16];
		int i = 0;
		for (Block b : content) {
			System.arraycopy(b.asByteArray(), 0, temp, i * 16, 16);
			i++;
		}
		MpqCrypto crypt = new MpqCrypto();
		temp = crypt.encryptMpqBlock(temp, temp.length,
				MpqCrypto.MPQ_KEY_BLOCK_TABLE);
		out.write(temp);
	}

	public Block getBlockAtPos(int pos) throws JMpqException {
		try {
			return content[pos];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new JMpqException("Invaild block position");
		}
	}

	public class Block {
		private int filePos;
		private int compressedSize;
		private int normalSize;
		private int flags;

		public Block(DataInput in) throws IOException {
			filePos = in.readInt();
			compressedSize = in.readInt();
			normalSize = in.readInt();
			flags = in.readInt();
		}

		public Block(int filePos, int compressedSize, int normalSize, int flags) {
			super();
			this.filePos = filePos;
			this.compressedSize = compressedSize;
			this.normalSize = normalSize;
			this.flags = flags;
		}

		public byte[] asByteArray() {
			byte[] temp = new byte[16];
			ByteBuffer bb = ByteBuffer.allocate(16);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putInt(filePos);
			bb.putInt(compressedSize);
			bb.putInt(normalSize);
			bb.putInt(flags);
			bb.position(0);
			bb.get(temp);
			return temp;
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
