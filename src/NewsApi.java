/*
 * Powered by NewsApi.org
 * https://newsapi.org/
 */
import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class NewsApi {
	private String urlString;
	private String api_key;	

	public NewsApi(String api_key) {
		this.urlString = "https://newsapi.org/v1/articles?";;
		this.api_key = api_key;
	}

	public NewsApi(String url, String api_key) {
		this.urlString = url;
		this.api_key = api_key;
	}

	public String sendGetSource(String source) {
		String sourceString = "&source="+source;
		System.out.println(sourceString);
		return sendGetRequest(sourceString);
	}

	private String sendGetRequest(String param) {
		try {
			URL urlObj = new URL(this.urlString+param);
			HttpsURLConnection httpConnection = (HttpsURLConnection) urlObj.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("X-Api-Key", this.api_key);
			int responseCode = httpConnection.getResponseCode();
			System.out.println("Url : " + this.urlString);
			System.out.println("rc : " + responseCode);

			BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = br.readLine()) != null) {
				response.append(line);
			}

			br.close();
			return response.toString();
		}
		catch (Exception e) {
			return null;
		}
	}
	}
