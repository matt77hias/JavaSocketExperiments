import java.io.IOException;
import java.net.Socket;

/**
 * A ServerConnection is a connection that has been made by a server.
 */
public class ServerConnection extends Connection {

	/**
	 * Constructor of ServerConnection that will make a complete new ServerConnection.
	 * @param socket
	 * @param chatserver
	 * @throws IOException
	 */
    public ServerConnection(Socket socket, ChatServer chatserver) throws IOException {
		super(socket, chatserver);
	}
}
