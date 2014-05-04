package de.peeeq.jmpq.tests;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.peeeq.jmpq.MpqCrypto;

public class MpqCryptoTests {

	@Test
	public void testHash() {
		MpqCrypto h = new MpqCrypto();
		assertEquals(h.hash("arr\\units.dat", MpqCrypto.MPQ_HASH_TABLE_INDEX), 0xF4E6C69D);
		assertEquals(h.hash("unit\\neutral\\acritter.grp", MpqCrypto.MPQ_HASH_TABLE_INDEX), 0xA26067F3);
	}
	
	
	@Test
	public void testEncrypt() {
		MpqCrypto h = new MpqCrypto();
		int key = 123;
		String s = "Hello World!";
		byte[] bytes = s.getBytes();
		byte[] encrypted = h.encryptMpqBlock(ByteBuffer.wrap(bytes), bytes.length, key);
		byte[] decrypted = h.decryptBlock(encrypted, key);
		assertEquals(s, new String(decrypted));
		
		
	}

}
