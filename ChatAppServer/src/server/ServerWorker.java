package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerWorker extends Thread{
	
	private String name;
	private Server server;
	private Socket client;
	private boolean online;
	private OutputStream ouputStream;
	
	public ServerWorker(Server server, Socket client) {
		this.server = server;
		this.client = client;
	}
	
	public void run() {
		try {
			Server.debug("handle client");
			handleClient();
		}
		catch (Exception e) {
			
		}
	}
	//handles all user requests
	private void handleClient() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		ouputStream = client.getOutputStream();
		String line;
		
		
		while((line = reader.readLine()) != null) {
			
			String[] tokens = line.split("\\|");
			
			if(tokens != null && tokens.length > 0) {
				String cmd = tokens[0].trim();
				if("logout".equalsIgnoreCase(cmd)) {
					handleLogoff();
				}
				else if("login".equalsIgnoreCase(cmd)) {
					handleLogin(tokens);
				}
				
				else if("msg".equalsIgnoreCase(cmd)) {
					handleMessage(tokens);
				}
			}
			else {
				ouputStream.write("unknown command".getBytes());
			}
		}
		
		
	}

	private void handleMessage(String[] tokens) throws IOException {
		if(tokens.length > 2) {
			String type = tokens[1];
			
			if("grp".equalsIgnoreCase(type) && tokens.length == 3) {
				String msg = "msg|grp|"+ this.name+"|" + tokens[2]+"\n";
				
				for(ServerWorker worker: server.getWorkers()) {
					if(!this.equals(worker) && worker.isOnline()) {
						worker.send(msg);
					}
				}
			}
			else if("p".equalsIgnoreCase(type) && tokens.length == 4) {
				String name = tokens[2];
				String msg = "msg|p|"+ this.name+"|" + tokens[3]+"\n";
				
				for(ServerWorker worker: server.getWorkers()) {
					if(!this.equals(worker) && worker.isOnline() && worker.getname().equals(name)) {
						worker.send(msg);
						break;
					}
				}
			}
		}
		
	}
	//removes the server worker from the list of active workers
	public void handleLogoff() throws IOException {
		server.removeServerWorker(this);
		String msg = "offline|"+this.name+"\n";
		for(ServerWorker worker: server.getWorkers()) {
			if(!this.equals(worker) && worker.isOnline()) {
				worker.send(msg);
			}
		}
		this.online = false;
		client.close();
	}

	private void handleLogin(String[] tokens) throws IOException {
		Server.debug(""+tokens.length);
		for(int i = 0; i < tokens.length; i++)
			Server.debug(tokens[i]);
		if(tokens.length == 2) {
			
			this.name = tokens[1];
			this.online = true;
			ouputStream.write("OK\n".getBytes());
			String msg = "online|"+this.name+"\n";
			ArrayList<ServerWorker> users = server.getWorkers();
			
			for(ServerWorker worker: users) {
				if(!worker.equals(this))
					worker.send(msg);
			}
		}
		else {
			ouputStream.write("error login\n".getBytes());
		}
		
	}
	
	public void send(String massage) throws IOException {
		if(isOnline())
			ouputStream.write(massage.getBytes());
	}

	public String getname() {
		return name;
	}

	public boolean isOnline() {
		
		return online;
	}
	
	
}
