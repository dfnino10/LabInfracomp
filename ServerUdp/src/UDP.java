

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
public class UDP {

	private DatagramSocket socket = null;
	private int timeout = 10000;
	byte[] data;
	public UDP(){

	}
	public void sendFile(byte[] file, InetAddress address, int port) throws Exception
	{
		socket = new DatagramSocket();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);

		os.writeObject(file);

		data = outputStream.toByteArray();

		DatagramPacket sendPacket = new DatagramPacket(outputStream.toByteArray(), data.length, address, port);

		socket.send(sendPacket);

	}
	public byte[] receiveFile(int port) throws Exception
	{
		socket = new DatagramSocket(port);
		socket.setSoTimeout(timeout);

		try
		{
			byte[] incomingData = new byte[1024*1000*500];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);
			System.out.println("Paquete recibido");
			byte[] data = incomingPacket.getData();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			return (byte[]) is.readObject();

		}catch (SocketTimeoutException e)
		{
			System.out.println("paquete no recibido");
			throw new Exception("paquete no recibido");
		}
	}
}
