package de.peeeq.jmpq;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;


public class Main {
	
	public static void main(String[] args) throws JMpqException, IOException{
		long mili = System.currentTimeMillis();
		JMpqEditor e= new JMpqEditor(new File("testmap.w3x"));
		e.extractFile("war3map.doo", new File("war3map.doo"));
		e.insertFile(new File("C:\\Users\\Crigges\\Desktop\\WurstPack alt\\lep.txt"), "lep.txt");
		e.insertFile(new File("war3map.doo"), "war3map.doo");
		e.close();
		System.out.println(System.currentTimeMillis() - mili);
		//new JMpqEditor(new File("testbuild.w3x")).extractFile("lep.txt", new File("lep.txt"));
	}

}
