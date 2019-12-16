package com.gg.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class PeerHandler extends Thread {
	private Server server = null;
	private Socket socket = null;
    private int id = -1;
    private DataInputStream input = null;
    private DataOutputStream output = null;

    public PeerHandler(Server server, Socket socket) {
    	this.server = server;
        this.socket = socket;
        this.id = socket.getPort();
    }
    
    public int getID() {  
    	return id;
    }
    
    public String getServer() {
    	return socket.getInetAddress().getHostAddress();
    }
    
    public void open() throws IOException { 

    	input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
    }
    
    public void send(String msg) {   
    	try {
    		output.writeUTF(msg);
    		output.flush();
        } catch(IOException ioe) {  
        	System.out.println(id + " ERROR sending: " + ioe.getMessage());
        	server.remove(id);
        	interrupt();
        }
    }

    public void run() {
    	System.out.println("Server Thread " + id + " running.");
        while (!socket.isClosed()) {  
        	try {
    			server.handle(input.readUTF());
            } catch(IOException ioe) {  
            	System.out.println(id + " ERROR reading: " + ioe.getMessage());
            	server.remove(id);
            	try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
           }
        }
    }
    
    public void close() throws IOException {  
    	if (socket != null)    
    		socket.close();
        if (output != null)  
        	output.close();
        if (input != null)
        	input.close();
    }
}
