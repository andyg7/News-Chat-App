import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class MyServer extends Thread {

	private int portNumber;
	private Socket clientSocket;
	private String usersFileName = "user_pass.txt";
	private HashMap<String, String> usersHash;
	private static HashSet<String> loggedInUsers;
	private static HashSet<QueuedMessage> queuedMessages;
	public MyServer(Socket s) {
		this.clientSocket = s;
		usersHash = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new FileReader(usersFileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				String[] user_password = line.split(" ");
				System.out.println(user_password[0]);
				System.out.println(user_password[1]);
				usersHash.put(user_password[0], user_password[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println("Running server thread");
		try {
			String inputLine;
			String outputLine;
			ChatProtocol chatPr = new ChatProtocol();
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
			SocketAddress sa = clientSocket.getRemoteSocketAddress();
			System.out.println(sa.toString());

			while ((inputLine = in.readLine()) != null) {
				outputLine = chatPr.processInput(inputLine);
				out.println(outputLine);
				out.println(outputLine);
				out.println(outputLine);
				out.println(outputLine);
				out.println(outputLine);
				out.println(outputLine);
				if (outputLine.equals("Done"))
					break;
			}
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
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
		return loggedInUsers.contains(username);
	}

	public static void addQueuedMessage(String username, String message) {

	}
}
