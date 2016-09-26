import java.io.DataInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * The Listener will listen for incoming packets and will parse them.
 */
public class Listener implements Runnable {
    /**
     * Create a new listener for the given socket
     * @param server
     * @throws IOException
     * 		There was an IO problem creating the input stream for this listener
     */
    public Listener(Connection connection) throws IOException{
        this.connection = connection;

	    //*************************
		this.in = new DataInputStream(this.connection.getSocket().getInputStream());
		//*************************
	
    }
    
    /**
     * Keep listening for incoming messages from the socket of this Listener and parse the messages.
     */
    public void run() {  

	    //*************************
		while(true) {
			 try {
				if(in.available() > 0) {
		            String received = in.readUTF();
		            System.out.println("Received: " +  received);
		            //Parse the received string into a string array
		            StringTokenizer tokenizer = new StringTokenizer(received, ""+ProtocolDB.COMMAND_DELIMITER);
		            String[] command = new String[tokenizer.countTokens()];
		            for(int i=0; i<command.length; i++) {
		                command[i] = tokenizer.nextToken();
		            }
		            
		            if(command[0].equals("TEST")) {
		                System.out.println("Test command recieved");
		            } else if(command[0].equals(ProtocolDB.CLIENTCONNECT_COMMAND)) {
		            	if(command.length>=2)
		            		connection.getChatServer().clientConnect((ClientConnection) connection, command[1].trim());
		            	else
		            		connection.getChatServer().clientConnect((ClientConnection) connection, "<empty>");
		            } else if(command[0].equals(ProtocolDB.CLIENTDISCONNECT_COMMAND)) {
		            	connection.getChatServer().clientDisconnect((ClientConnection) connection);
		            } else if(command[0].equals(ProtocolDB.CLIENTMESSAGE_COMMAND)) {
		            	if(command.length>=2)
		            		connection.getChatServer().clientMessage((ClientConnection) connection, command[1].trim());
		            	else
		            		connection.getChatServer().clientMessage((ClientConnection) connection, "<empty>");
		            } 
				}
		    } catch (IOException e) {
			       e.printStackTrace();
			}
		}
		//*************************
	}    

    private Connection connection;
    private DataInputStream in;
}

