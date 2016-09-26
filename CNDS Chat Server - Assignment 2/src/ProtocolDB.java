/**
 * The ProtocolDB class contains all the commands that can been exchanged between the clients and servers.
 */
public final class ProtocolDB {
	
	// With the CLIENTCONNECT command, the client asks the server for a connection. The client will add his nickname.
	static final String CLIENTCONNECT_COMMAND = "CLIENTCONNECT";
	// With the ACCEPTED command tells a server to a client that the connect has succeeded.
	static final String ACCEPTED_COMMAND = "ACCEPTED";
	// With the REJECTED command tells a server to a client that the connect is failed.
	static final String REJECTED_COMMAND = "REJECTED";
	// With the CLIENTDISCONNECT command, the client tells the server he is disconnecting.
	static final String CLIENTDISCONNECT_COMMAND = "CLIENTDISCONNECT";

	// With the SERVERCONNECT command, some other server asks this server for a connection.
	static final String SERVERCONNECT_COMMAND = "SERVERCONNECT";
	// With the SERVERDISCONNECT command, some other server tells this server he is disconnecting.
	static final String SERVERDISCONNECT_COMMAND = "SERVERDISCONNECT";

	// A CLIENTMESSAGE is a textmessage that has been sent by the client.
	static final String CLIENTMESSAGE_COMMAND = "CLIENTMESSAGE";
	// A SERVERMESSAGE is the forwarding of a clientmessage by the server, either to other clients or to the other servers. This message will contain the nickname of the sender.
	static final String SERVERMESSAGE_COMMAND = "SERVERMESSAGE";
	
	// With the CHECK_NICKNAME, a server asks to vote if the nickname can be used.
	static final String CHECK_NICKNAME_COMMAND = "CHECK_NICKNAME";	
	// With the VOTE command, a server will send his vote for a server nickname.
	static final String VOTE_COMMAND = "VOTE";
	// With the RELEASE_NICKNAME command, a server tells another server to release a certain nickname.
	static final String RELEASE_NICKNAME_COMMAND = "RELEASE_NICKNAME";
	
	// The COMMAND_DELIMITER is used for separating the different fields of the network messages.
	static final String COMMAND_DELIMITER = "#";
		
}
