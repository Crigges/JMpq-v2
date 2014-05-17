package de.peeeq.jmpq;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;


public class Main {
	
	public static void main(String[] args) throws JMpqException, IOException{
		JMpqEditor e= new JMpqEditor(new File("testmap.w3x"));
		e.insertFile(new File("C:\\Users\\Crigges\\Desktop\\WurstPack alt\\lep.txt"), "lep.txt");
		e.close();
		new JMpqEditor(new File("testbuild.w3x")).extractFile("lep.txt", new File("lep.txt"));
	}

}
