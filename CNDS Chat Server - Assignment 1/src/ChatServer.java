import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public ChatServer() {
	    clientConnections = new Vector<ClientConnection>();

		(new Thread(new ClientListenThread(this))).start();	
	}


	/******************
	 * LISTEN THREADS *
	 ******************/
	
	private class ClientListenThread implements Runnable {
		public ClientListenThread(ChatServer chatserver) {
			this.chatServer = chatserver;	
		}
		
		public void run() {
			
			//*************************
			try {
				
				/* 
				 * A server socket waits for requests to come in over the network.
				 * It performs some operation based on that request, and then
				 * possibly returns a result to the requester.
				 */
				ServerSocket serverSocket = new ServerSocket(ServerConfig.getLocalClientPort());
				while(true) {
					// The method blocks until a connection is made.
					Socket connectionSocket = serverSocket.accept();
					new ClientConnection(connectionSocket, this.chatServer);
				}				
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			//*************************
		}
		
		private ChatServer chatServer;
	}
	

	/********************
	 * PROTOCOL METHODS *
	 ********************/

	/**
	 * Connect a client to the chat network.
	 * @param connection
	 * @param nickname
	 */
	public void clientConnect(ClientConnection clientConnection, String nickname)
			throws NullPointerException {
		if(clientConnection == null) {
			throw new NullPointerException("The given client connection may not refer the null reference.");
		}
		
	    //*************************
		if(canHaveAsNickname(nickname)) {
			clientConnection.setNickname(nickname);
			this.clientConnections.add(clientConnection);
			clientConnection.setConnected(true);
			clientConnection.send(ProtocolDB.ACCEPTED_COMMAND, new String[] {});
		} else {
			clientConnection.setConnected(false);
			clientConnection.send(ProtocolDB.REJECTED_COMMAND, new String[] {});
		}
		//*************************
	}
	
	public boolean canHaveAsNickname(String request) {
		if(request != null && !"<empty>".equals(request)) {
			for(ClientConnection cc : this.clientConnections) {
				if(cc.getNickname().equals(request)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * removes a client connection
	 * @param clientConnection
	 */
	public void clientDisconnect(ClientConnection clientConnection)
			throws NullPointerException {
		if(clientConnection == null) {
			throw new NullPointerException("The given client connection may not refer the null reference.");
		}		
		
	    //*************************
		this.clientConnections.remove(clientConnection);
		clientConnection.setConnected(false);
		//*************************
	}


	/**
	 * Broadcast the message from the given client connection to all other clients. 
	 * @param clientConnection
	 * @param messageText
	 */
	public void clientMessage(ClientConnection clientConnection, String messageText)
			throws NullPointerException {
		if(clientConnection == null) {
			throw new NullPointerException("The given client connection may not refer the null reference.");
		}
		
	    //*************************
		if(!"<empty>".equals(messageText)) {
			for(ClientConnection cc : this.clientConnections) {
				if(cc != clientConnection) {
					cc.send(ProtocolDB.SERVERMESSAGE_COMMAND, new String[] {messageText, clientConnection.getNickname()});
				}
			}
		}
		//*************************
	}

	
	/*************
	 * VARIABLES *
	 *************/

	private Vector<ClientConnection> clientConnections;	
}