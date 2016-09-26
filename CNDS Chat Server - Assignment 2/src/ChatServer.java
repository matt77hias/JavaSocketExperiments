import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A class of chat servers. This class implements the main
 * logic of the chat server application.
 * 
 * @author	Matthias Moulin & Ruben Pieters
 * @version	6/03/2013
 *
 */
public class ChatServer {

	/**
	 * Initializes a new chat server.
	 */
	public ChatServer() {
		(new Thread(new ServerListenThread(this))).start();
		(new Thread(new ClientListenThread(this))).start();
		(new Thread(new CommandLineListener())).start();	
	}

	
	/**
	 * A class of listeners for clients for
	 * a given chat server.
	 * 
	 * @author	Matthias Moulin & Ruben Pieters
	 * @version	6/03/2013
	 *
	 */
	private class ClientListenThread implements Runnable {
		
		/**
		 * Initializes a new listener for clients for the
		 * given chat server.
		 * 
		 * @param 	chatserver
		 * 			The chat server.
		 */
		public ClientListenThread(ChatServer chatserver) {
			this.chatServer = chatserver;	
		}

		/**
		 * Starts listening for clients.
		 */
		public void run() {
			try {
				@SuppressWarnings("resource")
				ServerSocket listenSocket = new ServerSocket(ServerConfig.getLocalClientPort());
				System.out.println("Listening for connecting clients on port " + ServerConfig.getLocalClientPort());

				// Start listening for incoming connections from clients.
				while (true) {
					Socket remoteClientSocket = listenSocket.accept();
					// Create a new ClientConnection for the incoming connection.
					new ClientConnection(remoteClientSocket, chatServer); 
				}
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * The chat server.
		 */
		private ChatServer chatServer;
	}

	/**
	 * A class of listeners for servers for
	 * a given chat server.
	 * 
	 * @author	Matthias Moulin & Ruben Pieters
	 * @version	6/03/2013
	 *
	 */
	private class ServerListenThread implements Runnable {
		
		/**
		 * Initializes a new listener for servers for the
		 * given chat server.
		 * 
		 * @param 	chatserver
		 * 			The chat server.
		 */
		public ServerListenThread(ChatServer chatserver) {
			this.chatServer = chatserver;	
		}

		/**
		 * Starts listening for other servers and try to actively make
		 * a connection to the other servers.
		 */
		public void run() {
			try {
				int serverPort = ServerConfig.getLocalServerPort();
				@SuppressWarnings("resource")
				ServerSocket listenSocket = new ServerSocket(serverPort);
				System.out.println("Listening for connecting servers on port " + ServerConfig.getLocalServerPort());
				
				// Open a connection to other servers.
				for (int i=0; i < ServerConfig.getNbOtherServers(); i++) {
					try {
						int port = ServerConfig.getRemoteServerPort(i);
						String host = ServerConfig.getRemoteServer(i);
						if (!(port == serverPort && host.equals("localhost"))) {
							Socket otherServerConnSocket = new Socket(host, port);
							ServerConnection otherServerConnection = new ServerConnection(otherServerConnSocket, this.chatServer); 
							otherServerConnection.send(ProtocolDB.SERVERCONNECT_COMMAND, null);
							System.out.println("Connecting with server on port " + port);
						}
					}
					catch (ConnectException e) {
					}
				}
				
				// Start listening for incoming connections from servers.
				while (true) {
					Socket remoteServerSocket = listenSocket.accept();
					// Create a new ServerConnection for the incoming connection
					ServerConnection otherServerConnection = new ServerConnection(remoteServerSocket, this.chatServer); 
					otherServerConnection.send(ProtocolDB.SERVERCONNECT_COMMAND, null);
					System.out.println("Connection established on port " + remoteServerSocket.getPort());
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * The chat server.
		 */
		private ChatServer chatServer;
	}

	/**
	 * Listens for commandline commands.
	 */
	private class CommandLineListener implements Runnable{
		
		/**
		 * Starts listening for commandline commands.
		 */
		public void run() {
			boolean running = true;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			while(running) {
				System.out.println("Type \"shutdown\" to stop the server.");
				try{
					String command = br.readLine();

					if(command.equals("shutdown")){
						running = false;
						stopServer();	
					}
				}
				catch(IOException e){
					System.err.println("There was an IOException");
				}
			}
		}
	}
    
    
    /**
     * Stops this chat server.
     */
    public void stopServer() {
    	System.out.println("The server is stopping");
    	
    	for(int i=0; i < this.clientConnections.size(); i++) {
    		sendToServers(ProtocolDB.RELEASE_NICKNAME_COMMAND, new String[] {((ClientConnection) clientConnections.get(i)).getNickname()});
    	}
    	sendToServers(ProtocolDB.SERVERDISCONNECT_COMMAND, null);   	
    	System.exit(0);
    }

    /**
     * Connects a client to the chat network.
     * 
     * If the given nickname refers the null reference, meaning that
     * the client requested a zero character nickname, the client
     * is rejected.
     * If the given nickname the client proposes has already been used
     * for a client locally connected to this server or
     * voted for, the client is rejected.
     * Otherwise, a vote round is started for the given nickname.
     * 
     * @param 	clientConnection
     * 			The connection of the client that has to be
     * 			connected.
     * @param 	nickname
     * 			The nickname the client of the given client
     * 			connection requested.
     */
	public void clientConnect(final ClientConnection clientConnection, final String nickname) {
		// If the nickname refers the null reference, meaning that the client requested a zero
		// character nickname, reject the client.
		if (nickname == null) {
			System.out.println("Client who requested a zero character nickname " +
					"failed to connect to the chat network.");
			clientConnection.send(ProtocolDB.REJECTED_COMMAND, null);
		}
		// If the nickname the client proposes has already been used or voted for, reject the client.
		if (getVotedNicknames().contains(nickname)) {
			System.out.println("The client with requested nickname "
							+ nickname
							+ " failed to connect to the chat network. Nickname in use.");
			clientConnection.send(ProtocolDB.REJECTED_COMMAND, null);
		
		// otherwise, start a vote round for the nickname.
		} else {
			// Add the given nickname to the collection of voted nicknames.
			addVotedNickname(nickname);
			// Add the given nickname to the collection of nicknames
			// for which a voting is in progress.
			addNewVoting(nickname);
			sendToServers(ProtocolDB.CHECK_NICKNAME_COMMAND, new String[] { nickname });
			
			(new Thread(new Runnable(){
				
				/**
				 * Number of iterations.
				 */
				private int timeOut = 30;

				/**
				 * A vote round is started for the given nickname
				 * in a new thread. This thread stops after three seconds
				 * or earlier when the nickname is accepted.
				 */
				@Override
				public void run() {
					// Flag set when the client is allowed to connect under the given nickname
					boolean accepted = false;
					// Stops when the 3 secs are past or when the nickname is accepted.
					while (this.timeOut > 0 && !accepted) {
						try {
							// 100ms sleep
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						this.timeOut--;
						if (isAccepted(nickname)) {
							accepted = true;
						}
					}
					// The voting in progress for this nickname is removed.
					removeVoting(nickname);
					if (accepted) {
						clientConnection.setConnected(true);
						clientConnection.setNickname(nickname);
						addClientConnection(clientConnection);
						clientConnection.send(ProtocolDB.ACCEPTED_COMMAND, null);
					} else {
						clientConnection.send(ProtocolDB.REJECTED_COMMAND, null);
						// Vote for this client fails.
						removeVotedNickname(nickname);
						sendToServers(ProtocolDB.RELEASE_NICKNAME_COMMAND, new String[] { nickname });
					}
				}
			})).start();
		}
	}
	
	/**
	 * Connects a server to the chat network.
	 * 
	 * @param 	connection
	 * 			The connection of the server that has to be connected.
	 */
	public void serverConnect(ServerConnection serverConnection)
		throws IllegalArgumentException {
		if (serverConnection == null)
			throw new IllegalArgumentException("server connection is null");
		
	    addServerConnection(serverConnection);
	}

	/**
	 * Removes the given client connection from
	 * the collection of client connections of this chat server.
	 * 
	 * @param 	clientConnection
	 * 			The client connection that has to be removed.
	 */
	public void clientDisconnect(ClientConnection clientConnection) {		
	    String nickname = clientConnection.getNickname();
	    System.out.println("Disconnecting client: " + nickname);
	    // Tell the other servers that the nickname has been released.
		sendToServers(ProtocolDB.RELEASE_NICKNAME_COMMAND, new String[] { nickname });
		
		// Remove the client connection and forget the nickname.
		removeClientConnection(clientConnection);
		clientConnection.setConnected(false);
	    getVotedNicknames().remove(nickname);
	}

	/**
	 * Removes the given server connection from
	 * the collection of server connections of this chat server.
	 * 
	 * @param	serverConnection
	 * 			The server connection that has to be removed.
	 * @throws	IllegalArgumentException
	 * 			The given server connection may not refer
	 * 			the null reference.
	 * 			| serverConnection == null
	 */
	public void serverDisconnect(ServerConnection serverConnection)
			throws IllegalArgumentException {
		if (serverConnection == null)
			throw new IllegalArgumentException("The server connection may not refer the null reference.");
		
	    removeServerConnection(serverConnection);
	}

	/**
	 * Broadcasts the given message from the client corresponding the given
	 * connection to all locally connected other clients and all other servers.
	 * The nickname of the sender will be appended to the argument list.
	 * 
	 * @param	clientConnection
	 * 			The client connection of the client who sent the
	 * 			given message.
	 * @param	messageText
	 * 			The message that has to be broadcasted.
	 */
	public void clientMessage(ClientConnection clientConnection, String messageText) {
	    String nickname = clientConnection.getNickname();
	    
	    // Put arguments of SERVERMESSAGE together.
		String[] outArguments = new String[] { messageText, nickname };
		
		// Send to all your other connected clients.
		sendToClientsExcept(nickname, ProtocolDB.SERVERMESSAGE_COMMAND, outArguments);

		// Send to all other connected servers.
		sendToServers(ProtocolDB.SERVERMESSAGE_COMMAND, outArguments);
	}

	/**
	 * Broadcasts the given message to all the clients of this chat server.
	 * 
	 * @param 	messageText
	 * 			The message that has to be broadcasted.
	 * @param 	nickname
	 * 			The nickname of the sender of the given
	 * 			message.
	 */
	public void serverMessage(String messageText, String nickname) {
		// Put arguments of SERVERMESSAGE together.
		String[] outArguments = new String[] { messageText, nickname };

		// Send to all other connected servers.
		sendToClients(ProtocolDB.SERVERMESSAGE_COMMAND, outArguments);
	}

	/**
	 * Checks if the given nickname hasn't already been registered
	 * for a client connected to this chat server and vote for it if it's not.
	 * 
	 * @param 	serverConnection
	 * 			The server connection of the server where the nickname
	 * 			is requested.
	 * @param 	nickname
	 * 			The nickname that has to be checked.
	 */
	public void checkNickname(ServerConnection serverConnection, String nickname) {
		if (!getVotedNicknames().contains(nickname)) {
			serverConnection.send(ProtocolDB.VOTE_COMMAND, new String[] { nickname });
		}
		addVotedNickname(nickname);
	}
	
	/**
	 * Receives a vote for the given nickname.
	 * If a sufficient number of votes has been gathered,
	 * accept the client to the chat network.
	 * 
	 * @param 	nickname
	 * 			The nickname for which a vote is received.
	 */
	public void vote(String nickname) {
		incrementVoteForNickname(nickname);
	}

	/**
	 * Releases the given nickname from the list of taken nicknames of this server.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be released.
	 */
	public void releaseNickname(String nickname) {
		removeVotedNickname(nickname);
	}
	
	/**
	 * Sends the given command and arguments to all registered servers
	 * of this chat server.
	 * 
	 * @param 	command
	 * 			The command according to the protocol that
	 * 			has to be sent.
	 * @param 	arguments
	 * 			The arguments belonging to the given command
	 * 			according to the protocol.
	 */
	protected void sendToServers(String command, String[] arguments) {
	    for (Iterator<ServerConnection> iter = getServerConnections().iterator(); iter.hasNext();) {
			ServerConnection serverConnection = iter.next();
			serverConnection.send(command, arguments);
	    }
	}
	
	/**
	 * Sends the given command and arguments to all registered clients
	 * of this chat server.
	 * 
	 * @param 	command
	 * 			The command according to the protocol that
	 * 			has to be sent.
	 * @param 	arguments
	 * 			The arguments belonging to the given command
	 * 			according to the protocol.
	 */
	protected void sendToClients(String command, String[] arguments) {
	    for (Iterator<ClientConnection> iter = getClientConnections().iterator(); iter.hasNext();) {
			ClientConnection clientConnection = iter.next();
		    if (clientConnection.isConnected()) {
				clientConnection.send(command, arguments);
			}
		}
	}
	
	/**
	 * Sends the given command and arguments to all registered clients
	 * of this chat server except the one with the given nickname
	 * (the actual sender).
	 * 
	 * @param 	nickname
	 * 			The nickname of the writer of the message.
	 * @param 	command
	 * 			The command according to the protocol that
	 * 			has to be sent.
	 * @param 	arguments
	 * 			The arguments belonging to the given command
	 * 			according to the protocol.
	 */
	protected void sendToClientsExcept(String nickname, String command, String[] arguments) {
	    for (Iterator<ClientConnection> iter = getClientConnections().iterator(); iter.hasNext();) {
			ClientConnection clientConnection = iter.next();
		    if (!nickname.equals(clientConnection.getNickname()) && clientConnection.isConnected()) {
				clientConnection.send(command, arguments);
			}
		}
	}

	/**
	 * Collection containing the client connection of all the locally
	 * connected clients of this chat server.
	 */
	private Vector<ClientConnection> clientConnections = new Vector<ClientConnection>();
	
	/**
	 * Returns a collection containing the client connection of all the
	 * locally connected clients of this chat server.
	 */
	private Collection<ClientConnection> getClientConnections() {
		return this.clientConnections;
	}
	
	/**
	 * Adds the given client connection to the collection of client connections
	 * of this chat server.
	 * 
	 * @param 	conn
	 * 			The client connection that has to be added.
	 * @post	If the given client connection doesn't refer the null reference
	 * 			and the collection of client connections of this chat server
	 * 			doesn't contain the given client connection, the collection of
	 * 			client connections of this chat server contains the given client
	 * 			connection.
	 * 			| if (conn != null && !getClientConnections().contains(conn))
	 * 			| 	then new.getClientConnections().contains(conn)
	 */
	private void addClientConnection(ClientConnection conn) {
		if (conn != null && !getClientConnections().contains(conn)) {
			this.clientConnections.add(conn);
		}
	}
	
	/**
	 * Removes the given client connection from the collection of client
	 * connections of this chat server.
	 * 
	 * @param 	conn
	 * 			The client connection that has to be removed.
	 * @post	The given client connection is removed from the collection
	 * 			of client connections of this chat server.
	 * 			| !new.getClientConnections().contains(conn)
	 */
	private void removeClientConnection(ClientConnection conn) {
		this.clientConnections.remove(conn);
	}
	
	/**
	 * Collection containing the server connection of all the
	 * (connected) other servers with this chat server.
	 */
	private Vector<ServerConnection> serverConnections = new Vector<ServerConnection>();
	
	/**
	 * Returns a collection containing the server connection of all the
	 * (connected) other servers with this chat server.
	 */
	private Collection<ServerConnection> getServerConnections() {
		return this.serverConnections;
	}
	
	/**
	 * Adds the given server connection to the collection of server connections
	 * of this chat server.
	 * 
	 * @param 	conn
	 * 			The server connection that has to be added.
	 * @post	If the given server connection doesn't refer the null reference
	 * 			and the collection of server connections of this chat server
	 * 			doesn't contain the given server connection, the collection of
	 * 			server connections of this chat server contains the given server
	 * 			connection.
	 * 			| if (conn != null && !getServerConnections().contains(conn))
	 * 			| 	then new.getServerConnections().contains(conn)
	 */
	private void addServerConnection(ServerConnection conn) {
		if (conn != null && !getServerConnections().contains(conn)) {
			this.serverConnections.add(conn);
		}
	}
	
	/**
	 * Removes the given server connection from the collection of server
	 * connections of this chat server.
	 * 
	 * @param 	conn
	 * 			The server connection that has to be removed.
	 * @post	The given server connection is removed from the collection
	 * 			of server connections of this chat server.
	 * 			| !new.getServerConnections().contains(conn)
	 */
	private void removeServerConnection(ServerConnection conn) {
		this.serverConnections.remove(conn);
	}
	
	/**
	 * Collection containing the voted nicknames of this chat server.
	 */
	private Vector<String> votedNicknames = new Vector<String>();
	
	/**
	 * Returns the collection of voted nicknames of this chat server.
	 */
	private Collection<String> getVotedNicknames() {
		return this.votedNicknames;
	}
	
	/**
	 * Adds the given nickname to the collection of voted nicknames
	 * of this chat server.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be added.
	 * @post	If the given nickname doesn't refer the null reference,
	 * 			the collection of voted nicknames of this chat server contains
	 * 			the given nickname.
	 * 			| if(nickname != null)
	 * 			| 	then getVotedNicknames().contains(nickname)
	 * @post	If the collection of voted nicknames contains the
	 * 			given nickname n (with n>0) times before calling,
	 * 			the collection of voted nicknames contains the given
	 * 			nickname n+1 times.
	 */
	private void addVotedNickname(String nickname) {
		if (nickname != null) {
			this.votedNicknames.add(nickname);
		}
	}
	
	/**
	 * Removes the given nickname once from the collection of voted nicknames
	 * of this chat server.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be removed.
	 * @post	If the collection of voted nicknames contains the
	 * 			given nickname n (with n>0) times, the collection of
	 * 			voted nicknames will contain the given nickname n-1 times.
	 */
	private void removeVotedNickname(String nickname) {
		votedNicknames.remove(nickname);
	}
	
	/**
	 * Map containing the current votes for nicknames.
	 * The key represents a voting in progress for a nickname
	 * and the corresponding value is the current amount of votes.
	 */
	private Map<String, Integer> pendingRequests = new HashMap<String, Integer>();
	
	/**
	 * Increment the current amount of votes of the given nickname
	 * by one, if the given nickname has a voting in progress.
	 * 
	 * @param 	nickname
	 * 			The nickname that has a voting in progress.
	 */
	private void incrementVoteForNickname(String nickname) {
		Integer amount = this.pendingRequests.get(nickname);
		if (amount != null) {
			this.pendingRequests.put(nickname, amount + 1);
		}
	}
	
	/**
	 * Checks if the given nickname has enough votes
	 * to be accepted.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be checked.
	 * @return	True if and only if the given nickname
	 * 			received enough votes. A nickname is
	 * 			accepted if it achieved 50% or more of
	 * 			the possible votes. Every server has one
	 * 			vote.
	 */
	private boolean isAccepted(String nickname) {
		Integer i = getNumberOfVotes(nickname);
		if (i == null) {
			return false;
		}
		double ratio = ((double) i)/ ((double) ServerConfig.getNbServers());
		return (MINIMUM_RATIO.compareTo(ratio) <= 0);
	}
	
	/**
	 * The minimum ratio of votes that has to be required
	 * in order to accept a pending nickname.
	 */
	private static final Double MINIMUM_RATIO = 0.5d;
	
	/**
	 * Returns the number of votes for the given nickname.
	 * Returns null if there is no voting in progress for
	 * the given nickname.
	 * 
	 * @param 	nickname
	 * 			The nickname for which the number of votes
	 * 			has to be returned.
	 * @return	The number of votes for the given nickname.
	 * 			Returns null if there is no voting in progress
	 * 			for the given nickname.
	 */
	private Integer getNumberOfVotes(String nickname) {
		return this.pendingRequests.get(nickname);
	}
	
	/**
	 * Adds the given nickname to the collection of nicknames
	 * for which there is a voting in progress and initializes
	 * the number of votes for the given nickname to one vote.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be added.
	 */
	private void addNewVoting(String nickname) {
		if(nickname != null) {
			this.pendingRequests.put(nickname, 1);
		}
	}
	
	/**
	 * Removes the given nickname from the collection of nicknames
	 * for which there is a voting in progress.
	 * 
	 * @param 	nickname
	 * 			The nickname that has to be removed.
	 */
	private void removeVoting(String nickname) {
		this.pendingRequests.remove(nickname);
	}
}