import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The Sender will take care of all actions required for sending messages.
 */
public class Sender {
    /**
     * Create a new Sender that has an empty nickname-socket mapping
     */
    public Sender(Socket socket) throws IOException {
    	out = new DataOutputStream(socket.getOutputStream());
    }
    
    public void send(String command, String[] arguments) {
    	String message = command;
    	if(arguments != null) {
    		for(int i = 0; i < arguments.length; i++) {
    			message = message + ProtocolDB.COMMAND_DELIMITER + arguments[i];
    		}
    	}

    	try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private DataOutputStream out;
}