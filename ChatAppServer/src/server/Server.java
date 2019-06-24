package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{
	

	private final int port;
	private ArrayList<ServerWorker> workers = new ArrayList<>();
	
	public Server(int port) {
		this.port = port;
	}
	
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(this.port);
			while(true) {
				debug("waiting for connection");
				Socket client = serverSocket.accept();
				debug("accept client");
				ServerWorker worker = new ServerWorker(this, client);
				debug("creat service worer");
				worker.start();
				debug("start service worker");
				workers.add(worker);
				debug("add service worker to a list of workers");
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void debug(String msg) {
		System.out.println("Server: " + msg);
	}
	public ArrayList<ServerWorker> getWorkers() {
		return workers;
	}
	
	public void addServerWorker(ServerWorker worker) {
		workers.add(worker);
	}
	
	public void removeServerWorker(ServerWorker worker) {
		workers.remove(worker);
	}
}
