import java.io.DataInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * The Listener will listen for incoming packets and will parse them.
 * 
 * @author	Matthias Moulin & Ruben Pieters
 * @version	6/03/2013
 */
public class Listener implements Runnable {
	
    /**
     * Creates a new listener for the given socket.
     * 
     * @param	connection
     * 			The connection of which socket's input stream
     * 			must be listened to for incoming messages.
     * @throws 	IOException
     * 			There was an IO problem creating the input stream for this listener.
     */
    public Listener(Connection connection) throws IOException{
    	this.connection = connection;
    	this.in = new DataInputStream(connection.getSocket().getInputStream()); 
    }
    
    /**
     * Keeps listening for incoming messages from the socket of this Listener.
     */
    public void run() {  
        while(true) {
            try {
            	if (this.in.available() > 0) {
	                String received = this.in.readUTF();
	                System.out.println("Message received "+ received);
	                //Parse the received string into a string array
	                StringTokenizer tokenizer = new StringTokenizer(received, ProtocolDB.COMMAND_DELIMITER);
	                String[] command = new String[tokenizer.countTokens()];
	                for(int i=0; i<command.length; i++) {
	                    command[i] = tokenizer.nextToken();
	                }
	                
	                //Recognize the command to be called on the server
	                if(command[0].equals(ProtocolDB.CLIENTCONNECT_COMMAND)) {
	                	if(command.length > 1) {
	                		this.connection.getChatServer().clientConnect((ClientConnection) connection, command[1]);
	                	} else {
	                		this.connection.getChatServer().clientConnect((ClientConnection) connection, null);
	                	}
	                } else if(command[0].equals(ProtocolDB.CLIENTDISCONNECT_COMMAND)) {
	                	this.connection.getChatServer().clientDisconnect((ClientConnection) connection);
	                } else if(command[0].equals(ProtocolDB.CLIENTMESSAGE_COMMAND)) {
	                	this.connection.getChatServer().clientMessage((ClientConnection) connection, command[1]);
	                } else if(command[0].equals(ProtocolDB.SERVERCONNECT_COMMAND)) {
	                	this.connection.getChatServer().serverConnect((ServerConnection) connection);
	                } else if(command[0].equals(ProtocolDB.SERVERDISCONNECT_COMMAND)) {
	                	this.connection.getChatServer().serverDisconnect((ServerConnection) connection);
	                } else if(command[0].equals(ProtocolDB.SERVERMESSAGE_COMMAND)) {
	                	this.connection.getChatServer().serverMessage(command[1], command[2]);
	                } else if(command[0].equals(ProtocolDB.CHECK_NICKNAME_COMMAND)) {
	                	this.connection.getChatServer().checkNickname((ServerConnection) connection, command[1]);
	                } else if(command[0].equals(ProtocolDB.VOTE_COMMAND)) {
	                	this.connection.getChatServer().vote(command[1]);
	                } else if(command[0].equals(ProtocolDB.RELEASE_NICKNAME_COMMAND)) {
	                	this.connection.getChatServer().releaseNickname(command[1]);
	                } else {
	                	throw new IllegalArgumentException("protocol command is unknown " + command[0]);
	                }
            	}
                
            } catch (IOException e) {
            	System.out.println("A connection has been closed.");
                return;
            }
        }
    }
    
    /**
     * Sets the given connection to the connection of this
     * listener.
     * 
     * @param 	connection
     * 			The connection that has to be set.
     */
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * The connection.
     */
    private Connection connection;
    
    /**
     * The input stream the messages must be read from.
     */
    private DataInputStream in;
}