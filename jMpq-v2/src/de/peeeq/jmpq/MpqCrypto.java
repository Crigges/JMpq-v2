package de.peeeq.jmpq;


public class MpqCrypto {

	public final static int MPQ_HASH_TABLE_INDEX =    0;
	public final static int MPQ_HASH_NAME_A =         1;
	public final static int MPQ_HASH_NAME_B  =        2;
	public final static int MPQ_HASH_FILE_KEY =       3;
	public final static int MPQ_HASH_KEY2_MIX =       4;

	
	int[] cryptTable = new int[0x500];

	
	public MpqCrypto() {
		prepareCryptTable();
	}
	
	
	void prepareCryptTable() {
		int seed = 0x00100001, index1 = 0, index2 = 0, i;

		for (index1 = 0; index1 < 0x100; index1++) {
			for (index2 = index1, i = 0; i < 5; i++, index2 += 0x100) {
				int temp1, temp2;

				seed = (seed * 125 + 3) % 0x2AAAAB;
				temp1 = (seed & 0xFFFF) << 0x10;

				seed = (seed * 125 + 3) % 0x2AAAAB;
				temp2 = (seed & 0xFFFF);

				cryptTable[index2] = (temp1 | temp2);
			}
		}
	}

	/**
	 * This is a port of 
	 * unsigned long HashString(char *lpszFileName, unsigned long dwHashType)
	 * 
	 * which is described in http://www.zezula.net/en/mpq/techinfo.html#hashes
	 * 
	 * The implementation there uses 'long' which is a 32 bit int on Windows, so here we have to use int...
	 */
	public int hash(String fileName, int hashType) {
		int seed1 = 0x7FED7FED, seed2 = 0xEEEEEEEE;

		for (int i = 0; i < fileName.length(); i++) {
			char ch = Character.toUpperCase(fileName.charAt(i));

			seed1 = (int) (cryptTable[(hashType << 8) + ch] ^ (seed1 + seed2));
			seed2 = ch + seed1 + seed2 + (seed2 << 5) + 3;
		}

		return seed1;
	}

}
