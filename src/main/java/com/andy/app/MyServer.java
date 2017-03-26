import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyServer extends Thread {

	private int portNumber;
	private Socket clientSocket;
	private Lock lock;
	private String usersFileName = "/Users/andrewgrant/Documents/My-Chat-App/database_files/user_info.txt";
	private String blockedUsersFileName = "/Users/andrewgrant/Documents/My-Chat-App/database_files/blocked_users.txt";
	private String api_keyFileName = "/Users/andrewgrant/Documents/My-Chat-App/API_key/api_key";
	private String api_key;
	private HashMap<String, String> usersHash;
	private HashSet<String> blockedUsers;
	private HashSet<String> blockedFromUsers;
	private String currUsername;
	public int blockedTime = 60 * 1000;
	public static HashSet<User> loggedInUsers;
	public static HashSet<QueuedMessage> queuedMessages;
	public static HashSet<BlockedAddress> blockedAddresses;
	public static int maxNumberOpenConnections = 10;
	private NewsApi apiHandler;

	public MyServer(Socket s, Lock l) {
		this.clientSocket = s;
		this.lock = l;
		initializeDataStructures();
		readUserPassword();
		readAPIKey();
		this.apiHandler = new NewsApi(this.api_key);
	}

	private void initializeDataStructures() {
		usersHash = new HashMap<String, String>();
		blockedUsers = new HashSet<String>();
		blockedFromUsers = new HashSet<String>();
	}

	private void readUserPassword() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(usersFileName));	
			String line;
			while ((line = br.readLine()) != null) {
				String[] user_password = line.split(" ");
				usersHash.put(user_password[0], user_password[1]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAPIKey() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(api_keyFileName));
			String line;
			line = br.readLine();
			this.api_key = line;
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Running server thread");
		if (this.stillBlocked(this.clientSocket) == true) {
			String msg = "This ip is currently blocked. Try again in <= 60 seconds";	
			try {
				PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
				out.println(msg);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				this.clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		this.currUsername = logIn();
		if (currUsername != null) {
			try {
				PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
				String welcomeMsg = "Welcome to the greatest chat app of all time " + currUsername + "!\n";
				out.println(welcomeMsg);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			MyServer.loggedInUsers.add(new User(currUsername, clientSocket));
			readBlockedUsers();
			processCommands();
		} else {
			try {
				System.out.println("Failed to log in!");
				BlockedAddress blockedAddress = new BlockedAddress();
				String[] ipParts = this.clientSocket.getRemoteSocketAddress().toString().split(":");
				blockedAddress.ip = ipParts[0];
				blockedAddress.timestamp = System.currentTimeMillis();
				MyServer.blockedAddresses.add(blockedAddress);
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String logIn() {
		try {
			int passwordAttempts;
			int usernameAttempts;
			String username;
			String password;
			String inputLine;
			String outputLine;
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));

			passwordAttempts = 0;
			usernameAttempts = 0;
			username = null;
			password = null;
			outputLine = "Username:";

			while (usernameAttempts < 3) {
				out.println(outputLine);
				inputLine = in.readLine();
				//System.out.println(inputLine);
				if (this.usersHash.containsKey(inputLine)) {
					username = inputLine;
					break;
				}
				usernameAttempts++;
			}
			if (username == null) {
				return null;
			} 

			if (MyServer.userLoggedIn(username) == true) {
				System.out.println("User already logged in");
				sendMessage(this.clientSocket, this.currUsername, "You're logged in from somewhere else");
				return null;
			} else {
				System.out.println("Valid login");
			}

			outputLine = "Password:";
			while (passwordAttempts < 3) {
				out.println(outputLine);
				inputLine = in.readLine();
				String currPassword = usersHash.get(username);
				if (inputLine.equals(currPassword)) {
					password = inputLine;
					break;
				}
				passwordAttempts++;
			}
			if (password == null) {
				return null;
			} else {
				return username;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void readBlockedUsers() {
		this.lock.lock();
		try  {
			BufferedReader br = new BufferedReader(new FileReader(blockedUsersFileName));
			String line;
			while ((line = br.readLine()) != null) {
				String[] blockedData = line.split(" ");
				if (blockedData[0].equals(this.currUsername)) {
					blockedUsers.add(blockedData[1]);
				}
				if (blockedData[1].equals(this.currUsername)) {
					blockedFromUsers.add(blockedData[0]);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.lock.unlock();
		}
	}

	private void processCommands() {
		try {
			String inputLine;
			String outputLine;
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			outputLine = "Command:";
			out.println(outputLine);

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.equals("Done")) {
					out.println("Logging user out!");
					break;
				} else {
					intrepretCommand(inputLine);
					out.println(outputLine);
				}
			}
			System.out.println("Logging out user: " + this.currUsername);
			removeLoggedInUser(this.currUsername);
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void intrepretCommand(String fullCommand) {
		String[] parsedMessage = fullCommand.split(" ");
		if (parsedMessage.length == 0) {
			return;
		}
		String parsedCommand = parsedMessage[0];
		if (parsedCommand.equals("message")) {
			if (parsedMessage.length <= 2) {
				return;
			}
			String userReceiver = parsedMessage[1];
			String message = stringBuilderFromArray(parsedMessage, 2, parsedMessage.length);
			StringBuilder sb = new StringBuilder();
			sb.append("Message received from "); 
			sb.append(this.currUsername); 
			sb.append(": " + message);
			sendMessage(userReceiver, sb.toString());
		} else if (parsedCommand.equals("whoelse")) {
			StringBuilder sb = new StringBuilder("");
			sb.append("Other online users: \n");
			Iterator<User> it = MyServer.loggedInUsers.iterator();
			while (it.hasNext()) {
				User currUser = it.next();
				if (!currUser.getUserName().equals(this.currUsername)) {
					sb.append(currUser.getUserName());
					sb.append(" ");
				}
			}
			String listOfUsers = sb.toString();
			try {
				PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
				out.println(listOfUsers);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (parsedCommand.equals("broadcast")) {
			if (parsedMessage.length != 2) {
				return;
			}
			String message = stringBuilderFromArray(parsedMessage, 1, parsedMessage.length);
			StringBuilder sb = new StringBuilder();
			sb.append("Broadcasted message: ");
			sb.append(message);
			sendMessageExcept(this.currUsername, sb.toString());
		} else if (parsedCommand.equals("unblock")) {
			if (parsedMessage.length != 2) {
				return;
			}
			String userReceiver = parsedMessage[1];
			unblockUser(userReceiver);
		} else if (parsedCommand.equals("block")){
			if (parsedMessage.length != 2) {
				return;
			}
			String userReceiver = parsedMessage[1];
			blockUser(userReceiver);
		} else {
			if (!fullCommand.equals("")) {
				String unknownCommandMessage = "Unknown command: " + fullCommand;
				sendMessage(this.currUsername, unknownCommandMessage);
			}
		}
	}

	private void sendMessage(String username, String message) {
		boolean foundUser = false;
		Iterator<User> it = MyServer.loggedInUsers.iterator();
		while (it.hasNext()) {
			User currUser = it.next();
			if (currUser.getUserName().equals(username)) {
				Socket receiverSocket = currUser.getSocket();
				sendMessage(receiverSocket, username, message);
				foundUser = true;
				break;
			}
		}
		if (foundUser == true) {
			System.out.println("Sent message to " + username + " from " + this.currUsername);
		} else {
			System.out.println("Failed to send message to " + username + " from " + this.currUsername);
		}
	}

	private void sendMessageExcept(String username, String message) {
		Iterator<User> it = MyServer.loggedInUsers.iterator();
		while (it.hasNext()) {
			User currUser = it.next();
			if (!currUser.getUserName().equals(username)) {
				Socket receiverSocket = currUser.getSocket();
				sendMessage(receiverSocket, currUser.getUserName(), message);
			}
		}
	}

	private void sendMessage(Socket s, String userName, String message) {
		try {
			if (blockedFromUsers.contains(userName)) {
				PrintWriter out = new PrintWriter(this.clientSocket.getOutputStream(), true);
				String blockedMessage = "Blocked from sending to " + userName;
				out.println(blockedMessage);
			} else {
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				out.println(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void unblockUser(String user) {
		if (!blockedUsers.contains(user)) {
			return;
		}
		this.lock.lock();
		try {
			File initial_file = new File(blockedUsersFileName);
			BufferedReader br = new BufferedReader(new FileReader(blockedUsersFileName));
			String tmp = "tmp_f";
			File tmp_file = new File(tmp);
			BufferedWriter bw = new BufferedWriter(new FileWriter(tmp_file));
			String line;
			while ((line = br.readLine()) != null) {
				String[] blockedData = line.split(" ");
				if (blockedData[0].equals(this.currUsername) && blockedData[1].equals(user)) {
					continue;
				}
				bw.write(line);
				bw.write('\n');
			}
			boolean removedBlock = tmp_file.renameTo(initial_file);
			if (removedBlock) {
				blockedUsers.remove(user);
			}
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.lock.unlock();
		}
	}

	private void blockUser(String user) {
		if (blockedUsers.contains(user)) {
			return;
		}
		this.lock.lock();
		try {
			File initial_file = new File(blockedUsersFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(initial_file, true));
			String line = this.currUsername + " " + user + '\n';
			bw.write(line);
			bw.close();
			blockedUsers.add(user);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.lock.unlock();
		}
	}

	private String stringBuilderFromArray(String[] ar, int start, int end) {
		StringBuilder sb = new StringBuilder("");
		for (int i = start; i < end; i++) {
			sb.append(ar[i]);
			if (i != end) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	public boolean stillBlocked(Socket s) {
		String currIp = s.getRemoteSocketAddress().toString().split(":")[0];
		Iterator<BlockedAddress> it = MyServer.blockedAddresses.iterator();
		while (it.hasNext()) {
			BlockedAddress currAddress = it.next();
			if (currAddress.ip.equals(currIp)) {
				long now = System.currentTimeMillis();
				if (now - currAddress.timestamp> blockedTime) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean userLoggedIn(String username) {
		Iterator<User> it = MyServer.loggedInUsers.iterator();
		while (it.hasNext()) {
			User currUser = it.next();
			if (currUser.getUserName().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public static boolean removeLoggedInUser(String username) {
		Iterator<User> it = MyServer.loggedInUsers.iterator();
		while (it.hasNext()) {
			User currUser = it.next();
			if (currUser.getUserName().equals(username)) {
				return MyServer.loggedInUsers.remove(currUser);
			}
		}
		return false;
	}

}
