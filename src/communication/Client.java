package communication;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import cryptography.*;

public class Client {

	static Socket me;
	static String serverAddr = "localhost";
	static int port;
	static String fileName,opPath = "src/testFileOutput/test1op.txt";
	static byte[] freeFile;
	static byte[][] chunkyfileByte, unchunked;
	static ArrayList<Future<byte[]>> futureByte;
	static AES aes;
	static FileManager fm;
	
	public static void main(String[] args)throws IOException, InterruptedException, ExecutionException {
		
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	//IP, port input
		//System.out.println("Enter the ip address/ hostname to connect on");
		//serverAddr = br.readLine();
	System.out.println("Enter port number");
	port = Integer.parseInt(br.readLine());
	
	//take file input here
	System.out.println("Enter filename: ");
	fileName = br.readLine();
			
	try
    {
	   //connection
       System.out.println("Connecting to " + serverAddr +
		 " on port " + port);
       me = new Socket(serverAddr, port);
       System.out.println("Just connected to " 
		 + me.getRemoteSocketAddress());
       
       //server to client keystring
       InputStream inFromServer = me.getInputStream();
       DataInputStream in =
                      new DataInputStream(inFromServer);
       String keystring = in.readUTF();
       System.out.println("Recieved key: " + keystring);
       aes = new AES(AES.stringToKey(keystring));
       
       
       //client to server filename
       OutputStream outToServer = me.getOutputStream();
       DataOutputStream out = new DataOutputStream(outToServer);
       out.writeUTF("Key recieved, filename incoming ");
       out.flush();
       System.out.println("Sending filename");
       out.writeUTF(fileName);
       
       //server file response
       if(in.readUTF().equals("found")){
    	   System.out.println("File found, proceeding with transfer");
       }
       else{
    	   System.out.println("File not found, closing connection");
    	   me.close();
       }
       
       //read byte chunks
       int fblen = in.readInt();
       int csize = in.readInt();
       for(int i = 0 ; i < fblen ; i++){
    	   in.readFully(chunkyfileByte[i],0,csize);
       }
       
       //CryptoManager
       futureByte = CryptoManager.getExecutor(aes).decryptFile(chunkyfileByte);
       
       //Decrypt arraylist
       for(int i = 0 ; i < futureByte.size() ; i++){
    	   unchunked[i] = futureByte.get(i).get();
       }
       freeFile = fm.dechunkify(unchunked);
       
       //write file
       fm.writeFile(freeFile, opPath);
       //close connection
       me.close();
    }catch(IOException e)
    {
       e.printStackTrace();
    }
	}
}
