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
	
	
	
	private Sector[] sectors;
	private int sectorSize;
	private Block info;
	
	public MpqFile(byte[] fileAsArray, Block b, int sectorSize) throws IOException, JMpqException{
		this.info = b;
		this.sectorSize = sectorSize;
		int sectorCount = b.getNormalSize() / sectorSize + 2;
		
		if((b.getFlags() & ENCRYPTED) == ENCRYPTED){
			System.out.println("encrypted");
		}
		if((b.getFlags() & SINGLEUNIT) == SINGLEUNIT){
			System.out.println("single");
		}
		if((b.getFlags() & COMPRESSED) == COMPRESSED){
			DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray, b.getFilePos(), fileAsArray.length));
			sectors = new Sector[sectorCount - 1];
			int start = in.readInt();
			int startOffset = start;
			int end = in.readInt();
			int finalSize = 0;
			for(int i = 0; i < sectorCount - 1; i++){
				sectors[i] = new Sector(
						new LittleEndianDataInputStream(
								new ByteArrayInputStream(fileAsArray, b.getFilePos() + start, end)), end - start);
				start = end;
				end = in.readInt();
			}
		}else{
			throw new JMpqException("Uncompressed File");
		}
	}
	
	public void extractToFile(File f) throws IOException{
		byte[] fullFile = new byte[info.getNormalSize()];
		int i = 0;
		for(Sector s : sectors){
			if(i + sectorSize > info.getNormalSize()){
				System.arraycopy(JzLibHelper.inflate(s, sectorSize), 0, fullFile, i, info.getNormalSize() - i);
			}else{
				System.arraycopy(JzLibHelper.inflate(s, sectorSize), 0, fullFile, i, sectorSize);
			}
			i += sectorSize;
		}
		FileOutputStream out = new FileOutputStream(f);
		out.write(fullFile);
		out.close();
	}
	             
	public class Sector{
		byte compressionType;
		byte[] content;
		
		public Sector(DataInput in, int sectorSize) throws IOException, JMpqException{
			compressionType = in.readByte();
			if(!((compressionType & 2) == 2)){
				throw new JMpqException("Unsupported compression algorithm: " + compressionType);
			}
			content = new byte[sectorSize - 1];
			in.readFully(content);
			//System.out.println(DebugHelper.bytesToHex(content));
		}
	}
}
