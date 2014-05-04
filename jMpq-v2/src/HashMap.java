
public class HashMap {

	public HashMap(byte[] arr){
		ByteReader reader = new ByteReader(arr);
		for(int i = 0; i < arr.length; i += 16){
			new Entry(reader.readLong(), reader.readShort(), reader.readShort(), reader.readInt());
		}
	}
	
	public class Entry{
		long nameHash;
		short lang;
		short platform;
		int blockIndex;
		
		public Entry(long nameHash, short lang, short platform, int blockIndex){
			this.nameHash = nameHash;
			this.lang = lang;
			//System.out.println(lang);
			this.platform = platform;
			System.out.println(platform);
			this.blockIndex = blockIndex;
		}
		
		
	}
	
	
}
