/**
 * The ChatServerMain class will start the chat server.
 */
public class ChatServerMain {
	
	/**
	 * Start a new chat server.
	 * 
	 * @param	args
	 * 			Arguments.
	 */
	public static void main(String[] args) {
		chatServer = new ChatServer();
	}
	
	/**
	 * The chat server.
	 */
	public static ChatServer chatServer;
}