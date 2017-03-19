# SimpleChatApp

What is this?

It's a simple chat app I'm working on for fun

Database of Users:

use a simple file called "user_pass.txt" to store usernames and password
e.g. SomeUser SomePasword

How to run the server:

java MyServer <some_port>
e.g. java MyServer 4111

How to run client:

Currently just use telnet
e.g. telnet localhost 4111

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
