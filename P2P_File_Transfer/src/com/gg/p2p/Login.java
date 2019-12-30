package com.gg.p2p;

import com.gg.p2p.Peer;
import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Login {

	private JFrame frmLoginPp;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	static Connection conn = null;
 	static ResultSet res = null;
 	static PreparedStatement pst = null;
	private static int userId;
	private static String salt;
	private static JButton btnLogin;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login window = new Login();
					window.frmLoginPp.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Properties properties = new Properties();

		try {
			properties.load(Login.class.getResourceAsStream("peer.properties"));
			
			conn = SqlDbConnector.connectToDb(
					properties.getProperty("DB_HOST"),
					properties.getProperty("DB_PORT"),
					properties.getProperty("DB_NAME"), 
					properties.getProperty("DB_USERNAME"), 
					properties.getProperty("DB_PASSWORD")
			);
			
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						btnLogin.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Create the application.
	 */
	public Login() {
		
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
	  			initialize();
//			}
//	    });	
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLoginPp = new JFrame();
		frmLoginPp.setTitle("Login - P2P File Sharing");
		frmLoginPp.setBounds(100, 100, 487, 491);
		frmLoginPp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLoginPp.getContentPane().setLayout(null);
		
		btnLogin = new JButton("Login");
		btnLogin.setEnabled(false);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				
				try {
					String query = "SELECT `id`, `salt` FROM `users`"
							+ " WHERE `name` = ?";
					
					pst = conn.prepareStatement(query);
					pst.setString(1, txtUsername.getText());
					res = pst.executeQuery();
					
					while(res.next()){
						userId = res.getInt("id");
						salt = res.getString("salt");
			        }
					pst.close();
					
					String query2 ="SELECT * FROM `users`"
							+ " WHERE `id` = ? AND `password` = ?";
					
					pst = conn.prepareStatement(query2);
					
					pst.setString(1, String.valueOf(userId));
					pst.setString(2, SaltedMD5.getSecurePassword(String.valueOf(txtPassword.getPassword()), Base64.getDecoder().decode(salt)));
					res = pst.executeQuery();
					
					res.next();
					int count = res.getRow();
					pst.close();
					
					if ( count > 0 ) {
						frmLoginPp.dispose();
						Peer objPeer = new Peer(userId, conn);
						objPeer.setVisible(true);
					} else {
						JOptionPane.showMessageDialog(null, "Incorrect Crediencials!");
					}
				} catch (NullPointerException e1) {
					JOptionPane.showMessageDialog(null, "Incorrect Crediencials!");
					e1.printStackTrace();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, e2);
				}
			}
		});
		btnLogin.setBounds(177, 378, 115, 42);
		frmLoginPp.getContentPane().add(btnLogin);
		
		txtUsername = new JTextField();
		txtUsername.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if( arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					btnLogin.doClick();
				}
			}
		});
		txtUsername.setBounds(160, 254, 150, 25);
		frmLoginPp.getContentPane().add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(160, 229, 150, 14);
		frmLoginPp.getContentPane().add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(160, 294, 150, 14);
		frmLoginPp.getContentPane().add(lblPassword);
		
		JLabel label = new JLabel("");
		ImageIcon imageIcon = new ImageIcon(Login.class.getResource("/com/gg/resources/login.png")); 
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(200, 200,  java.awt.Image.SCALE_SMOOTH);   
		imageIcon = new ImageIcon(newimg);  
		label.setIcon(imageIcon);
		label.setBounds(129, 11, 200, 201);
		frmLoginPp.getContentPane().add(label);
		
		txtPassword = new JPasswordField();
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_ENTER) {
					btnLogin.doClick();
				}
			}
		});
		txtPassword.setBounds(160, 319, 150, 25);
		frmLoginPp.getContentPane().add(txtPassword);
	}
}
