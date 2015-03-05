package de.peeeq.jmpq;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws JMpqException, IOException {
		// before 118.052 bytes
		JMpqEditor e = new JMpqEditor(new File("C:\\Program Files (x86)\\Warcraft III\\war3patch.mpq"), true);
		e.extractFile("Abilities\\Spells\\Other\\Transmute\\Sparkle_Anim128.blp", new File("test.blp"));
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
