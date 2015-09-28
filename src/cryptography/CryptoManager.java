package cryptography;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CryptoManager {
	
	private static ExecutorService executor;
	private static AES aes;
	private static CryptoManager manager;
	
	private CryptoManager() {}
	
	/**
	 * Singleton object to manage memory more efficiently. Also keeps track of ExecutorService
	 * @param aes
	 * @return CryptoManager
	 */
	public static CryptoManager getExecutor(AES aes) {
		if(manager == null) {
			manager = new CryptoManager();
			CryptoManager.executor = Executors.newCachedThreadPool();
		}
		else {
			if(CryptoManager.executor == null)
				CryptoManager.executor = Executors.newCachedThreadPool();
		}
		CryptoManager.aes = aes;
		return manager;
	}
	
	/**
	 * MUST call this after the final line of the code. Initiates shutdown of the ExecutorService
	 */
	public static void shutdown() {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor = null;
	}
	
	/**
	 * Run a loop through all the members in this ArrayList<> and call the list.get(i).get(); 
	 * @param chunks
	 * @return ArrayList<Future<byte[]>> - List of Asynchronous encrypted chunks of a file. Use FileManager.dechunkify(...) to obtain full file
	 */
	public ArrayList<Future<byte[]>> encryptFile(final byte[][] chunks) {
		ArrayList<Future<byte[]>> list = new ArrayList<>();
		for(int i = 0; i < chunks.length; i++)
			list.add(encrypt(chunks[i]));
		return list;
	}
	
	private Future<byte[]> encrypt(final byte chunk[]) {
		Callable<byte[]> callable = new Callable<byte[]>() {
			
			@Override
			public byte[] call() throws Exception {
				return aes.encryptBytes(chunk);
			}
		};
		
		if(executor != null)
			return executor.submit(callable);
		else {
			executor = Executors.newCachedThreadPool();
			return executor.submit(callable);
		}
	}
	
	/**
	 * Run a loop through all the members in this ArrayList<> and call the list.get(i).get(); to get the partial byte arrays 
	 * @param chunks
	 * @return ArrayList<Future<byte[]>> - List of Asynchronous encrypted chunks of a file. Use FileManager.dechunkify(...) to obtain full file
	 */
	public ArrayList<Future<byte[]>> decryptFile(final byte chunks[][]) {
		ArrayList<Future<byte[]>> list = new ArrayList<>();
		for(int i = 0; i < chunks.length; i++) 
			list.add(decrypt(chunks[i]));
		return list;
	}
	
	private Future<byte[]> decrypt(final byte chunk[]) {
		Callable<byte[]> callable = new Callable<byte[]>() {
			
			@Override
			public byte[] call() throws Exception {
				return aes.decryptBytes(chunk);
			}
		};
		
		if(executor != null)
			return executor.submit(callable);
		else {
			executor = Executors.newCachedThreadPool();
			return executor.submit(callable);
		}
	}

}
