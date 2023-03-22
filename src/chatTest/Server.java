package chatTest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket serverSocket; 
	
	public Server(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	
	public static void main(String[] args) throws IOException {
		
		ServerSocket serverSocket = new ServerSocket(8080);
		Server server = new Server(serverSocket);
		server.startServer();
	}
	
	public void startServer() {
		
		try {
			while(!serverSocket.isClosed()) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("클라이언트 입장ㄹ");
				ClientHandler clientHandler = new ClientHandler(clientSocket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeServerSocket() {
		
		try {
			if(serverSocket != null) {
				serverSocket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
