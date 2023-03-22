package chatTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;
	
	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			System.out.println("clientUsername: " + clientUsername);
			clientHandlers.add(this);
			broadcastMessage("[CONNECTED] " + clientUsername + " 님이 입장하셨습니다.");
		}catch(IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	@Override
	public void run() {
		String messageFromClient;
		
		while(socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				broadcastMessage(messageFromClient);
			} catch(IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
				break;
			}
		}
	}

	private void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if(!clientUsername.equals(clientHandler.clientUsername)) {
					clientHandler.bufferedWriter.write(messageToSend);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
					try {
			            URL url = new URL("http://localhost:8081/message"); // REST API 요청을 보낼 URL
			            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // URL 연결 객체 생성
			            conn.setRequestMethod("POST"); // 요청 방식 설정
			            conn.setDoOutput(true);	// outputStream으로 POST데이터를 요청설정
			            conn.setRequestProperty("Content-Type", "text/plain");
			            String data = messageToSend;
			            OutputStream os = conn.getOutputStream();
			            os.write(data.getBytes());
			            os.flush();
			            os.close();

			            int responseCode = conn.getResponseCode(); // 서버 응답 코드 확인
			            if (responseCode != HttpURLConnection.HTTP_OK) { // 요청이 성공한 경우
			            	System.out.println("REST API 요청에 실패했습니다.");
			            } 
			        } catch (Exception e) {
			            System.out.println("예외가 발생했습니다. " + e.getMessage());
			        }
				}
			} catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
		}
	}
	
	public void removeClientHandler() {
		clientHandlers.remove(this);
		broadcastMessage("[DISCONNECTED] " + clientUsername + "님이 나갔습니다.");
	}
	
	private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removeClientHandler();
		try {
			if(bufferedReader != null) {
				bufferedReader.close();
			}
			if(bufferedWriter != null) {
				bufferedWriter.close();
			}
			if(socket != null) {
				socket.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
