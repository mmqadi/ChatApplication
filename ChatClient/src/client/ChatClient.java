package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChatClient {
	
	private final String serverName;
	private final int serverPort;
	private Socket socket;
	private OutputStream toServer;
	private InputStream fromServer;
	private BufferedReader bufferReader;
	
	private ArrayList<UserStatusListener> statusListeners = new ArrayList<>();
	private ArrayList<MessageListener> messageListeners = new ArrayList<>();
	private String name;

	public ChatClient(String serverName, int serverPort) {
		this.serverName = serverName;
		this.serverPort = serverPort;
	}
/*
	public static void main(String[] args) throws IOException {
		ChatClient client = new ChatClient("localhost", 8080);
		
		client.addUserLister(new UserStatusListener() {
			
			@Override
			public void online(String user) {
				System.out.println("Online: "+ user);
				
			}
			
			
			@Override
			public void offline(String user) {
				System.out.println("Offline: "+ user);
				
			}
		});
		
		client.addmessageLister(new MessageListener() {
			
			@Override
			public void onDirectMessage(String sender, String message) {
				System.out.println(sender+": " +message);
			}
		});
		if(!client.connect()) {
			System.err.println("not succesful");
			
		}
		else {
			System.out.println("succesfull");
			client.login("mqadi");
			client.sendDirectMessage("sizwe", "usalhona lapho my brother");
			
		}
	}
	*/

	public void sendDirectMessage(String receiver, String message) throws IOException {
		String cmd = "msg|p|"+receiver+"|"+message+"\n";
		toServer.write(cmd.getBytes());
	}
	
	public void sendGroupMessage(String message) throws IOException {
		String cmd = "msg|grp|"+message+"\n";
		toServer.write(cmd.getBytes());
	}

	public boolean login(String name) throws IOException {
		this.name = name;
		String cmd = "login|"+name+"\n";
		toServer.write(cmd.getBytes());
		
		String line = bufferReader.readLine();
		System.out.println(line);
		if(line.equals("OK")) {
			readMessages();
			return true;
		}
		
		return false;
	}
	
	public String getName() {
		return name;
	}

	public void logout() throws IOException {
		toServer.write("logout\n".getBytes());
	}
	private void readMessages() {
		Thread read = new Thread() {
			public void run() {
				messageLoop();
			}
		};
		read.start();
	}

	protected void messageLoop() {
		String line;
		try {
			while((line = bufferReader.readLine()) != null) {
				String[] tokens = line.split("\\|");
				if(tokens != null && tokens.length > 0) {
					String cmd = tokens[0];
						if("online".equals(cmd)) {
							handleOnline(tokens);
						}
						else if("offline".equals(cmd)) {
							handleOffline(tokens);
						}
						else if("msg".equals(cmd)) {
							handleMessage(tokens);
						}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				socket.close();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
		}
		
	}

	private void handleMessage(String[] tokens) {
		String type = tokens[1];
		if("p".equals(type)) {
			String sender = tokens[2];
			String message = tokens[3];
			
			for(MessageListener listener: messageListeners) {
				listener.onDirectMessage(sender, message);
			}
		}
		
		else if("grp".equals(type)) {
			String sender = tokens[2];
			String message = tokens[3];
			for(MessageListener listener: messageListeners) {
				listener.onGroupMessage(sender, message);
			}
		}
		
	}

	private void handleOffline(String[] tokens) {
		String user = tokens[1];
		for(UserStatusListener listener: statusListeners) {
			listener.offline(user);
		}
	}

	private void handleOnline(String[] tokens) {
		String user = tokens[1];
		for(UserStatusListener listener: statusListeners) {
			listener.online(user);
		}
	}

	public boolean connect() {
		try {
			this.socket = new Socket(serverName, serverPort);
			this.toServer = socket.getOutputStream();
			this.fromServer = socket.getInputStream();
			this.bufferReader = new BufferedReader(new InputStreamReader(fromServer)); 
			return true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.err.println(e.printStackTrace());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("connection failed");
		return false;
	}
	
	public void addUserListener(UserStatusListener listener) {
		statusListeners.add(listener);
	}
	
	public void removeUserListener(UserStatusListener listener) {
		statusListeners.remove(listener);
	}
	
	public void addmessageListener(MessageListener listener) {
		messageListeners.add(listener);
	}
	
	public void removemessageListener(MessageListener listener) {
		messageListeners.remove(listener);
	}
}
