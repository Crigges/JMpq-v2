package de.peeeq.jmpq;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;

import com.google.common.io.LittleEndianDataInputStream;
import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

import de.peeeq.jmpq.BlockTable.Block;

public class MpqFile {
	private static final int COMPRESSED = 0x00000200;
	private static final int ENCRYPTED = 0x00010000;
	private static final int SINGLEUNIT = 0x01000000;
	
	
	
	private Sector[] sectors;
	
	public MpqFile(byte[] fileAsArray, Block b, int sectorSize) throws IOException, JMpqException{
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
		}	
		FileOutputStream out = new FileOutputStream(new File("testj.txt"));
		int c = 0;
		
		Inflater inf = new Inflater();
		byte[] output = new byte[sectorSize];
		inf.setInput(sectors[0].content);
		inf.setOutput(output);
		
		 while(inf.total_out<sectorSize &&
			      inf.total_in<sectors[0].content.length) {
			      inf.avail_in=inf.avail_out=1; /* force small buffers */
			      int err=inf.inflate(JZlib.Z_NO_FLUSH);
			      if(err==JZlib.Z_STREAM_END) break;
			    }
		System.out.println(new String(output));
		 
		for(Sector s : sectors){
			c += s.content.length + 1;
		}
		c += (sectorCount) * 4;
		System.out.println(c + " vs " + b.getCompressedSize());
	}
	             
	public class Sector{
		byte compressionType;
		byte[] content;
		
		public Sector(DataInput in, int sectorSize) throws IOException, JMpqException{
			compressionType = in.readByte();
			if(!((compressionType & 2) == 2)){
				throw new JMpqException("Unsupported compression algorithm");
			}
			content = new byte[sectorSize - 1];
			in.readFully(content);
			System.out.println(DebugHelper.bytesToHex(content));
		}
	}
}
