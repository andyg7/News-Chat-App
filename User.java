import java.net.*;
import java.util.*;

public class User {
	private String username;
	private Socket clientSocket;
	private SocketAddress socketAddress; 
	private ArrayList<String> blockedUsers;

	public User(String username, Socket clientSocket) {
		this.username = username;
		this.clientSocket = clientSocket;
		this.socketAddress = clientSocket.getRemoteSocketAddress();
		blockedUsers = new ArrayList<String>();
	}

	public String getUserName() {
		return username;
	}

	public Socket getSocket() {
		return this.clientSocket;
	}

	public String getIp() {
		return socketAddress.toString();
	}

	public void addBlockedUser(String blockedUser) {
		blockedUsers.add(blockedUser);
	}

	public void unblockUser(String blockedUser) {
		blockedUsers.remove(blockedUser);
	}

	public boolean isBlockedUser(String user) {
		for (String s : blockedUsers) {
			if (s.equals(user)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	public int hashCode() {
		return username.hashCode();
	}
	
	public boolean equals(User o) {
		return o.getUserName().equals(this.username);
	}
	
	public boolean equals(String s) {
		return s.equals(this.username);
	}
	*/

}

