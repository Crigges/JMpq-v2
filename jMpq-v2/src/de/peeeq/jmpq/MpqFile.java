package de.peeeq.jmpq;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Array;

import com.google.common.io.LittleEndianDataInputStream;

import de.peeeq.jmpq.BlockTable.Block;

public class MpqFile {
	private Sector[] sectors;
	
	public MpqFile(byte[] fileAsArray, Block b, int sectorSize) throws IOException{
		int sectorCount = b.getCompressedSize() / sectorSize;
		
		DataInput in = new LittleEndianDataInputStream(new ByteArrayInputStream(fileAsArray));
		sectors = new Sector[sectorCount];
		for(int i = 1; i < sectorCount; i++){
			sectors[i] = new Sector(
					new LittleEndianDataInputStream(
							new ByteArrayInputStream(fileAsArray, in.readInt(), sectorSize)), sectorSize);
			
		}
		
	}
	
	public class Sector{
		int compressionType;
		byte[] content;
		
		public Sector(DataInput in, int sectorSize) throws IOException{
			compressionType = in.readInt();
			content = new byte[sectorSize];
			in.readFully(content);
		}
		
		
	}

}
