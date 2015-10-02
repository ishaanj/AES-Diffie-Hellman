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
	static AES aes;
	static Socket me, client;
	static ServerSocket meServ;
	private static ArrayList<Future<byte[]>> futureByte;
	private static byte[] freeFile, fileByte;
	private static byte[][] chunkyFileByte;

	public static void main(String[] args) throws IOException,
			InterruptedException, ExecutionException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter port to listen on");
		portMe = Integer.parseInt(br.readLine());

		System.out.println("Enter port of authentic server");
		portHim = Integer.parseInt(br.readLine());

		meServ = new ServerSocket(portMe);
		meServ.setSoTimeout(timeout);

		FileManager fm = new FileManager();

		while (true) {
			try {

				// connect to client
				System.out.println("Listening for client on port "
						+ meServ.getLocalPort());
				client = meServ.accept();
				System.out.println("Client connected from: "
						+ client.getRemoteSocketAddress());

				// connect to server
				me = new Socket(ipAddr, portHim);

				// I/O for Server and Client
				DataOutputStream outC = new DataOutputStream(
						client.getOutputStream());
				DataInputStream inC = new DataInputStream(
						client.getInputStream());
				DataOutputStream outS = new DataOutputStream(
						me.getOutputStream());
				DataInputStream inS = new DataInputStream(me.getInputStream());

				// Read keystring from server
				String keystring = inS.readUTF();
				System.out
						.println("Read Keystring from server as " + keystring);

				// Create new Aes instance to use with client
				aes = new AES();
				String keystringClient = aes.keyToString();

				// Send Keystring to client
				outC.writeUTF(keystringClient);

				// Recieve filename and pass it on
				System.out.println(inC.readUTF());
				fileName = inC.readUTF();
				System.out.println("Filename : " + fileName);
				outS.writeUTF("Key recieved, filename incoming ");
				outS.writeUTF(fileName);

				// Check if file found or not
				String response = inS.readUTF();
				System.out.println(response);
				outC.writeUTF(response);

				// read byte chunks
				int fblen = inS.readInt();
				int csize = inS.readInt();
				for (int i = 0; i < fblen; i++) {
					inS.readFully(chunkyfileByte[i], 0, csize);
				}

				// CryptoManager
				futureByte = CryptoManager.getExecutor(aes).decryptFile(
						chunkyfileByte);

				// Decrypt arraylist
				for (int i = 0; i < futureByte.size(); i++) {
					unchunked[i] = futureByte.get(i).get();
				}

				freeFile = fm.dechunkify(unchunked);

				// write file
				fm.writeFile(freeFile, opPath);

				// Chunkify file
				fileByte = fm.readFile(basePath + fname);
				System.out.println("Reading file and chunkifying");
				chunkyFileByte = fm.chunkify(fileByte);

				// CryptoManager
				futureByte = CryptoManager.getExecutor(aes).encryptFile(
						chunkyFileByte);
				outC.writeInt(futureByte.size());
				outC.writeInt(fm.chunkSize);
				System.out.println("Sending File chunks");
				for (int i = 0; i < futureByte.size() - 1; i++) {
					outC.write(futureByte.get(i).get(), 0, fm.chunkSize);
				}

				// Close connection
				me.close();
				meServ.close();
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
