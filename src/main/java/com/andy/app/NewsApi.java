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

	private String articlesUrlString;
	private String sourcesUrlString;
	private String api_key;	

	public NewsApi(String api_key) {
		this.articlesUrlString = "https://newsapi.org/v1/articles?";
		this.sourcesUrlString = "https://newsapi.org/v1/sources?";
		this.api_key = api_key;
	}

	public String sendGetArticle(String source) {
		String sourceString = "&source="+source;
		return sendGetRequest(this.articlesUrlString, sourceString);
	}

	public String sendGetSource() {
		return sendGetRequest(this.sourcesUrlString, "");
	}

	public String sendGetArticleContent(String source) {
		return sendGetRequest(source, "");
	}

	private String sendGetRequest(String urlS, String param) {
		try {
			URL urlObj = new URL(urlS+param);
			HttpsURLConnection httpConnection = (HttpsURLConnection) urlObj.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("X-Api-Key", this.api_key);
			int responseCode = httpConnection.getResponseCode();

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
			e.printStackTrace();
			return null;
		}
	}
}
