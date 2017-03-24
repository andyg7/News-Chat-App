# News Chat App

What is this?

It's a chat app for news junkies to share news. Currently powered by https://newsapi.org/ 

Database of Users:

use a simple file called "user_info.txt" to store usernames and password
e.g. SomeUser SomePasword

How to run the server:

java MyServer <some_port>
mvn exec:java -Dexec.mainClass=Main -Dexec.args="<port_number>"

How to run client:

mvn exec:java -Dexec.mainClass=MyClient -Dexec.args=" <host_name> <port_number>"


Commands:

- message <person>
   - Sends message to another user e.g. message Andy hey whats up
- articles <news_source>
   - gets articles from source
- sources
   - returns news sources
- Done
   - Logs user out

TODO

- Add some sort of crypto to communications
   - messages are sent in cleartext
- Add support for more types of commands user can use
  - specifically news api ones
