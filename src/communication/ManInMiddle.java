package communication;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cryptography.AES;
import cryptography.CryptoManager;
import cryptography.FileManager;

public class ManInMiddle {

	static int portMe, timeout = 100000, portHim;
	static String ipAddr = "localhost", fileName,
			opPath = "src/testFileOutput/test1mim.txt",
			basePath = "src/testMiM/",
			fname = "test1mim.txt";
	
	static byte[][] chunkyfileByte, unchunked;
	static AES aesMIMToClient, aesServerToMIM;
	static Socket mimClient, originalClient;
	static ServerSocket mimServer;
	private static ArrayList<Future<byte[]>> futureByte;
	private static byte[] freeFile;
	
	public static void main(String[] args) throws IOException,
			InterruptedException, ExecutionException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("MitM: Enter own port to listen on");
		portMe = Integer.parseInt(br.readLine());

		System.out.println("MitM: Enter server port");
		portHim = Integer.parseInt(br.readLine());

		mimServer = new ServerSocket(portMe);
		mimServer.setSoTimeout(timeout);

		FileManager fm = new FileManager();

		while (true) {
			try {

				// connect to client
				System.out.println("MitM: Listening for client on port "
						+ mimServer.getLocalPort());
				originalClient = mimServer.accept();
				System.out.println("MitM: Client connected from: "
						+ originalClient.getRemoteSocketAddress());

				// connect to server
				mimClient = new Socket(ipAddr, portHim);

				// I/O for Server and Client
				DataOutputStream outClient = new DataOutputStream(
						originalClient.getOutputStream());
				DataInputStream inClient = new DataInputStream(
						originalClient.getInputStream());
				DataOutputStream outServer = new DataOutputStream(
						mimClient.getOutputStream());
				DataInputStream inServer = new DataInputStream(mimClient.getInputStream());

				// Read keystring from server
				String serverKeystring = inServer.readUTF();
				System.out.println("MitM: Read Keystring from server as " + serverKeystring);

				// Recreate aes keys from server keystring
				aesServerToMIM = new AES(AES.stringToKey(serverKeystring));
				
				// Create new Aes instance to use with client
				aesMIMToClient = new AES();
				String keystringClient = aesMIMToClient.keyToString();

				// Send Keystring to client
				System.out.println("MitM: Sending Client keystring as " + keystringClient);
				outClient.writeUTF(keystringClient);

				// Recieve filename and pass it on
				System.out.println(inClient.readUTF());
				fileName = inClient.readUTF();
				System.out.println("MitM: Filename : " + fileName);
				outServer.writeUTF("MitM: Key recieved, filename incoming ");
				outServer.writeUTF(fileName);

				// Check if file found or not
				String response = inServer.readUTF();
				System.out.println(response);
				outClient.writeUTF(response);

				// read byte chunks
				int fblen = inServer.readInt();
				int csize = inServer.readInt();
				chunkyfileByte = new byte[fblen][];
				unchunked = new byte[fblen][];
				
				int len = 0;
				for (int i = 0; i < fblen; i++) {
					len = inServer.readInt();
					if(len != 0) {
						chunkyfileByte[i] = new byte[len];
						inServer.read(chunkyfileByte[i], 0, len);
					}
				}

				// CryptoManager
				futureByte = CryptoManager.getExecutor(aesServerToMIM).decryptFile(chunkyfileByte);

				// Decrypt arraylist
				for (int i = 0; i < futureByte.size(); i++) {
					unchunked[i] = futureByte.get(i).get();
				}
				freeFile = fm.dechunkify(unchunked);

				// write file
				fm.writeFile(freeFile, basePath + fileName);

				//re-encrypt
				//fm = new FileManager();
				byte[] fileByte = fm.readFile(basePath + fileName);
				System.out.println("MitM: Reading file and chunkifying");
				chunkyfileByte = fm.chunkify(fileByte);
				
				// CryptoManager
				futureByte = CryptoManager.getExecutor(aesMIMToClient).encryptFile(chunkyfileByte);
				
				outClient.writeInt(futureByte.size());
				outClient.writeInt(fm.chunkSize);
				System.out.println("MitM: Sending File chunks to original client");
				byte[] holder = null;
				for (int i = 0; i < futureByte.size(); i++) {
					holder = futureByte.get(i).get();
					if(holder != null) {
						outClient.writeInt(holder.length);
						outClient.write(holder, 0, holder.length);
					}
					else {
						outClient.write(0);
					}
				}

				System.out.println("MitM: Finished sending file to original client");

				// Close connection
				mimClient.close();
				mimServer.close();
				originalClient.close();

			} catch (SocketTimeoutException s) {
				System.out.println("MitM: Socket timed out!");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

}
