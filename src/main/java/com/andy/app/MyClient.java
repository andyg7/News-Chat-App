import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;


public class MyClient implements Runnable {
	private String api_keyFileName = "/Users/andrewgrant/Documents/My-Chat-App/API_key/api_key";
	private String api_key;
	private NewsApi apiHandler;
	private String hostName;
	private int portNumber;
	private int sender;
	private Socket socket;
	private LinkedList<Article> prevUrlSeen;

	public MyClient(Socket socket, int s)  {
		this.socket = socket;
		readAPIKey();
		apiHandler = new NewsApi(this.api_key);
		this.sender = s;
		initializeDataStructures();
	}

	private void initializeDataStructures() {
		prevUrlSeen = new LinkedList<Article>();
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
		try {
			String userInput;
			if (this.sender == 1) {
				BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				while ((userInput = in.readLine()) != null) {
					System.out.println(userInput);
					if (userInput.equals("Done")) {
						break;
					}
				}
			} else {
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				while ((userInput = stdIn.readLine()) != null) {
					boolean continue_parsing = processCommand(userInput);
					if (continue_parsing == false) {
						break;
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean processCommand(String fullCommand) {
		String[] parsedMessage = fullCommand.split(" ");
		String parsedCommand = parsedMessage[0];
		if (parsedMessage.length == 0) {
			return true;
		}
		try {
			PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
			if (parsedCommand.equals("articles")) {
				if (parsedMessage.length <= 1) {
					return true;
				}
				String newsSource = parsedMessage[1];
				String response = apiHandler.sendGetArticle(newsSource);
				if (response != null) {
					parseJsonArticles(newsSource, response);
				}
				out.println("");
			} else if (parsedCommand.equals("news_sources")) {
				String response = apiHandler.sendGetSource();
				if (response != null) {
					parseJsonSources(response);
				}
				out.println("");
			} else if (parsedCommand.equals("read")) {
				if (parsedMessage.length <= 1) {
					return true;
				}
				String articleUrl = parsedMessage[1];
				if (articleUrl.charAt(0) == '[' && articleUrl.charAt(articleUrl.length() - 1) == ']') {
					StringBuilder sb = new StringBuilder("");
					for (int i = 1; i < articleUrl.length() - 1; i++) {
						sb.append(articleUrl.charAt(i));	
					}
					String s = sb.toString();
					int urlIndex = Integer.parseInt(s);
					if (urlIndex > prevUrlSeen.size())  {
						System.out.println("Invalid article index");
						return true;
					}
					articleUrl = prevUrlSeen.get(urlIndex).url;
				}
				Process p;
				try {
					p = Runtime.getRuntime().exec(new String[] { "open", articleUrl });
					p.waitFor();
					int ev = p.exitValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String response = apiHandler.sendGetArticleContent(articleUrl);
				if (response != null) {
					//System.out.println(response);
				}
			} else if (parsedCommand.equals("Done")) {
				out.println(fullCommand);
				return false;
			} else {
				out.println(fullCommand);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void parseJsonSources(String response) {
		try {
			JSONObject json = new JSONObject(response);
			JSONArray arr = json.getJSONArray("sources");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject jsonObj  = arr.getJSONObject(i);
				System.out.println(jsonObj.getString("name"));
			}
		} catch (Exception e) {

		}
	}

	public void parseJsonArticles(String source, String response) {
		prevUrlSeen.clear();
		try {
			JSONObject json = new JSONObject(response);
			JSONArray arr = json.getJSONArray("articles");
			for (int i = 0; i < arr.length(); i++) {
				System.out.println("[" + i + "]");
				JSONObject jsonObj  = arr.getJSONObject(i);
				System.out.println("Title:");
				String title = jsonObj.getString("title");
				System.out.println(title);
				System.out.println("Description:");
				System.out.println(jsonObj.getString("description"));
				System.out.println("Url:");
				String url = jsonObj.getString("url");
				System.out.println(url);
				Article newArticle = new Article(source, title, url);
				this.prevUrlSeen.addLast(newArticle);
			}
		} catch (Exception e) {

		}
	}

	public static void main(String args[]) {
		String h = args[0];
		int p = Integer.parseInt(args[1]);
		Socket s;
		try {
			s = new Socket(h, p);
			MyClient senderClient = new MyClient(s, 1);
			MyClient receiverClient = new MyClient(s, 0);
			Thread t1 = new Thread(senderClient, "sender");
			Thread t2 = new Thread(receiverClient, "receiver");
			t1.start();
			t2.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
