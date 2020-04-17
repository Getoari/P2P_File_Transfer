package com.gg.p2p;

import java.awt.Desktop;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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
import java.text.DecimalFormat;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

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
    private static JTable tblFiles;
    private static File file = null;
    private static int userId;
    private static String fileName;
    private static int fileId;
    
    private static final int SERVER_PORT = 8888;
    private static final long FILE_SIZE_CHECK_VALUE = (long) Math.pow(1, 6);
	protected static final String DOWNLOAD_PATH = 
			System.getProperty("user.home") + File.separator + "P2P Downloads" + File.separator;

	/**
	 * Create the frame.
	 * @param userId 
	 */
	public Peer(int userId, Connection conn) {
		setTitle("Peer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 551);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Peer.userId = userId;
		Peer.conn = conn;
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setVgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.setBorder(new TitledBorder(null, "File Upload", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 70, 769, 70);
		contentPane.add(panel);
		
		txtFilePath = new JTextField();
		txtFilePath.setEditable(false);
		txtFilePath.setFont(new Font("Tahoma", Font.PLAIN, 17));
		txtFilePath.setPreferredSize(new Dimension(560, 25));
		panel.add(txtFilePath);
		
		JButton btnChooseFile = new JButton("Choose File");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File("user.home"));
                fc.setDialogTitle("Choose the file...");
                
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
			                directory.mkdirs();
			            }

						ProgressMonitor pm = new ProgressMonitor(Peer.this,
								fileName + " (" + readableFileSize(file.length()) + ")",
                                "Starting...", 0, 100);
						
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {							            
					            pm.setMillisToDecideToPopup(100);
					            pm.setMillisToPopup(100);
							}
						});
			            
			            final File fileToSave = file;			            
			        				            		        
				        new SwingWorker<Object, Object>() {

							@Override
							protected Object doInBackground() throws Exception {
								FileInputStream is;
								
								try {
									is = new FileInputStream(fileToSave);
									FileOutputStream os = new FileOutputStream(DOWNLOAD_PATH + fileName);
									
									byte[] buffer = new byte[1024];
							        int length;
									DecimalFormat df2 = new DecimalFormat("#.#");
							        int cycle = 0;
							        double progress;
							        									
									while ((length = is.read(buffer)) > 0) {
										
										os.write(buffer, 0, length);
							        	
							        	cycle++;
							        	
							        	progress = 100.0*cycle*1024 / fileToSave.length();
							        	
						                pm.setNote("Loading file: " + df2.format(progress) + " %");
						                pm.setProgress((int) progress);
									}
									
									// Make the file read only
							        File savedFile = new File(DOWNLOAD_PATH + fileName);
							        savedFile.setReadOnly();
							        
							        out.writeUTF("upload:" + userId + ":" + fileName + ":" + fileToSave.length() + ":" + fileType[0]);
							        is.close();
							        os.close();
							        
							        SwingUtilities.invokeLater(new Runnable() {
										public void run() {							            
								            pm.close();
									        txtFilePath.setText("");
										}
									});
							        
								} catch (IOException e) {
									e.printStackTrace();
								}
						        
								return null;
							}
				        }.execute();
				        
				        
				        file = null;
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
			            if (!directory.exists()){
			                directory.mkdir();
			            }
						
						if(file.exists())		    
							desktopOpen(file);
						else {		
							
							new SwingWorker<Object, Object>() {

								@Override
								protected Object doInBackground() throws Exception {
									
									String receivedFileName = fileName;
									int receivedFileId = fileId;
									
									DefaultTableModel model = (DefaultTableModel)tblFiles.getModel();
									final int currentRow = tblFiles.getSelectedRow();
									
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											model.setValueAt("Wating...", tblFiles.getSelectedRow(), 6);											
										}
									});
									
									long fileSize = 0;
									
									String query = "SELECT `size`"
											+ " FROM `files`" 
											+ " WHERE `id` = ?";
									
									try {
										pst = conn.prepareStatement(query);
										pst.setString(1, String.valueOf(receivedFileId));
										
										res = pst.executeQuery();
										
										if (res.next()){
											fileSize = res.getLong("size");
								        }
										
									} catch (SQLException e2) {
										e2.printStackTrace();
									}
									
									
									ServerSocket fileServerSocket;
									try {
										fileServerSocket = new ServerSocket(0);
										
										// File request sender
										try {
											out.writeUTF("request" + ":" + socket.getLocalAddress().getHostAddress() + ":" + fileServerSocket.getLocalPort() + ":" + fileId + ":" + userId);
											System.out.println("request" + ":" + socket.getLocalAddress().getHostAddress() + ":" + fileServerSocket.getLocalPort() + ":" + fileId + ":" + userId);
										} catch (IOException e) {
											e.printStackTrace();
										}
										
										Socket fileSocket = fileServerSocket.accept();
										
										int count;
										long cycle = 0;
										double progress;
										DecimalFormat df2 = new DecimalFormat("#.#");
									    byte[] buffer = new byte[2048];
									    
									    InputStream is = fileSocket.getInputStream();
									    
									    FileOutputStream fos = new FileOutputStream(DOWNLOAD_PATH + receivedFileName);
									    BufferedOutputStream bos = new BufferedOutputStream(fos);
									    while ((count = is.read(buffer)) > 0)
									    {
									    	bos.write(buffer, 0, count);								    		
									    							    	
									    	cycle++;			
									    	
									    	progress = 100*cycle / (fileSize/2048.0);
									    										    	
									    	if (progress < 99.9)
									    		model.setValueAt( df2.format(progress) + "%", currentRow, 6);
									    	else
									    		model.setValueAt( "Preparing..." , currentRow, 6);
									    }
									    
									    bos.close();
									    fileSocket.close();
									    fileServerSocket.close();
									    
									    File receivedFile = new File(DOWNLOAD_PATH + receivedFileName);
									    receivedFile.setReadOnly();
									    
									    if (receivedFile.length() == fileSize) {
									    	out.writeUTF("confirm" + ":" + receivedFileId + ":" + userId);
									    	
									    	SwingUtilities.invokeLater(new Runnable() {
												public void run() {
											    	model.setValueAt( "100.0%", currentRow, 6);
												}
											});;
									    } else {
									    	JOptionPane.showMessageDialog(null, "Failed to download " + receivedFileName + " please try again!");
									    	file.delete();
									    	
									    	SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													model.setValueAt( "0.0%", currentRow, 6);													
												}
											});
									    }
										
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									
									return null;
								}
							}.execute();	
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
				fileId = (int) model.getValueAt(tblFiles.getSelectedRow(), 0);
				fileName = (String) model.getValueAt(tblFiles.getSelectedRow(), 2);
				
				file = new File(DOWNLOAD_PATH + fileName); 
		
				if (file.exists())  			        
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
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					out.writeUTF("delete" + ":" + fileId + ":" + userId);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				file = new File(DOWNLOAD_PATH + fileName);

				if(file.delete()) { 
		            System.out.println("File deleted successfully"); 
		            btnDownload.setText("Download");
		            file = null;
		        } else { 
		            System.out.println("Failed to delete the file"); 
		        } 
			}
		});
		btnDelete.setBounds(556, 457, 89, 23);
		contentPane.add(btnDelete);
	
		initialize();
	}

	private static void initialize() {
		
		try { 
			
			Properties properties = new Properties();

			properties.load(Peer.class.getResourceAsStream("peer.properties"));
			
            socket = new Socket(properties.getProperty("DB_HOST"), SERVER_PORT);  
            lblPeer.setText(lblPeer.getText() + " - " + socket.getLocalSocketAddress());
            System.out.println("Connected");
            
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("assign" + ":" + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort() + ":" + userId);
            out.flush();   		
        } catch(UnknownHostException u) { 
            System.out.println(u); 
        } catch(IOException i) { 
            System.out.println(i); 
        }
		
		// Message Receiver Thread
		Thread messageReceiverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					
					String serverResponse = "";
					
					try {
						
						input = new DataInputStream(socket.getInputStream());
						out = new DataOutputStream(socket.getOutputStream());
				
						serverResponse = input.readUTF();
						String[] response = serverResponse.split(":");
						
						
						if(response[0].equals("update")) {
							updateTable();
						} else if (response[0].equals("send")) {
							
							Thread fileSenderThread = new Thread(new Runnable() {

								@Override
								public void run() {
									String query = "SELECT `name` FROM `files` WHERE `id` = ?";
									String fileToSave = "";
									String receiverIp = response[1];
									int receiverPort = Integer.valueOf(response[2]);
									String fileToSendId = response[3];
									
									try {
										pst = conn.prepareStatement(query);
										
										pst.setString(1, fileToSendId);
										res = pst.executeQuery();
										
										if (res.next()) {
											fileToSave = res.getString(1);
										}		
										
										File fileToSend = new File(DOWNLOAD_PATH + fileToSave);
										Socket fileSocket = new Socket(receiverIp, receiverPort);
										
										if (fileToSend.length() > FILE_SIZE_CHECK_VALUE) 
									    	out.writeUTF("set_busy" + ":1:" + fileToSendId + ":" + userId);
										
										int count;
										byte[] buffer = new byte[2048];
										
										InputStream is = new FileInputStream(fileToSend);
										OutputStream os = fileSocket.getOutputStream();
										
									    BufferedInputStream bis = new BufferedInputStream(is);
									    while ((count = bis.read(buffer)) > 0) {
									    	os.write(buffer, 0, count);				
									    }							    
									    
									    is.close();
									    bis.close();
									    fileSocket.close();
									    
									    if (fileToSend.length() > FILE_SIZE_CHECK_VALUE) 
									    	out.writeUTF("set_busy" + ":0:" + fileToSendId + ":" + userId);
									    										    
									} catch (SQLException | NumberFormatException | IOException e) {

										e.printStackTrace();
									}
								}
								
							});
							
							fileSenderThread.start();
							
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		});
		
		messageReceiverThread.start();
		
        updateTable();
	}
	
	private void desktopOpen(File file) throws IOException {
		
		if(!Desktop.isDesktopSupported()) { 
			JOptionPane.showMessageDialog(null, "Not Supported!");
			return;  
		}  
		
		Desktop desktop = Desktop.getDesktop();  
		
		if(file.exists())  			        
			desktop.open(file);
		
	}
	
	public static void updateTable() {
		try {			
									
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String sql = "SELECT `files`.`id` AS ID, ROW_NUMBER() OVER (ORDER BY `id` ASC) AS No,"
							+ " `files`.`name` as Name, pretty_size(`files`.`size`) as Size, `files`.`type` as Type,"
							+ " DATE_FORMAT(`files`.`added`, '%d/%c/%Y  %H:%i:%s') as Added, '0.0%' as Downloaded"
							+ " FROM `files`"
							+ " INNER JOIN `has_file` ON `files`.`id` = `has_file`.`fid`"
							+ " INNER JOIN `users` ON `users`.`id` = `has_file`.`uid`"
							+ " WHERE `users`.`active` = '1'"
							+ " GROUP BY `files`.`id` ORDER BY `id` DESC";
										
					try {
						pst = conn.prepareStatement(sql);
						res = pst.executeQuery();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					tblFiles.setModel(DbUtils.resultSetToTableModel(res));
					tblFiles.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
					tblFiles.removeColumn(tblFiles.getColumnModel().getColumn(0));
					
					tblFiles.getColumnModel().getColumn(0).setResizable(true);
					tblFiles.getColumnModel().getColumn(0).setPreferredWidth(5);;
					
					try {
						pst.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					for (int row = 0; row < tblFiles.getRowCount(); row++){
				        DefaultTableModel model = (DefaultTableModel)tblFiles.getModel();
				        String selected = model.getValueAt(row, 2).toString();
				        
				        File checkFile = new File(DOWNLOAD_PATH + selected);
				        
				        if (checkFile.exists())
				        	model.setValueAt("100.0%", row, 6);
				    }
					
					TableColumnModel columnModel = tblFiles.getColumnModel();
				    TableColumn column = columnModel.getColumn(5); 
				    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
				    renderer.setHorizontalAlignment(JLabel.RIGHT);
				    column.setCellRenderer(renderer);
				}
			});			
			
			
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
