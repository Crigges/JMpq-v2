package de.peeeq.jmpq.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import org.junit.Test;

import de.peeeq.jmpq.JMpqEditor;
import de.peeeq.jmpq.JMpqException;

public class RealTest {

	
	@Test
	public void test() throws JMpqException, IOException {
		try (JMpqEditor editor = new JMpqEditor(new File("testmap.w3x"))) {
			String script = new String(editor.extractFile("war3map.j"));
			Assert.assertTrue(script.startsWith("//=="));
		}
	}
}
