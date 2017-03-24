# News Chat App

What is this?

It's a chat app for news junkies to share news. Currently powered by https://newsapi.org/ 

Database of Users:

use a simple file called "user_pass.txt" to store usernames and password
e.g. SomeUser SomePasword

How to run the server:

java MyServer <some_port>
mvn exec:java -Dexec.mainClass=Main -Dexec.args="<port_number>"

How to run client:

mvn exec:java -Dexec.mainClass=MyClient -Dexec.args="<host> <port_number>"


Commands:

- message
   - Sends message to another user e.g. message Andy hey whats up

- Done
   - Logs user out

TODO

- Add some sort of crypto to communications
   - messages are sent in cleartext
- Add client code
- Add support for me types of commands user can use
