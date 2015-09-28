package communication;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import cryptography.*;

public class Server {

	static ServerSocket me;
	static Socket him;
	static int port, timeout = 100000;
	static String fileName, basePath = "src/testFileInput/";
	static File file;
	static byte[] fileByte;
	static byte[][] chunkyFileByte;
	static ArrayList<Future<byte[]>> futureByte;
	static AES aes;
	static FileManager fm;

	public static void main(String[] args)throws IOException, InterruptedException, ExecutionException {
	
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Enter the port number to listen on");
		port = Integer.parseInt(br.readLine());
		
		//create server and set timeout
		me = new ServerSocket(port);
		me.setSoTimeout(timeout);
		
		//create AES instance
		aes = new AES();
		String keystring = aes.keyToString();
		
		while(true)
		{
	         try
	         {
	        	//connection
	            System.out.println("Waiting for client on port: " +
	            me.getLocalPort() + "...");
	            Socket him = me.accept();
	            System.out.println("Client connected from: "
	                  + him.getRemoteSocketAddress());
	            
	            //server to client keystring
	            DataOutputStream out =
	                 new DataOutputStream(him.getOutputStream());
	            out.writeUTF(keystring);
	            
	            //client to server filename
	            DataInputStream in =
	                  new DataInputStream(him.getInputStream());
	            System.out.println(in.readUTF());
	            fileName = in.readUTF();
	            
	            //file from server to client
	            file = new File(basePath+fileName);
	           	if(file.exists()){
	           		System.out.println("Requested File found");
	           		out.writeUTF("found");
	           	}
	           	else{
	           		System.out.println("Requested File not found");
	           		out.writeUTF("!found");
	           		System.out.println("Closing connection");
	           		him.close();
	           	}
	           	
	           	//FileManager 
	           	fm = new FileManager();
	           	fileByte = fm.readFile(basePath+fileName);
	           	System.out.println("Reading file and chunkifying");
	           	chunkyFileByte = fm.chunkify(fileByte);
	           	
	           	//CryptoManager
	           	futureByte = CryptoManager.getExecutor(aes).encryptFile(chunkyFileByte);
	           	out.writeInt(futureByte.size());
	           	out.writeInt(fm.chunkSize);
	           	System.out.println("Sending File chunks");
	           	for(int i = 0 ; i < futureByte.size() - 1 ; i++){
	           		out.write(futureByte.get(i).get(),0,fm.chunkSize);
	           	}
	           	
	            //close connection
	            him.close();
	         }catch(SocketTimeoutException s)
	         {
	            System.out.println("Socket timed out!");
	            break;
	         }catch(IOException e)
	         {
	            e.printStackTrace();
	            break;
	         }
	      }
		
	}
}
