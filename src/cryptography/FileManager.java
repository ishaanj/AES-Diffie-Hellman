package cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
	public long chunkSize;
	
	public FileManager() {
		this(4 * 1024); // 4 KB
	} 
	
	public FileManager(long chunkSize) {
		this.chunkSize = chunkSize;
	}

	public byte[] encryptedFile(String ipFileNae, AES encoder) {
		Path path = Paths.get(ipFileNae);
		byte fileData[] = null;
		try {
			fileData = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return encoder.encryptBytes(fileData);
	}
	
	public boolean decryptFile(byte file[], String opFileName, AES decoder) {
		Path path = Paths.get(opFileName);
		byte[] fileData = decoder.decryptBytes(file);
		try {
			Files.write(path, fileData);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
