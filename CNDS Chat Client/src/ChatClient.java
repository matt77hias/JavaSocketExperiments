import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient {
	
	/**
	 * @param args[0] = servername
	 * @param args[1] = port
	 * @param args[2] = nickname
	 */
    public static void main(String[] args) {
    	String server = null;
    	String port = null;
    	String nickname = null;
    	
    	if (args.length != 3) {
    		try {
    			// Server + Port are not given yet
    			System.out.print("Give the address of the server: ");
    			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    			server = br.readLine();
    			System.out.print("Give the port on that server: ");
    			port = br.readLine();
    			System.out.print("What nickname do you want? ");
    			nickname = br.readLine();
    		} catch (IOException e) {
    			e.printStackTrace();
    			System.exit(0);
    		}
    	} else {
    		server		= args[0];
    		port		= args[1];
    		nickname	= args[2];
    	}
    	
    	new ChatClient(server, Integer.parseInt(port), nickname);
    }
    
    /**
     * 
     */
    public ChatClient(String host, int port, String nickname) {
        try {
            socket = new Socket(host, port);
            sender = new Sender(socket);            
            sender.send(ProtocolDB.CLIENTCONNECT_COMMAND, new String[]{nickname});
            (new Thread(new Listener(socket, this))).start();

            
            while(true) {
    			try {
    				br = new BufferedReader(new InputStreamReader(System.in));
    				String text = br.readLine();
    				if(text.toLowerCase().equals("disconnect")) {
    					System.out.println("Disconnecting...");
    					sender.send(ProtocolDB.CLIENTDISCONNECT_COMMAND, null);
    					closeProgram();
    				} else {
    					sender.send(ProtocolDB.CLIENTMESSAGE_COMMAND, new String[]{text});
    				}
    			} catch (IOException e) {
    				e.printStackTrace();
    				System.exit(0);
    			}
            }
        } catch (UnknownHostException e) {
        	System.out.println("No host found on this ip.");
        } catch (IOException e) {
        	System.out.println("No chatserver found on this portnumber.");
        }
    }
    
    public void accepted() {
        System.out.println("Connection to server successful, your nickname is accepted");
        System.out.println("Type your chatmessages... Type DISCONNECT to quit from the server");
    }

    /**
     * 
     */
    public void rejected() {
        System.out.println("Connection to server failed");
        closeProgram();
    }

    /**
     * @param string
     */
    public void serverMessage(String message, String nickname) {
        System.out.println("User "+nickname+" says: "+message);
    }
    
    private void closeProgram() {
    	 try {
    	 	System.out.println("Closing the program");
        	System.exit(0);
        } catch(Exception e) {
        	System.out.println("There is a problem with closing the connection: " + e);
        }	
    }
    
    private Sender sender;
    private Socket socket;
    private BufferedReader br;
}
