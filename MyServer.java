import java.net.*;
import java.io.*;
import java.util.*;


public class MyServer extends Thread {

	private int portNumber;
	private Socket clientSocket;
	private String usersFileName = "user_pass.txt";
	private HashMap<String, String> usersHash;
	public static HashSet<User> loggedInUsers;
	public static HashSet<QueuedMessage> queuedMessages;
	private String currUsername;

	public MyServer(Socket s) {
		this.clientSocket = s;
		usersHash = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new FileReader(usersFileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] user_password = line.split(" ");
				usersHash.put(user_password[0], user_password[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Running server thread");
		this.currUsername = logIn();
		if (currUsername != null) {
			System.out.println("Welcome to the great chat app of all time!" + currUsername);
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			MyServer.loggedInUsers.add(new User(currUsername, clientSocket));
			processCommands();
		} else {
			try {
				System.out.println("Failed to log in!");
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

			System.out.println("Trying to log in user");
			passwordAttempts = 0;
			usernameAttempts = 0;
			username = null;
			password = null;
			outputLine = "Username:";

			while (usernameAttempts < 3) {
				out.println(outputLine);
				inputLine = in.readLine();
				System.out.println("user entered: " + inputLine);
				if (this.usersHash.containsKey(inputLine)) {
					username = inputLine;
					break;
				}
				usernameAttempts++;
			}
			if (username == null) {
				return null;
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

	private void processCommands() {
		try {
			String inputLine;
			String outputLine;
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			outputLine = "Command:";
			out.println(outputLine);

			System.out.println("Logged in users");

			Iterator<User> it = MyServer.loggedInUsers.iterator();
			while (it.hasNext()) {
				User currUser = it.next();
				String s = currUser.getUserName();
				System.out.println(s);
			}

			while ((inputLine = in.readLine()) != null) {
				out.println(outputLine);
				if (inputLine.equals("Done")) {
					break;
				} else {
					intrepretCommand(inputLine);
				}
			}
			System.out.println("Removing user: " + this.currUsername);
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
			String userReceiver = parsedMessage[1];
			String message = stringBuilderFromArray(parsedMessage, 2, parsedMessage.length);
			sendMessage(userReceiver, message);
		} else {

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

	private void sendMessage(String username, String message) {
		try {
			boolean foundUser = false;
			Iterator<User> it = MyServer.loggedInUsers.iterator();
			while (it.hasNext()) {
				User currUser = it.next();
				if (currUser.getUserName().equals(username)) {
					Socket receiverSocket = currUser.getSocket();
					PrintWriter out = new PrintWriter(receiverSocket.getOutputStream(), true);
					out.println(message);
					foundUser = true;
					break;
				}
			}
			if (foundUser == true) {
				System.out.println("Sent message to " + username + " from " + this.currUsername);
			} else {
				System.out.println("Failed to send message to " + username + " from " + this.currUsername);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		MyServer.loggedInUsers = new HashSet<User>();
		MyServer.queuedMessages = new HashSet<QueuedMessage>();
		int portNumber = Integer.parseInt(args[0]);
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				MyServer ms = new MyServer(clientSocket);
				ms.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean userLoggedIn(String username) {
		return MyServer.loggedInUsers.contains(username);
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

	public static void addQueuedMessage(String username, String message) {

	}
}
