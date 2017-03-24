import java.io.*;
import java.net.*;

public class MyClient implements Runnable {
	private String api_keyFileName = "../API_key/api_key";
	private String api_key;
	private NewsApi apiHandler;
	private String hostName;
	private int portNumber;
	private int sender;
	private Socket socket;

	public MyClient(Socket socket, int s)  {
		this.socket = socket;
		readAPIKey();
		this.sender = s;
	}

	private void readAPIKey() {
		try (BufferedReader br = new BufferedReader(new FileReader(api_keyFileName))) {
			String line;
			line = br.readLine();
			this.api_key = line;
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String userInput;
			if (this.sender == 1) {
				BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				while ((userInput = in.readLine()) != null) {
					System.out.println(userInput);
				}
			} else {
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
				while ((userInput = stdIn.readLine()) != null) {
					userInput = userInput + '\n';
					out.println(userInput);
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		String h = args[0];
		int p = Integer.parseInt(args[1]);
		Socket s;
		try {
			s = new Socket(h, p);
			//PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			//out.println("hyyyyy");
			MyClient senderClient = new MyClient(s, 1);
			MyClient receiverClient = new MyClient(s, 0);
			Thread t1 = new Thread(senderClient, "sender");
			Thread t2 = new Thread(receiverClient, "receiver");
			System.out.println("about to start");
			t1.start();
			t2.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
