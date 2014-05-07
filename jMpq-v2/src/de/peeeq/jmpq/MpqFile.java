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
				if(b.getNormalSize() - finalSize <= sectorSize){
					sectors[i] = new Sector(
						new LittleEndianDataInputStream(
						new ByteArrayInputStream(fileAsArray, b.getFilePos() + start, end)), end - start, b.getNormalSize() - finalSize);
				}else{
					sectors[i] = new Sector(
						new LittleEndianDataInputStream(
						new ByteArrayInputStream(fileAsArray, b.getFilePos() + start, end)), end - start, sectorSize);
				}
				finalSize += sectorSize;
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
		
		public Sector(DataInput in, int sectorSize, int uncomSectorSize) throws IOException, JMpqException{
			if(sectorSize == uncomSectorSize){
				content = new byte[uncomSectorSize];
				in.readFully(content);
			}else{
				isCompressed = true;
				compressionType = in.readByte();
				if(!((compressionType & 2) == 2)){
					throw new JMpqException("Unsupported compression algorithm: " + compressionType);
				}
				byte[] temp = new byte[sectorSize];
				in.readFully(temp);
				content = JzLibHelper.inflate(temp, uncomSectorSize);
			}
			//System.out.println(DebugHelper.bytesToHex(content));
		}
	}
}
