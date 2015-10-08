package communication.secure;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cryptography.AES;
import cryptography.CryptoManager;
import cryptography.FileManager;

public class SecureClient {
	private static Socket client;
	private static String serverAddr = "localhost";
	private static int port;
	private static String fileName, opPath = "src/testFileOutput/";
	private static byte[] freeFile;
	private static byte[][] chunkyfileByte, unchunked;
	private static ArrayList<Future<byte[]>> futureByte;
	
	private static AES keyAES;
	private static AES aes;

	// Secret Key
	private static final byte internalKey[] = "a!`g;haetl+d$#(^".getBytes(StandardCharsets.UTF_8);
	
	private static FileManager fm;

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		fm = new FileManager();

		// IP, port input
		// System.out.println("Enter the ip address/ hostname to connect on");
		// serverAddr = br.readLine();
		System.out.println("Client: Enter port number");
		port = Integer.parseInt(br.readLine());

		// take file input here
		System.out.println("Client: Enter filename: ");
		fileName = br.readLine();
		
		System.out.print("Client: Give file name : ");
		opPath = opPath + br.readLine();

		try {
			// connection
			System.out.println("Client: Connecting to " + serverAddr + " on port " + port);
			client = new Socket(serverAddr, port);
			System.out.println("Client: Just connected to "	+ client.getRemoteSocketAddress());

			// server to client keystring
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			
			// Encrypted keystring from SecureServer
			String keystring = in.readUTF();
			System.out.println("Client: Encrypted keystring - " + keystring + " Key Length : " + keystring.length());
			keyAES = new AES(internalKey);
			
			// Decrypted Keystring
			keystring = new String(Base64.getDecoder().decode(keystring));
			keystring = new String(keyAES.decryptBytes(keystring.getBytes()));
			
			System.out.println("Client: Decrypted keystring - " + keystring + " Key Length : " + keystring.length());
			
			//System.out.println("Client: Decrypted Key: " + Base64.getDecoder().de);
			aes = new AES(AES.stringToKey(keystring));

			// client to server filename
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			out.writeUTF("Client: Key recieved, filename incoming ");
			out.flush();
			System.out.println("Client: Sending filename");
			out.writeUTF(fileName);

			// server file response
			if (in.readUTF().equals("found")) {
				System.out.println("Client: File found, proceeding with transfer");
			} else {
				System.out.println("Client: File not found, closing connection");
				client.close();
			}

			// read byte chunks
			int fblen = in.readInt();
			int csize = in.readInt();
			
			chunkyfileByte = new byte[fblen][];
			unchunked = new byte[fblen][];
			
			int len = 0;
			for (int i = 0; i < fblen; i++) {
				len = in.readInt();
				if(len != 0) {
					chunkyfileByte[i] = new byte[len];
					in.read(chunkyfileByte[i], 0, len);
				}
			}

			// CryptoManager
			futureByte = CryptoManager.getExecutor(aes).decryptFile(chunkyfileByte);

			// Decrypt arraylist
			for (int i = 0; i < futureByte.size(); i++) {
				unchunked[i] = futureByte.get(i).get();
			}
			freeFile = fm.dechunkify(unchunked);

			// write file
			fm.writeFile(freeFile, opPath);
			// close connection
			client.close();
			System.out.println("Client: Finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			CryptoManager.shutdown();
		}
	}
}
