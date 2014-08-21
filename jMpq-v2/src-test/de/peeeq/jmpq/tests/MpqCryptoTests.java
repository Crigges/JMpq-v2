package de.peeeq.jmpq.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import de.peeeq.jmpq.MpqCrypto;

public class MpqCryptoTests {

	@Test
	public void testHash() {
		MpqCrypto h = new MpqCrypto();
		assertEquals(h.hash("arr\\units.dat", MpqCrypto.MPQ_HASH_TABLE_INDEX),
				0xF4E6C69D);
		assertEquals(h.hash("unit\\neutral\\acritter.grp",
				MpqCrypto.MPQ_HASH_TABLE_INDEX), 0xA26067F3);
		assertEquals(h.hash("(hash table)", MpqCrypto.MPQ_HASH_FILE_KEY),
				0xC3AF3770);
	}

	@Test
	public void testEncrypt() {
		MpqCrypto h = new MpqCrypto();
		int key = 123;
		String s = "Hello World!";
		byte[] bytes = s.getBytes();
		byte[] encrypted = h.encryptMpqBlock(bytes, bytes.length, key);
		byte[] decrypted = h.decryptBlock(encrypted, key);
		assertEquals(s, new String(decrypted));
	}

	@Test
	public void testEncrypt2() {
		MpqCrypto h = new MpqCrypto();
		int key = 123;
		byte[] bytes = { 51, 48, -61, 4, 5, 6, 7, 9, 1, 2, 4, 2, -25, 15, 4, 3 };
		byte[] encrypted = h.encryptMpqBlock(bytes, bytes.length, key);
		byte[] decrypted = h.decryptBlock(encrypted, key);
		assertEquals(Arrays.toString(bytes), Arrays.toString(decrypted));
	}

}
