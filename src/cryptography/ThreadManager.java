package cryptography;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadManager {
	
	private static ExecutorService executor;
	private static AES aes;
	private static ThreadManager manager;
	
	private ThreadManager() {}
	
	public static ThreadManager getExecutor(AES aes) {
		if(manager == null) {
			manager = new ThreadManager();
			ThreadManager.executor = Executors.newCachedThreadPool();
		}
		else {
			if(ThreadManager.executor == null)
				ThreadManager.executor = Executors.newCachedThreadPool();
		}
		ThreadManager.aes = aes;
		return manager;
	}
	
	public static void releaseExecutor() {
		executor.shutdown();
		executor = null;
	}
	
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
		
		return executor.submit(callable);
	}
	
	public ArrayList<Future<byte[]>> decryptFile(final byte chunks[][]) {
		ArrayList<Future<byte[]>> list = new ArrayList<>();
		for(int i = 0; i < chunks.length; i++) 
			list.add(decrypt(chunks[i]));
		return list;
	}
	
	private Future<byte[]> decrypt(final byte chunk[]) {
		Callable<byte[]> callanble = new Callable<byte[]>() {
			
			@Override
			public byte[] call() throws Exception {
				return aes.decryptBytes(chunk);
			}
		};
		return executor.submit(callanble);
	}

}
