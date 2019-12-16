package com.gg.p2p;

import java.awt.Desktop;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import net.proteanit.sql.DbUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Peer extends JFrame {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -486520131545036406L;
	private JPanel contentPane;
	private static JLabel lblPeer;
	private JTextField txtFilePath;
	private static Socket socket = null; 
    private static DataInputStream input = null; 
    private static DataOutputStream out = null;
 	static Connection conn = null;
 	static ResultSet res = null;
 	static PreparedStatement pst = null;
    public static final String SERVER_ADDRESS = "192.168.1.90";
    private static final int SERVER_PORT = 8888;
	protected static final String DOWNLOAD_PATH = "C:\\Downloads\\"; 
    private static JTable tblFiles;
    private static File file = null;
    private static int userId;
    private static String fileName;


	/**
	 * Create the frame.
	 * @param userId 
	 */
	public Peer(int userId) {
		setTitle("Peer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 551);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Peer.userId = userId;
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setVgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setBorder(new TitledBorder(null, "File Upload", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 70, 769, 70);
		contentPane.add(panel);
		
		txtFilePath = new JTextField();
		txtFilePath.setFont(new Font("Tahoma", Font.PLAIN, 17));
		txtFilePath.setEditable(false);
		txtFilePath.setColumns(40);
		panel.add(txtFilePath);
		
		JButton btnChooseFile = new JButton("Choose File");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				
//				FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
//				fc.setFileFilter(filter);
				
				fc.setCurrentDirectory(new java.io.File("user.home"));
                fc.setDialogTitle("Choose the file...");
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                
                if (fc.showOpenDialog(btnChooseFile) == JFileChooser.APPROVE_OPTION) {
                	file = fc.getSelectedFile();
                    txtFilePath.setText(file.getAbsolutePath());
                }
			}
		});
		panel.add(btnChooseFile);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(file != null) {
			        try {
			        	String fileName = file.getName();
			        	String[] fileType = new MimetypesFileTypeMap().getContentType(file).split("/");
			        	
			        	File directory = new File(DOWNLOAD_PATH);
			            if (! directory.exists()){
			                directory.mkdir();
			            }

			            FileInputStream is = new FileInputStream(file);
						FileOutputStream os = new FileOutputStream(DOWNLOAD_PATH + fileName);
						
						byte[] buffer = new byte[1024];
				        int length;
				        while ((length = is.read(buffer)) > 0) {
				            os.write(buffer, 0, length);
				        }
				        				
						JOptionPane.showMessageDialog(null, "File successfuly added!");	
				        
				        out.writeUTF("upload:" + fileName + ":" + userId + ":" + file.length() + ":" + fileType[0]);
				        is.close();
				        os.close();	
				        
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "Falied to add the file!");
						e.printStackTrace();
					} finally {
						
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please choose the file you want to add!");
				}
			}
		});
		panel.add(btnAdd);
		
		lblPeer = new JLabel("Peer");
		lblPeer.setHorizontalAlignment(SwingConstants.CENTER);
		lblPeer.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblPeer.setBounds(10, 11, 764, 55);
		contentPane.add(lblPeer);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 150, 769, 296);
		contentPane.add(scrollPane);
		
		JButton btnDownload = new JButton("Download");
		btnDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(fileName.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please first select a file!");
				} else {
					try { 
						
						File directory = new File(DOWNLOAD_PATH);
			            if (! directory.exists()){
			                directory.mkdir();
			            }
						
						if(file.exists())		    
							desktopOpen(file);
						else {						
							// File Receiver Thread
							Thread t1 = new Thread(new Runnable() {
								
								@Override
								public void run() {
									
									int uid = 0;
									
									String query = "SELECT uid,size FROM files where name='" + fileName +"'";
									try {
										pst = conn.prepareStatement(query);
										res = pst.executeQuery();
										
										while(res.next()){
											uid = res.getInt("uid");
								        }
										
									} catch (SQLException e2) {
										e2.printStackTrace();
									}
									
									
									ServerSocket fileServerSocket;
									try {
										fileServerSocket = new ServerSocket(0);
										
										// File request sender
										try {
											out.writeUTF("request" + ":" + socket.getLocalAddress().getHostAddress() + ":" + fileServerSocket.getLocalPort() + ":" + fileName + ":" + uid);
											System.out.println("request" + ":" + socket.getLocalAddress().getHostAddress() + ":" + fileServerSocket.getLocalPort() + ":" + fileName + ":" + uid);
										} catch (IOException e) {
											e.printStackTrace();
										}
										
										Socket fileSocket = fileServerSocket.accept();
										
										int count;
									    byte[] buffer = new byte[1024];
									    
									    InputStream is = fileSocket.getInputStream();
									    FileOutputStream fos = new FileOutputStream(DOWNLOAD_PATH + fileName);
									    BufferedOutputStream bos = new BufferedOutputStream(fos);
									    while ((count = is.read(buffer)) > 0)
									    {
									        bos.write(buffer, 0, count);
									    }
									    bos.close();
									    fileSocket.close();
									    fileServerSocket.close();
									} catch (IOException e1) {
										e1.printStackTrace();
									}	
								}
							});
							
							t1.start();		    
						}
					}  
					catch(Exception e)  {  
						e.printStackTrace();  
					}
				}
			}
		});
		btnDownload.setBounds(655, 457, 119, 23);
		contentPane.add(btnDownload);
		
		tblFiles = new JTable() {
		    /**
			 * Files Table
			 */
			private static final long serialVersionUID = 7192899895149442674L;
			@Override
		    public boolean isCellEditable(int row, int column) {
		        return false;
		    }
		};
		tblFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				DefaultTableModel model = (DefaultTableModel)tblFiles.getModel();
