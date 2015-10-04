package communication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cryptography.*;

public class Client {

	static Socket client;
	static String serverAddr = "localhost";
	static int port;
	static String fileName, opPath = "src/testFileOutput/";
	static byte[] freeFile;
	static byte[][] chunkyfileByte, unchunked;
	static ArrayList<Future<byte[]>> futureByte;
	static AES aes;
	static FileManager fm;
	
	private static final int clientP = 401, clientG = 3;

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
			
			
			/*StringBuilder sb = new StringBuilder();
			String keystring = "";
			int keyLength = in.readInt();
			
			for(int i = 0; i < keyLength; i++) {
				
			}*/
			String keystring = in.readUTF();
			
			
			
			System.out.println("Client: Encrypted keystring - " + keystring + " Key Length : " + keystring.length());
			
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			CryptoManager.shutdown();
		}
	}
}
