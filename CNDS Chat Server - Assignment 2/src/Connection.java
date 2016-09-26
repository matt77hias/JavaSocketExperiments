import java.io.IOException;
import java.net.Socket;

/**
 * A generic Connection.
 */

public abstract class Connection {
    	
	/**
	 * Constructor for making a new connection with a new Listener and Sender
	 * @param socket
	 * @param chatserver
	 * @throws IOException
	 */
	public Connection(Socket socket, ChatServer chatserver) throws IOException {
		this.socket = socket;
		this.chatserver = chatserver;
		sender = new Sender(this);
		listener = new Listener(this);
		(new Thread(listener)).start();
	}
	
	public void send(String command, String[] arguments) {
		sender.send(command, arguments);
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public ChatServer getChatServer() {
		return chatserver;
	}
	
	protected Listener getListener() {
	    return listener;
	}
	
	protected void setListener(Listener listener) {
	    this.listener = listener;
	}
	
	protected Sender getSender() {
	    return sender;
	}
	
	protected void setSender(Sender sender) {
	    this.sender = sender;
	}
	
	private Socket socket;
	private Listener listener;
	private Sender sender;
	private ChatServer chatserver;
	
}
