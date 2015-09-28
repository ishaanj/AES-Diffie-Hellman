package cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FileManager {
	public int chunkSize;

	public FileManager() {
		this(4 * 1024); // 4 KB
	} 

	public FileManager(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public byte[] readFile(String ipFileNae) {
		Path path = Paths.get(ipFileNae);
		byte fileData[] = null;
		try {
			fileData = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileData;
	}

	public boolean writeFile(byte file[], String opFileName) {
		Path path = Paths.get(opFileName);
		try {
			Files.write(path, file, StandardOpenOption.CREATE, StandardOpenOption.WRITE/*, StandardOpenOption.APPEND*/);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public byte[][] chunkify(byte fileData[]) {
		byte chunks[][] = null;
		int noOfChunks = (int) (fileData.length / chunkSize) + 1;
		chunks = new byte[noOfChunks][];
		
		System.out.println("Chunkify : filesize = " + fileData.length);

		for(int i = 0, k = 0; i < fileData.length; i += chunkSize, k++) {
			if(i + chunkSize < fileData.length) {
				chunks[k] = Arrays.copyOfRange(fileData, i, i + chunkSize);
			}
			else {
				chunks[k] = Arrays.copyOfRange(fileData, i, fileData.length);
			}
		}
		int length = 0;
		for(int i = 0; i < chunks.length; i++)
			length += chunks[i].length;
		
		System.out.println("Chunks size : " + length);
		
		return chunks;
	}

	public byte[] dechunkify(byte chunks[][]) {
		byte file[] = null;
		int length = 0;
		int bytesRead = 0;

		for(int i = 0; i < chunks.length; i++)
			length += chunks[i].length;
		
		System.out.println("Length = " + length);

		file = new byte[length];

		for(int i = 0; i < chunks.length; i++) {
			if(bytesRead + chunkSize < length) {
				System.arraycopy(chunks[i], 0, file, bytesRead, chunkSize);
				bytesRead += chunkSize;
			}
			else {
				System.arraycopy(chunks[i], 0, file, bytesRead, length - bytesRead);
				bytesRead += length - bytesRead;	
			}
		}
		return file;
	}
}