//				int id = (int)model.getValueAt(tblFiles.getSelectedRow(), 0);
				fileName = (String)model.getValueAt(tblFiles.getSelectedRow(), 1);
				
				file = new File(DOWNLOAD_PATH + fileName); 
				
				if(file.exists())  			        
					btnDownload.setText("Open");
				else
					btnDownload.setText("Download");
				
				if(arg0.getClickCount() % 2 == 0) 
					try {
						desktopOpen(file);
					} catch (IOException e) {
						e.printStackTrace();
					} 
												
			}
		});
		tblFiles.setShowHorizontalLines(false);
		tblFiles.setShowGrid(false);
		scrollPane.setViewportView(tblFiles);
		
		JButton btnTest = new JButton("Test");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					out.writeUTF("update");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnTest.setBounds(556, 457, 89, 23);
		contentPane.add(btnTest);
	
		initialize();
	}

	private static void initialize() {
		conn = SqlDbConnector.connectToDb(SERVER_ADDRESS, "user", "fiek!@#");
		
		updateTable();
		
		try { 
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);  
            lblPeer.setText(lblPeer.getText() + " - " + socket.getLocalSocketAddress());
            System.out.println("Connected"); 
            
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("assign" + ":" + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort() + ":" + userId);
            out.flush();   		
 
        } catch(UnknownHostException u) { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
		
		// Message Receiver Thread
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					
					String serverResponse = "";
					
					try {
						
						input = new DataInputStream(socket.getInputStream());
						out = new DataOutputStream(socket.getOutputStream());
				
						serverResponse = input.readUTF();
						System.out.println(serverResponse);
						String[] response = serverResponse.split(":");
						
						
						if(response[0].equals("update")) {
							updateTable();
						} else if (response[0].equals("send")) {

							File fileToSend = new File(DOWNLOAD_PATH + response[3]);
							Socket fileSocket = new Socket(response[1], Integer.valueOf(response[2]));
							int length = (int) fileToSend.length();
							
							System.out.println(length);
							byte[] mybytearray = new byte[length];
						    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
						    bis.read(mybytearray, 0, mybytearray.length);
						    OutputStream os = fileSocket.getOutputStream();
						    os.write(mybytearray, 0, mybytearray.length);
						    os.flush();
						    fileSocket.close();
						    bis.close();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		});
		
		t1.start();
	}
	
	private void desktopOpen(File file) throws IOException {
		
		if(!Desktop.isDesktopSupported()) {  
			System.out.println("not supported");  
			return;  
		}  
		Desktop desktop = Desktop.getDesktop();  
		
		if(file.exists())  			        
			desktop.open(file);
		
	}
	
	public static void updateTable() {
		try {
			String sql = "select id as ID, name as Name, pretty_size(size) as Size, type as Type, DATE_FORMAT(modified, '%d/%c/%Y  %H:%i') as Modified from files";
			
			pst = conn.prepareStatement(sql);
			res = pst.executeQuery();
			
			tblFiles.setModel(DbUtils.resultSetToTableModel(res));
			pst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
