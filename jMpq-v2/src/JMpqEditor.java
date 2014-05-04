import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Arrays;

public class JMpqEditor {
	private final static BigInteger hashkey = new BigInteger("C3AF3770", 16);
	
	private byte[] fileAsArray;
	//Header
	private int idOffset;
	private int headerSize;
	private int archiveSize;
	private short formatVersion;
	private short discBlockSize;
	private int hashPos;
	private int blockPos;
	private int hashSize;
	private int blockSize;
	
	private HashMap hashMap;
	
	public JMpqEditor(File mpq) throws JMpqException{
		try {
			fileAsArray = Files.readAllBytes(mpq.toPath());;
		} catch (IOException e) {
			throw new JMpqException("The target file does not exists");
		}
		ByteReader reader = new ByteReader(Arrays.copyOfRange(fileAsArray, 512, 512 + 32));
		if (!new String(reader.readBytes(3)).equals("MPQ")){
			throw new JMpqException("Invaild file format or damaged mpq");
		}
		//read header
		idOffset = (int) reader.readByte();
		headerSize = reader.readInt();
		archiveSize = reader.readInt();
		formatVersion = reader.readShort();
		discBlockSize = (short) (512 * Math.pow(2, reader.readShort()));
		hashPos = reader.readInt();
		blockPos = reader.readInt();
		hashSize = reader.readInt();
		blockSize = reader.readInt();
		
		hashMap = new HashMap(Arrays.copyOfRange(fileAsArray, hashPos, hashPos + hashSize));
	}
	
	private void prepareCryptTable(){
		//TODO implement
	}
}
