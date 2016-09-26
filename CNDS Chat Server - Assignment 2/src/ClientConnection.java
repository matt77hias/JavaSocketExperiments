import java.io.IOException;
import java.net.Socket;

/**
 * The ClientConnection is a connection that has been made by a client.
 */
public class ClientConnection extends Connection {

	/**
	 * Constructor of ClientConnection that will make a complete new ClientConnection.
	 * @param socket
	 * @param chatServer
	 * @throws IOException
	 */
	public ClientConnection(Socket socket, ChatServer chatServer) throws IOException {
		super(socket, chatServer);
		connected = false;
	}
	
	/**
	 * Add one vote for the nickname of this client
	 */
	public void addVote() {
		nbVotes++;
	}
	
	public int getVote() {
		return nbVotes;
	}
	
	public String getNickname() {
		return nickname;
	}
	
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
	
	public boolean isConnected() {
	    return connected;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	private String nickname;
	private int nbVotes;
	private boolean connected;
	
}
