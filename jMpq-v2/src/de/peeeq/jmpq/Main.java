package de.peeeq.jmpq;
import java.io.File;
import java.io.IOException;


public class Main {
	
	public static void main(String[] args) throws JMpqException, IOException{
		new JMpqEditor(new File("testmap.w3x")).extractFile("war3mapMap.blp", new File("war3map5.blp"));
	}

}
