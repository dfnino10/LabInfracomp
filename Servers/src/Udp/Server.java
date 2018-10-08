package Udp;

import java.io.*;
import java.net.*;



public class Server {
	private DatagramSocket socket = null;
	private FileEvent fileEvent = null;

	public Server() {
	}

	public void createAndListenSocket() {
		try {
			socket = new DatagramSocket(9876);
			byte[] incomingData = new byte[1024*1000*500];
			while (true) {
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);
				byte[] data = incomingPacket.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream is = new ObjectInputStream(in);
				fileEvent = (FileEvent) is.readObject();
				if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
					System.out.println("Upss , algo fallo ");
					System.exit(0);
				}
				createAndWriteFile(); // writing the file to hard disk
				
				InetAddress IPAddress = incomingPacket.getAddress();
				int port = incomingPacket.getPort();
				String reply = "Thank you for the message";
				byte[] replyBytea = reply.getBytes();
				DatagramPacket replyPacket =
						new DatagramPacket(replyBytea, replyBytea.length, IPAddress, port);
				socket.send(replyPacket);
				Thread.sleep(3000);
				System.exit(0);

			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void createAndWriteFile() {
		String outputFile = fileEvent.getDestinationDirectory() +"/"+ fileEvent.getFilename();
		if (!new File(fileEvent.getDestinationDirectory()).exists()) {
			new File(fileEvent.getDestinationDirectory()).mkdirs();
		}
		File dstFile = new File(outputFile);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(dstFile);
			fileOutputStream.write(fileEvent.getFileData());
			fileOutputStream.flush();
			fileOutputStream.close();
			System.out.println("Output file : " + outputFile + " el archivo ha sido guardado exitosamente :) ");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Server server = new Server();
		server.createAndListenSocket();
	}
}
