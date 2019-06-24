package client;

public interface MessageListener {
	public void onDirectMessage(String sender, String message);
	public void onGroupMessage(String sender, String message);
	
}
