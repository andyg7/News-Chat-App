public class Article {

	public String source;
	public String title;
	public String url;
	public String description;
	public Article(String source, String title, String url, String description) {
		this.source = source;
		this.title = title;
		this.url = url;
		this.description = description;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("Source: ");
		sb.append(this.source);
		sb.append(',');
		sb.append(" Title: ");
		sb.append(this.title);
		sb.append(',');
		sb.append(" Url: ");
		sb.append(this.url);
		sb.append(',');
		sb.append(this.description);
		return sb.toString();
	}
}
