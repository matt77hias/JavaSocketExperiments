import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class Listener implements Runnable {
    /**
     * Create a new listener for the given socket
     * @param socket
     * @param server
     * @throws IOException
     * 		There was an IO problem creating the input stream for this listener
     */
    public Listener(Socket socket, ChatClient chatclient) throws IOException{
        this.socket = socket;
        this.chatclient = chatclient;
        in = new DataInputStream(getSocket().getInputStream()); 
    }
    
    /**
     * Keep listening for incoming messages from the socket of this Listener
     */
    public void run() {
        while(true) {
            try {
                String received = in.readUTF();
                //Parse the received string into a string array
                StringTokenizer tokenizer = new StringTokenizer(received, ""+ProtocolDB.COMMAND_DELIMITER);
                String[] command = new String[tokenizer.countTokens()];
                for(int i=0; i<command.length; i++) {
                    command[i] = tokenizer.nextToken();
                }
                
                
                //Recognize the command to be called on the server
                if(command[0].equals("TEST")) {
                    System.out.println("Test command recieved");
                } else if(command[0].equals(ProtocolDB.ACCEPTED_COMMAND)) {
                	chatclient.accepted();
                } else if(command[0].equals(ProtocolDB.REJECTED_COMMAND)) {
                	chatclient.rejected();
                } else if(command[0].equals(ProtocolDB.SERVERMESSAGE_COMMAND)) {
                    chatclient.serverMessage(getArguments(command)[0], getArguments(command)[1]);
                } 
            } catch (IOException e) {
                System.out.println("Connection with server is lost");
                System.exit(0);
            }
        }
    }
    
    /**
     * @return the socket of this listener
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * Remove the command so that only the arguments will remain
     * @param command
     * @return
     */
    private String[] getArguments(String[] command) {
    	String[] arguments = new String[command.length - 1];
    	for(int i = 1; i < command.length; i++) {
    		arguments[i-1] = command[i];
    	}
    	return arguments;
    }
    
    private Socket socket;
    private ChatClient chatclient;
    private DataInputStream in;
}

