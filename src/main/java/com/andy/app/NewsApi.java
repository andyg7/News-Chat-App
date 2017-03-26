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
import java.util.*;

public class NewsApi {

	private String articlesUrlString;
	private String sourcesUrlString;
	private String api_key;	

	public NewsApi(String api_key) {
		this.articlesUrlString = "https://newsapi.org/v1/articles?";
		this.sourcesUrlString = "https://newsapi.org/v1/sources?";
		this.api_key = api_key;
	}

	public String sendGetArticles(String source, HashMap<String, String> options) {
		StringBuilder sourceWithOptions = new StringBuilder("");
		sourceWithOptions.append(this.articlesUrlString);
		sourceWithOptions.append("source=");
		sourceWithOptions.append(source);
		Iterator<String> it = options.keySet().iterator();
		while (it.hasNext()) {
			sourceWithOptions.append("&");
			String option = it.next();
			String value = options.get(option);
			sourceWithOptions.append(option);
			sourceWithOptions.append("=");
			sourceWithOptions.append(value);
		}
		System.out.println(sourceWithOptions.toString());
		return sendGetRequest(sourceWithOptions.toString(), "");
	}

	public String sendGetSources(HashMap<String, String> options) {
		StringBuilder sourceWithOptions = new StringBuilder("");
		sourceWithOptions.append(this.sourcesUrlString);
		Iterator<String> it = options.keySet().iterator();
		while (it.hasNext()) {
			String option = it.next();
			String value = options.get(option);
			sourceWithOptions.append(option);
			sourceWithOptions.append("=");
			sourceWithOptions.append(value);
			if (it.hasNext()) {
				sourceWithOptions.append("&");
			}
		}
		return sendGetRequest(sourceWithOptions.toString(), "");
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
			return null;
		}
	}
}
