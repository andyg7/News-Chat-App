import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

public class MyClient implements Runnable {

	private String api_keyFileName;
	private String sources_options_FileName = "/Users/andrewgrant/Documents/My-Chat-App/database_files/sources_options.txt";
	private String articles_options_FileName = "/Users/andrewgrant/Documents/My-Chat-App/database_files/articles_options.txt";
	private String api_key;
	private NewsApi apiHandler;
	private String hostName;
	private int portNumber;
	private int sender;
	private Socket socket;
	private LinkedList<Article> prevUrlSeen;
	private HashSet<String> sources_options;
	private HashSet<String> articles_options;

	public MyClient(Socket socket, int s, String apiFileLocation)  {
		initializeDataStructures();
		this.socket = socket;
		this.api_keyFileName = apiFileLocation;
		readAPIKey(this.api_keyFileName);
		readOptionsData(this.sources_options_FileName, sources_options);
		readOptionsData(this.articles_options_FileName, articles_options);
		apiHandler = new NewsApi(this.api_key);
		this.sender = s;
	}

	private void initializeDataStructures() {
		prevUrlSeen = new LinkedList<Article>();
		sources_options = new HashSet<String>();
		articles_options = new HashSet<String>();
	}

	private void readAPIKey(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			line = br.readLine();
			this.api_key = line;
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readOptionsData(String fileName, HashSet<String> options) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				options.add(line);
			}
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
				MyClient.clientsDone = 1;
			} else {
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				while ((userInput = stdIn.readLine()) != null) {
					boolean continue_parsing = processCommand(userInput);
					if (continue_parsing == false) {
						break;
					}
					if (MyClient.clientsDone == 1) {
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
				HashMap<String, String> options = new HashMap<String, String>();
				if (parsedMessage.length <= 1) {
					System.out.println("articles usage: articles <news_source> <sort_by>");
					return true;
				}
				String newsSource = parsedMessage[1];
				for (int i = 2; i < parsedMessage.length; i++) {
					String option = parsedMessage[i];
					String[] splitOption = option.split("=");
					String optionName = splitOption[0];
					if (articles_options.contains(optionName)) {
						if (splitOption.length > 1) {
							options.put(optionName, splitOption[1]);
						}
					} else {
						System.out.println("Unrecognized option: " + optionName);
					}
				}
				String response = apiHandler.sendGetArticles(newsSource, options);
				if (response != null) {
					parseJsonArticles(newsSource, response);
				}
				out.println("");
			} else if (parsedCommand.equals("news_sources")) {
				String response;
				HashMap<String, String> options = new HashMap<String, String>();
				if (parsedMessage.length == 1) {
					response = apiHandler.sendGetSources(options);
				} else {
					for (int i = 1; i < parsedMessage.length; i++) {
						String option = parsedMessage[i];
						String[] splitOption = option.split("=");
						String optionName = splitOption[0];
						if (sources_options.contains(optionName)) {
							if (splitOption.length > 1) {
								options.put(splitOption[0], splitOption[1]);
							} 
						} else {
							System.out.println("Unrecognized option: " + optionName);
						}
					}
					response = apiHandler.sendGetSources(options);
				}
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (parsedCommand.equals("Done")) {
				out.println(fullCommand);
				return false;
			} else if (parsedCommand.equals("send")) {
				if (parsedMessage.length <= 1) {
					return true;
				}
				String articleNo = parsedMessage[2];
				StringBuilder sb = new StringBuilder("");
				for (int i = 1; i < articleNo.length() - 1; i++) {
					sb.append(articleNo.charAt(i));	
				}
				String s = sb.toString();
				int urlIndex = Integer.parseInt(s);
				if (urlIndex > prevUrlSeen.size())  {
					System.out.println("Invalid article index");
					return true;
				}
				String articleInfo = prevUrlSeen.get(urlIndex).toString();
				String receipient = parsedMessage[1];
				String newCommand = "message " + receipient + " " + articleInfo;
				out.println(newCommand);
			} else if (parsedCommand.equals("categories")) {
				String categories = "business, entertainment, gaming, general, music, science-and-nature, sport, technology";
				System.out.println(categories);	
			} else if (parsedCommand.equals("languages")) {
				String languages = "english=en, german=de, french=fr";
				System.out.println(languages);	
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
			e.printStackTrace();
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
				String description = jsonObj.getString("description");
				System.out.println(description);
				System.out.println("Url:");
				String url = jsonObj.getString("url");
				System.out.println(url);
				Article newArticle = new Article(source, title, url, description);
				this.prevUrlSeen.addLast(newArticle);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int clientsDone;
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Invalid number of arguments.");
			return;
		}
		String h = args[0];
		int p = Integer.parseInt(args[1]);
		String apiFileLocation = args[2];
		Socket s;
		try {
			clientsDone = 0;
			s = new Socket(h, p);
			MyClient senderClient = new MyClient(s, 1, apiFileLocation);
			MyClient receiverClient = new MyClient(s, 0, apiFileLocation);
			Thread t1 = new Thread(senderClient, "sender");
			Thread t2 = new Thread(receiverClient, "receiver");
			t1.start();
			t2.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
