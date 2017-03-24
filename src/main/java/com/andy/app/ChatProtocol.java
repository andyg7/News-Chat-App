public class ChatProtocol {

	public ChatProtocol() {

	}

	public String processInput(String input) {
		String output;
		if (input.equals("Done")) {
			output = "Done";
		} else {
			output = "hey";
		}
		return output;
	}
}
