import java.io.File;
import java.io.FileNotFoundException;

public class JMpqEditor {
	private BinFileReader reader;
	
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
	
	
	public JMpqEditor(File mpq) throws JMpqException{
		try {
			reader = new BinFileReader(mpq);
		} catch (FileNotFoundException e) {
			throw new JMpqException("The target file does not exists");
		}
		//pass offset
		reader.readBytes(512);
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
	}
}
