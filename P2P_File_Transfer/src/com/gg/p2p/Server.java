package com.gg.p2p;

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextArea;

import net.proteanit.sql.DbUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class Server implements Runnable {

	private JFrame frmServer;
	private static ServerSocket serverSocket = null;
	private static Server server = null;	
    private static DataInputStream input = null;
    private static DataOutputStream output = null;
 	static Connection conn = null;
 	static ResultSet res = null;
 	static PreparedStatement pst = null;
 	private static JLabel lblServerStatus;
 	private static JLabel lblServerStatusImage;
 	private static JTextArea txtPeerList;
 	private static Thread serverStarter;
	private volatile static boolean serverOnline = false;
 	private static int clientCount = 0;
	private PeerHandler[] peers = new PeerHandler[50];
    private static JTable tblFiles;
  

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frmServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		server = new Server(8888);
	}

	/**
	 * Create the application.
	 */
	
	public Server() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmServer = new JFrame();
		frmServer.setTitle("Server");
		frmServer.setBounds(100, 100, 667, 446);
		frmServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmServer.getContentPane().setLayout(null);
		
		JLabel lblPeerList = new JLabel("Peer list:");
		lblPeerList.setBounds(27, 91, 160, 14);
		frmServer.getContentPane().add(lblPeerList);
		
		txtPeerList = new JTextArea();
		txtPeerList.setEditable(false);
		txtPeerList.setBounds(24, 116, 163, 262);
		frmServer.getContentPane().add(txtPeerList);
		
		lblServerStatusImage = new JLabel("");
		lblServerStatusImage.setIcon(new ImageIcon(Server.class.getResource("/com/gg/resources/offline.png")));
		lblServerStatusImage.setBounds(27, 25, 25, 25);
		frmServer.getContentPane().add(lblServerStatusImage);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				connect();
			}
		});
		btnConnect.setBounds(171, 27, 89, 23);
		frmServer.getContentPane().add(btnConnect);
		
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				disconnect();
			}
		});
		btnDisconnect.setBounds(270, 27, 112, 23);
		frmServer.getContentPane().add(btnDisconnect);
		
		lblServerStatus = new JLabel("Disconected");
		lblServerStatus.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblServerStatus.setBounds(61, 25, 111, 25);
		frmServer.getContentPane().add(lblServerStatus);
		
		JButton btnAccounts = new JButton("Accounts");
		btnAccounts.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent arg0) {
				if(serverOnline) {
					Accounts objAccounts = new Accounts(conn);
					objAccounts.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(null, "Please connect to server!");
				}
			}
		});
		btnAccounts.setBounds(552, 25, 89, 23);
		frmServer.getContentPane().add(btnAccounts);
		
		JLabel lblFileList = new JLabel("File list:");
		lblFileList.setBounds(194, 91, 66, 14);
		frmServer.getContentPane().add(lblFileList);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(197, 116, 444, 262);
		frmServer.getContentPane().add(scrollPane);
		
		tblFiles = new JTable() {
		    /**
			 * Files Table
			 */
			private static final long serialVersionUID = -8797850998211366970L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		        return false;
		    }
		};

		tblFiles.setShowHorizontalLines(false);
		tblFiles.setShowGrid(false);
		scrollPane.setViewportView(tblFiles);
		
	}
	
	public Server(int port) {  
		try {  
			System.out.println("Binding to port " + port + ", please wait...");
			serverSocket = new ServerSocket(port);  
			System.out.println("Server started: " + serverSocket);
			
		} catch(IOException ioe) {  
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); 
		}
	}
	
	public static void updateTable() {
		try {
			String sql = "select ROW_NUMBER() OVER (ORDER BY added DESC) AS No, name as Name, pretty_size(size) as Size, type as Type, DATE_FORMAT(added, '%d/%c/%Y  %H:%i') as Added from files";
			
			pst = conn.prepareStatement(sql);
			res = pst.executeQuery();
			
			tblFiles.setModel(DbUtils.resultSetToTableModel(res));
			pst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void connect(){
		try {
			if(!serverOnline) {	

				Properties properties = new Properties();

				try {
					properties.load(Server.class.getResourceAsStream("server.properties"));
										
					conn = SqlDbConnector.connectToDb(
							properties.getProperty("DB_HOST"),
							properties.getProperty("DB_PORT"),
							properties.getProperty("DB_NAME"), 
							properties.getProperty("DB_USERNAME"), 
							properties.getProperty("DB_PASSWORD")
					);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				serverStarter = new Thread(new Runnable() {
					
					@Override
					public void run() {
						server.run();
					}
				});
				
				updateTable();
				
				serverStarter.start();
				
				if(serverSocket.isClosed()) {
					serverSocket = new ServerSocket(8888);
				}
				
				lblServerStatus.setText("Connected");
				
				lblServerStatusImage.setIcon(new ImageIcon(Server.class.getResource("/com/gg/resources/online.png")));
				serverOnline = true;
			} else {
				JOptionPane.showMessageDialog(null, "Server is currently active!");
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public synchronized static void disconnect() {
		try {
			server.stop();
			serverStarter.interrupt();
			
			serverSocket.close();
			
			server.dissconnectPeers();
			
			lblServerStatus.setText("Dissconected");
			
			serverOnline = false;
			
			lblServerStatusImage.setIcon(new ImageIcon(Server.class.getResource("/com/gg/resources/offline.png")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	private int findPeer(int id) {
		for (int i = 0; i < clientCount; i++)
			if (peers[i].getID() == id)
				return i;
		return -1;
	}
	
	public synchronized void remove(int id) {  
		int pos = findPeer(id);
	    if (pos >= 0) {
	    	PeerHandler toTerminate = peers[pos];
	        System.out.println("Removing peer thread " + id + " at " + pos);
	        if (pos < clientCount-1)
	        	for (int i = pos+1; i < clientCount; i++)
	        		peers[i-1] = peers[i];
	        clientCount--;
	        updateOnlinePeers();
	        try {  
	        	toTerminate.close();
	        	toTerminate.interrupt();
	        } catch(IOException ioe) {  
	        	System.out.println("Error closing thread: " + ioe); 
	        }
	    }
	}
	
	public synchronized void dissconnectPeers() throws IOException {
		for (int i = 0; i < clientCount; i++) {
    		peers[i].close();
    		peers[i].interrupt();
		}
			
	} 
	
	public synchronized void updateOnlinePeers() {
       	txtPeerList.setText("");
        if(clientCount != 0) {
			for (int i = 0; i < clientCount; i++) {
	            txtPeerList.append(peers[i].getServer()+ ":" + peers[i].getID() + "\n");
	        }
        }
    }
	
	public synchronized void handle(String str) {
		
		if( str.equals("update")) {
			for (int i = 0; i < clientCount; i++) {
	            peers[i].send(str);
            }		
	    } else if (getParameterFromString(str, 0).equals("upload")) {
	    		    	
	    	 String query = "INSERT INTO `files` (`uid`, `name`, `size`, `type`, `added`) "
			 			+ "VALUES (?, ?, ?, ?, ?)";
	    	
	    	try {
	    		pst = conn.prepareStatement(query);
	    		pst.setString(1, getParameterFromString(str, 1));
	    		pst.setString(2, getParameterFromString(str, 2));
	    		pst.setString(3, getParameterFromString(str, 3));
	    		pst.setString(4, getParameterFromString(str, 4));
	    		pst.setString(5, String.valueOf(LocalDateTime.now()));
	    		pst.executeUpdate(); 
			} catch (SQLException e1) {
				e1.printStackTrace();
			} finally {
				handle("update");
				updateTable();
			}
	    } else if (getParameterFromString(str, 0).equals("request")) {
	    	
	    	String query = "SELECT `ip`, `port` FROM `users` "
	    			+ "WHERE `id` = ?";
	    	
	    	int port = 0;
//	    	String ip = "";
	    	
	    	try {
				pst = conn.prepareStatement(query);
				pst.setString(1, getParameterFromString(str, 4));
				res = pst.executeQuery();
				
				while(res.next()) {
//					ip = res.getString("ip");
					port = res.getInt("port");
				}
				
				for (int i = 0; i < clientCount; i++) {
					if(peers[i].getID() == port) 
						peers[i].send("send" + ":" + getParameterFromString(str, 1) + ":" + getParameterFromString(str, 2) + ":" + getParameterFromString(str, 3) + 
								":" + getParameterFromString(str, 4));
					System.out.println("send" + ":" + getParameterFromString(str, 1) + ":" + getParameterFromString(str, 2) + ":" + getParameterFromString(str, 3) + 
							":" + getParameterFromString(str, 4));
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
	    	
	    } else if (getParameterFromString(str, 0).equals("assign")) {
	    	
	    	String query = "UPDATE `users` SET `ip` = ?, `port` = ? "
	    	 		+ "WHERE (`id` = ?);";
	    	 	    	
	    	try {
	    		pst = conn.prepareStatement(query);
	    		pst.setString(1, getParameterFromString(str, 1));
	    		pst.setString(2, getParameterFromString(str, 2));
	    		pst.setString(3, getParameterFromString(str, 3));
	    		pst.executeUpdate();
			} catch (SQLException e1) {
				e1.printStackTrace();
			} 
	    }		
		
		txtPeerList.setCaretPosition(txtPeerList.getDocument().getLength());    
	}
	
	private String getParameterFromString(String str, int i) {

		String[] strs = str.split(":");
		
		return strs[i];
	}

	private void addThread(Socket socket)   {  
		if (clientCount < peers.length) {  
			System.out.println("Client accepted: " + socket);
	        peers[clientCount] = new PeerHandler(this, socket);
	        
	        try {  
	        	peers[clientCount].open();
	        	peers[clientCount].start();
	            txtPeerList.append(socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "\n");
	          
	            clientCount++; 
	         } catch(IOException ioe) {  
	        	 System.out.println("Error opening thread: " + ioe); 
	         } 
	   } else {
	         System.out.println("Client refused: maximum " + peers.length + " reached.");
	   }
	}
	
	public void stop() {  
    	try {  
    		if (input != null)  input.close();
		    if (output != null)  output.close();
		    if (serverSocket != null)  serverSocket.close();
        } catch(IOException ioe) {  
    	   System.out.println("Error closing ...");
        }
    }


	@Override
	public void run() {
		while (true) {
			if (serverOnline)
				try { 
					System.out.println("Waiting for a client ..."); 
					addThread(serverSocket.accept()); 
				} catch(IOException ioe) {  
		        	System.out.println("Server accept error: " + ioe);
		        	stop(); 
		        }
	    }	
		
	}
}
