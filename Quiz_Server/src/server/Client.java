package server;

import io.IO;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import packet.Packet;
import packet.PacketHeaders;
import quiz.Game;

/**
 * Client
 * @author Joshua Preece
 * @version 1.0
 */

public class Client implements Runnable {

	private Socket clientSocket;
	private DataInputStream fromClient;
	private Runnable sender;
	
	private ExecutorService exe = Executors.newSingleThreadExecutor();
	
	private final int PACKET_SIZE = 2024;
	
	private Game game;
	
	private int clientID = 0;
	private String teamName = "";
	
	/**
	 * Creates a new client
	 * @param client Client socket to use to communicate.
	 * @param newGame Game handle to communicate with the Game thread
	 */
	public Client(Socket client, Game newGame, int id) {
		this.clientSocket = client;
		this.game = newGame;
		this.clientID = id;
	}

	@Override
	public void run() {
		
		sendClientID();
		listen();
		
	}
	
	/**
	 * Main listen thread each client will have their own listener on
	 * separate threads. This method handles the receiving of data from the client
	 * that this listener is assigned to.
	 */
	public void listen() {
		for (;;) {
			if (!Thread.currentThread().isInterrupted()) {
				try {
					
					byte[] receivedPacket = new byte[PACKET_SIZE];
					fromClient = new DataInputStream(clientSocket.getInputStream());
					fromClient.read(receivedPacket);
					Packet packet = new Packet(receivedPacket);
					process_data(packet);
					
				} catch (Exception ex) {
					IO.println("Client Disconnected!");
					break;
				}
			} else {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Processes the data received, and acts on the data
	 * @param packet Packet in which to process
	 */
	public void process_data(Packet packet) {
		if (packet.getHeader() == PacketHeaders.unknown.ordinal()) {
			IO.println("Error: client " + packet.getID() + " : has sent unknown data");
		} else if (packet.getHeader() == PacketHeaders.questions.ordinal()) {
			IO.println("Error: client " + packet.getID() + " : sent questions!"); 
		} else if (packet.getHeader() == PacketHeaders.results.ordinal()) {
			IO.println("Client : " + packet.getID() + " : has returned their results!");
			game.addResult(packet.returnDataArray());
		} else if (packet.getHeader() == PacketHeaders.team.ordinal()) {
			IO.println("Client " + packet.getID() + " : Name : " + packet.getData());
			teamName = packet.getData();
		} else if (packet.getHeader() == PacketHeaders.id.ordinal()) {
			IO.println("Warning: client " + packet.getID() + " : sent id!");
		} else if (packet.getHeader() == PacketHeaders.disconnect.ordinal()) {
			IO.println("Client " + packet.getID() + " : has disconnected!\nReason for disconnect : " + packet.getData());
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Starts a new sender thread which sends data to this threads client
	 * @param send Data Packet to send
	 */
	public void sendPacket(Packet send) {
		try {
			sender = new Sender(clientSocket, send);
			exe.execute(sender);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Initiates a send command to send the client id generated by this server
	 * to the client connected to this listener.
	 */
	public void sendClientID() {
		try {
			Packet packet = new Packet(1, PacketHeaders.id, Integer.toString(clientID));
			sender = new Sender(clientSocket, packet);
			exe.execute(sender);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Gets a team name
	 * @return string team name
	 */
	public String get_TeamName() {
		return teamName;
	}
	
	/**
	 * Gest the clients id
	 * @return int client id
	 */
	public int get_ClientID() {
		return clientID;
	}
}
