import java.net.*;
import java.io.*;

public class TCPClient {

	public static void main(String args[]) throws Exception {
		
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket = new Socket("localhost", 6789);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		String clientSentence = inFromUser.readLine();
		outToServer.writeBytes(clientSentence + "\n");
		String modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		clientSocket.close();
	}
}
