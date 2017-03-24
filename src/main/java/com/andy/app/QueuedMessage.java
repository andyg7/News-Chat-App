public class QueuedMessage {
	private String usernameTo; 
	private String usernameFrom; 
	private String message;

	public QueuedMessage(String usernameTo, String usernameFrom, String message) {
		this.usernameTo = usernameTo;
		this.usernameFrom = usernameFrom;
		this.message = message;
	}

	public String getUsernameTo() {
		return this.usernameTo;
	}

	public String getUsernameFrom() {
		return this.usernameFrom;
	}

	public String getMessage() {
		return this.message;
	}
}
