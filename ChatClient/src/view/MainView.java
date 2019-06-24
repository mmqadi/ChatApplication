package view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

import client.ChatClient;
import client.MessageListener;
import client.UserStatusListener;

public class MainView  extends JFrame implements UserStatusListener, MessageListener{
	private ChatClient client;
	private JList<String> usersListUI;
	private DefaultListModel<String> usersList;
	
	private JList<String> messageListUI;
	private DefaultListModel<String> messageList;
	
	public static void main(String[] args) {
		new MainView();

	}
	
	public MainView() {
		client = new ChatClient("localhost", 8080);
		client.connect();
		client.addUserListener(this);
		client.addmessageListener(this);
		
		 try {
	            //here you can put the selected theme class name in JTattoo
	            UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
	 
	        } catch (ClassNotFoundException ex) {
	         //   java.util.logging.Logger.getLogger(this..class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (InstantiationException ex) {
	           // java.util.logging.Logger.getLogger(PC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (IllegalAccessException ex) {
	           // java.util.logging.Logger.getLogger(PC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
	           // java.util.logging.Logger.getLogger(PC.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        }
		
		addComponents();
		try {
			client.login(JOptionPane.showInputDialog("Enter User Name"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initialise();
		
	
	}
	
	public void initialise() {
		this.setTitle("Chat Application (" + client.getName() + ")");
		this.setSize(700, 700);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
	}
	
	public void addComponents() {
		usersList = new DefaultListModel<>();
		usersListUI = new JList<>(usersList);
		
		messageList = new DefaultListModel<>();
		messageListUI = new JList<>(messageList);
		this.setLayout(new BorderLayout());
		this.add(new SidePanel(),BorderLayout.WEST);
		this.add(new MainChatPanel(), BorderLayout.CENTER);
		
		
	}
	
	class SidePanel extends JPanel {
		public SidePanel() {
			this.setLayout(new BorderLayout());
			//add title
			this.add(new JLabel("Online Users"), BorderLayout.NORTH);
			
			JPanel userListPanel = new JPanel();
			userListPanel.setLayout(new BorderLayout());
			userListPanel.add(new JScrollPane(usersListUI), BorderLayout.CENTER);
			this.add(userListPanel, BorderLayout.CENTER);
			
		}
		
	}
	
	class MainChatPanel extends JPanel {
		public MainChatPanel() {
			this.setLayout(new BorderLayout(5,5));
			this.add(new JScrollPane(messageListUI), BorderLayout.CENTER);
			
			//chat input
			JPanel inputPanel = new JPanel();
			inputPanel.setLayout(new BorderLayout());
			JTextArea text = new JTextArea();
			JButton send = new JButton("send");
			inputPanel.add(text, BorderLayout.CENTER);
			inputPanel.add(send, BorderLayout.EAST);
			send.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String msg = text.getText();
					messageList.addElement("you: "+ msg);
					try {
						client.sendGroupMessage(msg);
						text.setText("");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			
			this.add(inputPanel, BorderLayout.SOUTH);
			
		}
	}
	@Override
	public void online(String user) {
		usersList.addElement(user);
	}

	@Override
	public void offline(String user) {
		usersList.removeElement(user);
		
	}

	@Override
	public void onDirectMessage(String sender, String message) {
		String msg = sender+": " +message;
		messageList.addElement(msg);
	}

	@Override
	public void onGroupMessage(String sender, String message) {
		String msg = sender+": " +message;
		messageList.addElement(msg);
	}
	
}
