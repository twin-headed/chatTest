package chatTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class VerySimpleChatServer {
	
	ArrayList clientOutputStreams; 
	
	// ClientHandler는 Runnable을 구현한 일감 -> 클라이언트로부터 오는 메세지를 계속 읽는다.
	public class ClientHandler implements Runnable {
		
		BufferedReader reader;	
		Socket sock;
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket; 
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					System.out.println("read" + message);
					tellEveryone(message);
				}
			}catch(Exception ex) {ex.printStackTrace();}
		}
	}
	
	public static void main(String[] args) {
		new VerySimpleChatServer().go();
	}
	
	public void go() {	// 8081 포트를 열고 클라이언트 요청을 계속 대기
		clientOutputStreams = new ArrayList();
		try {
			ServerSocket serverSock = new ServerSocket(8081);
			
			while(true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				System.out.println("got a connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void tellEveryone(String message) {
		Iterator it = clientOutputStreams.iterator();
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
