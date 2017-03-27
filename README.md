# News Chat App

What is this?

It's a chat app for news junkies to share news. Currently powered by https://newsapi.org/ 

Database of Users:

use a simple file called "user_info.txt" to store usernames and password
e.g. SomeUser SomePasword

How to run the server:

mvn exec:java -Dexec.mainClass=Main -Dexec.args="<port_number>"

How to run client:

mvn exec:java -Dexec.mainClass=MyClient -Dexec.args=" <host_name> <port_number> <api_file_location>"

New API Key

Need an api key to access news information from the app. Get a key at https://newsapi.org/


Commands:

- message <person>
   - Sends message to another user e.g. message Andy hey whats up
- articles <news_source>
   - gets articles from source
   - Examples:
      - articles techcrunch sortBy=latest
- news_sources
   - returns news sources
   - Examples:
      - news_sources
      - news_sources category=sport
      - news_sources language=en
      - news_sources country=us
- read
   - open article in browser
   - Examples:
   - read http://www.bbc.com/
   - read [2] (where 2 is the label given by the __last__ articles command)

- Done
   - Logs user out

TODO

- Add some sort of crypto to communications
   - messages are sent in cleartext
- Add support for more types of commands user can use
  - specifically news api ones
- Implement queued messages
- Add groups
