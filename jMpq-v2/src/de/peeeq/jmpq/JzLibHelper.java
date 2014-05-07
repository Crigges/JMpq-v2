package de.peeeq.jmpq;

import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

import de.peeeq.jmpq.MpqFile.Sector;

public class JzLibHelper {
	
	public static byte[] inflate(byte[] bytes, int uncompSize){
		byte[] uncomp = new byte[uncompSize];
		Inflater inf = new Inflater();
		inf.setInput(bytes);
		inf.setOutput(uncomp);
		 while(inf.total_out<uncompSize &&
			      inf.total_in<bytes.length) {
			      inf.avail_in=inf.avail_out=1;
			      int err=inf.inflate(JZlib.Z_NO_FLUSH);
			      if(err==JZlib.Z_STREAM_END) break;
			    }
		 return uncomp;
	}

}
