import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
	public static void main(String args[]) {
		MyServer.loggedInUsers = new HashSet<User>();
		MyServer.queuedMessages = new HashSet<QueuedMessage>();
		MyServer.blockedAddresses = new HashSet<BlockedAddress>();
		Lock lock = new ReentrantLock();
		try {
			int portNumber = Integer.parseInt(args[0]);
			ServerSocket serverSocket = new ServerSocket(portNumber);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				int openConnections = MyServer.activeCount();
				if (openConnections >= MyServer.maxNumberOpenConnections) {
					System.out.println("Closing socket - too many open");
					clientSocket.close();
				} else {
					MyServer ms = new MyServer(clientSocket, lock);
					ms.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
