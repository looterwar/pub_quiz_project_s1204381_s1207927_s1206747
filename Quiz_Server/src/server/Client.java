package server;

import io.IO;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import packet.Packet;
import packet.PacketHeaders;

public class Client implements Runnable {

	private Socket clientSocket;
	private ObjectOutputStream toClient;
	private ObjectInputStream fromClient;
	
	public Client(Socket client) {
		this.clientSocket = client;
	}

	@Override
	public void run() {

		listen();
		
	}
	
	public void listen() {
		for (;;) {
			
			try {
				
				Packet receivedPacket;
				fromClient = new ObjectInputStream(clientSocket.getInputStream());
				receivedPacket = (Packet) fromClient.readObject();
				if (receivedPacket.getHeader() == PacketHeaders.unknown) {
					IO.println("Client: " + receivedPacket.getClientId() + "\nError unknown data");
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
				IO.println("Error Client Disconnected!");
				break;
			}
		}
	}
}
