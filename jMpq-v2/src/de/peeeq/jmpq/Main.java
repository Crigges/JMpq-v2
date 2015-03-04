package de.peeeq.jmpq;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws JMpqException, IOException {
		// before 118.052 bytes
		JMpqEditor e = new JMpqEditor(new File("C:\\Users\\Crigges-Pc\\Desktop\\mpqedit\\War3.mpq"));
		e.extractFile("TerrainArt\\LordaeronSummer\\Lords_Dirt.blp", new File("test.blp"));
		e.extractFile("(listfile)", new File("listfile.txt"));
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// e.insertFile(new
		// File("C:\\Users\\Crigges\\Desktop\\WurstPack alt\\lep.txt"),
		// "lep.txt");
		// e.insertFile(new File("war3map.doo"), "war3map.doo");
		long mili = System.currentTimeMillis();
		e.close(false);
		System.out.println(System.currentTimeMillis() - mili);
		// new JMpqEditor(new File("testbuild.w3x")).extractFile("lep.txt", new
		// File("lep.txt"));
	}

}
