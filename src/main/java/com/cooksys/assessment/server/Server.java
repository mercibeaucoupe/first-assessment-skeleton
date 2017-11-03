package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private static ArrayList<ClientHandler> clientList;
	private int port;
	private ExecutorService executor;
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("server started");
		clientList = new ArrayList<ClientHandler>();
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				String hostName = socket.getInetAddress().getHostName();
				System.out.println(this.port);
				System.out.println(hostName);
				ClientHandler handler = new ClientHandler(socket, hostName);

				synchronized (clientList) {
					clientList.add(handler);
				}
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public static ArrayList<ClientHandler> getClients() {
		return clientList;
	}

}
