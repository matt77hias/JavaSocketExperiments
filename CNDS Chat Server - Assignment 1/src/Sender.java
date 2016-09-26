import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The Sender will take care of all actions required for sending messages.
 */
public class Sender {
    /**
     * Create a new Sender that has an empty nickname-socket mapping
     */
    public Sender(Connection connection) throws IOException {
    	this.connection = connection;

	    //*************************
		this.out = new DataOutputStream(this.connection.getSocket().getOutputStream());
		//*************************
	
    }
    
    /**
     * Send a message
     * @param command The command of this message
     * @param arguments	The arguments of this message
     */
    public void send(String command, String[] arguments) {

	    //*************************
    	String message = command;
    	if(arguments != null) {
	    	for(int i=0; i<arguments.length; i++) {
	    		message = message.concat(ProtocolDB.COMMAND_DELIMITER);
	    		if(arguments[i]!=null)
	    			message = message.concat(arguments[i]);
	    		else
	    			message = message.concat("<empty>");
	    	}
			try {
				this.out.writeUTF(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
		//*************************
	
    }
    
    private Connection connection;
    private DataOutputStream out;
}