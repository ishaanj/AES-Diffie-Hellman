package communication.secure;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cryptography.AES;
import cryptography.CryptoManager;
import cryptography.FileManager;

public class SecureServer {
	
	private static ServerSocket server;
	private static Socket him;
	private static int port, timeout = 100000;
	private static String fileName, basePath = "src/testFileInput/";
	private static File file;
	private static byte[] fileByte;
	private static byte[][] chunkyFileByte;
	private static ArrayList<Future<byte[]>> futureByte;
	
	private static AES keyAES;
	private static AES aes;

	// Secret Key
	private static final byte internalKey[] = "a!`g;haetl+d$#(^".getBytes(StandardCharsets.UTF_8);
	
	private static FileManager fm;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Server: Enter the port number to listen on");
		port = Integer.parseInt(br.readLine());

		// create server and set timeout
		server = new ServerSocket(port);
		server.setSoTimeout(timeout);

		// create AES instance
		keyAES = new AES(internalKey);
		aes = new AES();
		
		// Unencrypted / Decrypted Keystring
		String keystring = aes.keyToString();
		System.out.println("Server: Raw Keystring - " + keystring + " String Size : " + keystring.length());
		
		// Keystring encrypted using internal AES key encryption
		keystring = Base64.getEncoder().encodeToString((keyAES.encryptBytes(keystring.getBytes())));
		
		System.out.println("Server: Encrypted Keystring - " + keystring + " String Size : " + keystring.length());

		while (true) {
			try {
				// connection
				System.out.println("Server: Waiting for client on port: "+ server.getLocalPort() + "...");
				Socket client = server.accept();
				System.out.println("Server: Client connected from: "+ client.getRemoteSocketAddress());

				// server to client keystring
				DataOutputStream out = new DataOutputStream(client.getOutputStream());
				out.writeUTF(keystring);
				
				// client to server filename
				DataInputStream in = new DataInputStream(client.getInputStream());
				System.out.println(in.readUTF());
				fileName = in.readUTF();

				// file from server to client
				file = new File(basePath + fileName);
				if (file.exists()) {
					System.out.println("Server: Requested File found");
					out.writeUTF("found");
				} else {
					System.out.println("Server: Requested File not found");
					out.writeUTF("!found");
					System.out.println("Server: Closing connection");
					client.close();
				}

				// FileManager
				fm = new FileManager();
				fileByte = fm.readFile(basePath + fileName);
				System.out.println("Server: Reading file and chunkifying");
				chunkyFileByte = fm.chunkify(fileByte);
				
				//System.out.println("Server : Decrypted Bytes - " + Arrays.toString(chunkyFileByte[0]));

				// CryptoManager
				futureByte = CryptoManager.getExecutor(aes).encryptFile(chunkyFileByte);
				
				out.writeInt(futureByte.size());
				out.writeInt(fm.chunkSize);
				System.out.println("Server: Sending File chunks");
				byte[] holder = null;
				for (int i = 0; i < futureByte.size(); i++) {
					holder = futureByte.get(i).get();
					if(holder != null) {
						out.writeInt(holder.length);
						out.write(holder, 0, holder.length);
					}
					else {
						out.write(0);
					}
				}

				System.out.println("Server: Finished sending file");

				// close connection
				client.close();
			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
