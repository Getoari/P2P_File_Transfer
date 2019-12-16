package com.gg.p2p;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import net.proteanit.sql.DbUtils;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

public class Accounts extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3308592757110230243L;
	private JPanel contentPane;
	private JTextField txtUsername;
	private static JTable tblUsers;
 	static Connection conn = null;
 	static ResultSet res = null;
 	static PreparedStatement pst = null;
 	private JPasswordField txtPassword;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Accounts frame = new Accounts();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the frame.
	 */
	public Accounts() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 673, 385);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblAccounts = new JLabel("Accounts");
		lblAccounts.setHorizontalAlignment(SwingConstants.CENTER);
		lblAccounts.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblAccounts.setBounds(10, 11, 637, 47);
		contentPane.add(lblAccounts);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Register new account", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(20, 63, 291, 196);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(10, 29, 385, 14);
		panel.add(lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setBounds(10, 54, 250, 20);
		panel.add(txtUsername);
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(10, 85, 385, 14);
		panel.add(lblPassword);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(txtUsername.getText().isEmpty()) 
					JOptionPane.showMessageDialog(null, "Please fill the username field!");
				else if (String.valueOf(txtPassword.getPassword()).isEmpty())
					JOptionPane.showMessageDialog(null, "Please fill the password field!");
				else {
					
					String salt = SaltedMD5.generateSalt();
					
					String query = "INSERT INTO `users` (`name`, `password`, `salt`) "
			    			+ "VALUES ('" + txtUsername.getText() + "', '" + SaltedMD5.getSecurePassword(String.valueOf(txtPassword.getPassword()), Base64.getDecoder().decode(salt)) + "', '" + salt + "')";
					
					try {
			    		pst = conn.prepareStatement(query);
			    		pst.execute();
					} catch (SQLException e1) {
						e1.printStackTrace();
					} finally {
						updateTable();
					}
					
					txtUsername.setText("");
					txtPassword.setText("");
				}
			}
		});
		btnRegister.setBounds(170, 151, 89, 23);
		panel.add(btnRegister);
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(10, 110, 250, 20);
		panel.add(txtPassword);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(321, 69, 312, 235);
		contentPane.add(scrollPane);
		
		tblUsers = new JTable();
		scrollPane.setViewportView(tblUsers);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setBounds(546, 315, 89, 23);
		contentPane.add(btnDelete);


		conn = SqlDbConnector.connectToDb("localhost", "admin", "toor");
		updateTable();
	}
	
	private static void updateTable() {
		try {
			String sql = "select id as ID,  name as Username from users";
			
			pst = conn.prepareStatement(sql);
			res = pst.executeQuery();
			
			tblUsers.setModel(DbUtils.resultSetToTableModel(res));
			pst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
