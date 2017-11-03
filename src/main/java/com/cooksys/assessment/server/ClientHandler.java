package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.cooksys.assessment.server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class); 

	private Socket socket;
	private String clientIP;
	private String userName;
	
	private BufferedReader reader;
	private PrintWriter writer;
	private ObjectMapper mapper;
	
	private ArrayList<Message> messageHistory;

	public ClientHandler(Socket socket, String clientIP) throws IOException {
		super();
		this.socket = socket;
		this.clientIP = clientIP;
		
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public void run() {
		try {
			mapper = new ObjectMapper();
			messageHistory = new ArrayList<Message>();

			while (!socket.isClosed()) {
				String raw = reader.readLine();
                Message message = mapper.readValue(raw, Message.class);
                
                userName = message.getUsername();
 
				if (message.getCommand().equals("connect")) {
					log.info("user <{}> connected", message.getUsername());
					message.setCommand("connection alert");
					message.setContents("has connected");
					broadcast(message);
				} else if(message.getCommand().equals("disconnect")) {
					log.info("user <{}> disconnected", message.getUsername());
					message.setCommand("connection alert");
					message.setContents("has disconnected");
					broadcast(message);
					ArrayList<ClientHandler> clientList = Server.getClients();
					clientList.remove(this);
					socket.close();
				} else if (message.getCommand().equals("echo")) {
					log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
					message.setTime(time());
					message.setType("(echo):");
					String response = mapper.writeValueAsString(message);
					writer.write(response);
					writer.flush();
					messageHistory.add(message);
				} else if (message.getCommand().equals("broadcast")) {
					log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
					broadcast(message);
					messageHistory.add(message);
				} else if (message.getCommand().equals("users")) {
					log.info("user <{}> requested users", message.getUsername());
					getUsers(message);
				} else if (isUserOn(message)) {
					log.info("user <{}> whispered message <{}> to recipient <{}>", message.getUsername(), message.getContents(), message.getCommand());
                	privateMessage(message);
                	messageHistory.add(message);
				} else if ( !messageHistory.isEmpty() 
						&& ( messageHistory.get(messageHistory.size() - 1).getType().equals("(whisper):") 
								|| messageHistory.get(messageHistory.size() - 1).getType().equals("(all):")
								|| messageHistory.get(messageHistory.size() - 1).getType().equals("(echo):"))) {
					message.setContents(message.getCommand() + " " + message.getContents());
					message.setCommand(messageHistory.get(messageHistory.size() - 1).getCommand());
					message.setType(messageHistory.get(messageHistory.size() - 1).getType());
					message.setTime(time());
					switch (messageHistory.get(messageHistory.size() - 1).getType()) {
					    case "(whisper):":
					    	privateMessage(message);
					    	break;
					    case "(all):":
					    	broadcast(message);
					    	messageHistory.add(message);
					    	break;
					    case "(echo):":
					    	String response = mapper.writeValueAsString(message);
							writer.write(response);
							writer.flush();
							messageHistory.add(message);
							break;
					}
                } else {
                    message.setTime(time());
                    message.setType(" was not recognized");
                    message.setContents("");
                    message.setUsername(message.getCommand());
                    String response = mapper.writeValueAsString(message);
                    writer.write(response);
                    writer.flush();
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public void getUsers(Message sendMessage) throws JsonProcessingException {
		String userList = "";
		ArrayList<ClientHandler> clientList = Server.getClients();
		for (int i = 0; i < clientList.size(); i++) {
			ClientHandler users = (ClientHandler)clientList.get(i);
			userList = userList + users.userName + " ";
		}
		sendMessage.setContents(userList);
		post(sendMessage);
	}
	
	public void broadcast(Message sendMessage) throws JsonProcessingException {
		ArrayList<ClientHandler> clientList = Server.getClients();
		for (int i = 0; i < clientList.size(); i++) {
			ClientHandler users = (ClientHandler)clientList.get(i);
			users.post(sendMessage);
		}
	}
	
	public void privateMessage(Message sendMessage) throws JsonProcessingException {
		ArrayList<ClientHandler> clientList = Server.getClients();
		boolean found = false;
		for (int i = 0; i < clientList.size(); i++) {
			ClientHandler users = (ClientHandler)clientList.get(i);
			if (users.userName.equals(sendMessage.getCommand())) {
				sendMessage.setType("(whisper):");
				users.post(sendMessage);
				found = true;
			}
		}
		if (!found) {
			sendMessage.setTime(time());
        	sendMessage.setType(" Could not find user, " + sendMessage.getCommand());
        	sendMessage.setContents("");
        	sendMessage.setUsername("");
        	String response = mapper.writeValueAsString(sendMessage);
        	writer.write(response);
        	writer.flush();
		}
	}
	
	public void post(Message sendMessage) throws JsonProcessingException {
		if (sendMessage.getCommand().equals("broadcast")) {
			sendMessage.setTime(time());
			sendMessage.setType("(all):");
			String response = mapper.writeValueAsString(sendMessage);
		    writer.write(response);
		    writer.flush();
		} else if (sendMessage.getCommand().equals("users")){
			sendMessage.setTime(time());
			sendMessage.setType("currently connected users:");
			sendMessage.setUsername("");
			String response = mapper.writeValueAsString(sendMessage);
		    writer.write(response);
		    writer.flush();
		} else if (sendMessage.getCommand().equals("connection alert")) {
			sendMessage.setType("");
			sendMessage.setTime(time());
			String response = mapper.writeValueAsString(sendMessage);
		    writer.write(response);
		    writer.flush();
		} else if (sendMessage.getType().equals("(whisper):")) {
			String temp = sendMessage.getCommand();
			sendMessage.setTime(time());
			sendMessage.setCommand("direct message");
			String response = mapper.writeValueAsString(sendMessage);
			writer.write(response);
			writer.flush();
			sendMessage.setCommand(temp);
			messageHistory.add(sendMessage);
		}
	}
	
	public boolean isUserOn(Message sendMessage) {
		ArrayList<ClientHandler> clientList = Server.getClients();
		boolean found = false;
		if (clientList.size() == 1) {
			return false;
		}
		for (int i = 0; i < clientList.size(); i++) {
			ClientHandler users = (ClientHandler)clientList.get(i);
			if (users.userName.equals(sendMessage.getCommand())) {
				found = true;
			}
		}
		return found;
	}
	
	public String time() {
		Calendar timeStamp = Calendar.getInstance();
		return timeStamp.get(Calendar.HOUR_OF_DAY) + ":" 
				+ timeStamp.get(Calendar.MINUTE) + ":" 
		        + timeStamp.get(Calendar.SECOND);
	}
	

}
