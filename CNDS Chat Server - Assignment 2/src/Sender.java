import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The Sender will take care of all actions required for sending messages.
 * 
 * @author	Matthias Moulin & Ruben Pieters
 * @version	6/03/2013
 */
public class Sender {
	
    /**
     * Creates a new Sender that has an empty nickname-socket mapping.
     * 
     * @param	connection
     * 			The connection of which socket's output stream
     * 			the messages must be written to.
     */
    public Sender(Connection connection) throws IOException {
    	this.out = new DataOutputStream(connection.getSocket().getOutputStream());
    }
    
    /**
     * Sends a message corresponding to the given arguments.
     * 
     * @param	command
     * 			The command of this message.
     * @param	arguments
     * 			The arguments of this message.
     * @throws	NullPointerException
     * 			The given command may not refer the null reference.
     * 			| command == null
     */
    public void send(String command, String[] arguments) 
    		throws NullPointerException{
    	if (command == null)
    		throw new NullPointerException("The given command may not refer the null reference.");
    	
    	StringBuilder message = new StringBuilder();
    	message.append(command);
    	if(arguments != null) {
    		for(int i = 0; i < arguments.length; i++) {
    			message.append(ProtocolDB.COMMAND_DELIMITER + arguments[i]);
    		}
    	}

    	try {
    	    System.out.println("Writing message " + message.toString());
            this.out.writeUTF(message.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * The output stream the messages must be written to.
     */
    private DataOutputStream out;
}