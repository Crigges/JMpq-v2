package de.peeeq.jmpq;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;

import com.google.common.io.LittleEndianDataInputStream;

import de.peeeq.jmpq.BlockTable.Block;

public class MpqFile {
	private static final int COMPRESSED = 0x00000200;
	private static final int ENCRYPTED = 0x00010000;
	private static final int SINGLEUNIT = 0x01000000;
	
	
	
	private Sector[] sectors;
	
	public MpqFile(byte[] fileAsArray, Block b, int sectorSize) throws IOException{
		int sectorCount = b.getNormalSize() / sectorSize + 2;
		
		if((b.getFlags() & ENCRYPTED) == ENCRYPTED){
			System.out.println("encrypted");
		}
		if((b.getFlags() & SINGLEUNIT) == SINGLEUNIT){
			System.out.println("single");
		}
		if((b.getFlags() & COMPRESSED) == COMPRESSED){
			DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray, b.getFilePos(), fileAsArray.length));
			sectors = new Sector[sectorCount];
			for(int i = 0; i < sectorCount ; i++){
				int bla = in.readInt();
				sectors[i] = new Sector(
						new LittleEndianDataInputStream(
								new ByteArrayInputStream(fileAsArray, bla + b.getFilePos(), sectorSize)), sectorSize);
			}	
		}
	}
	             
	public class Sector{
		byte compressionType;
		byte[] content;
		
		public Sector(DataInput in, int sectorSize) throws IOException{
			compressionType = in.readByte();
			if((compressionType & 2) == 2){
				
				//System.out.println("is zlib");
			}
			content = new byte[sectorSize - 1];
			in.readFully(content);
			System.out.println(DebugHelper.bytesToHex(content));
		}
	}
}
