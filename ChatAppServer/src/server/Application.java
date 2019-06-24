package server;

public class Application {

	public static void main(String[] args) {
		Server server = new Server(8080);
		server.run();

	}

}
