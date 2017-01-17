import java.net.*;
import java.util.*;

public class User {
	private String username;
	private SocketAddress socketAddress; 
	private ArrayList<String> blockedUsers;

	public User(String username, SocketAddress socketAddress) {
		this.username = username;
		this.socketAddress = socketAddress;
		blockedUsers = new ArrayList<String>();
	}

	public String getUserName() {
		return username;
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

}

