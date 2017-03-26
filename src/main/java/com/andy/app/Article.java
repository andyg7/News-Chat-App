public class Article {
	public String source;
	public String title;
	public String url;
	public Article(String source, String title, String url) {
		this.source = source;
		this.title = title;
		this.url = url;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("Source: ");
		sb.append(this.source);
		sb.append(',');
		sb.append("Title: ");
		sb.append(this.title);
		sb.append(',');
		sb.append("Url: ");
		sb.append(this.url);
		sb.append(',');
		return sb.toString();
	}
}
