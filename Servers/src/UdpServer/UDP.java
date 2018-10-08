package UdpServer;

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

import ServerClientNetwork.MyFTP.ChecksumGen;

public class UDP {

	private DatagramSocket socket = null;
	public final int packetsize = 10000;
	private int timeout = 10000;
	public UDP(){

	}
	public void sendFile(byte[] file, InetAddress address, int port) throws Exception
	{
		long filelen = file.length;
		long current = 0;

		socket = new DatagramSocket();
		while (current < filelen)
		{
			int size = packetsize;
			if (filelen - current < size){
				size = (int) (filelen - current);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);

			os.writeObject(Arrays.copyOfRange(file,(int) current,(int) (current+size)));

			byte[] data = outputStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
			Thread.sleep(100);
			socket.send(sendPacket);
			System.out.println("Paquete enviado");
			current+= size;
		}



	}
	public ArrayList<byte[]> receiveFile(int port,int chunks) throws Exception
	{
		socket = new DatagramSocket(port);
		socket.setSoTimeout(timeout);
		ArrayList<byte[]> paquetes = new ArrayList<byte[]>();
		int i = 0; 
		while (i<chunks)
		{
			try
			{
				byte[] incomingData = new byte[1024*1000*500];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);
				System.out.println("Paquete recibido");
				byte[] data = incomingPacket.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream is = new ObjectInputStream(in);
				paquetes.add( (byte[]) is.readObject() );
				i++;
			}catch (SocketTimeoutException e)
			{
				System.out.println("Archivo incompleto");
				return paquetes;
			}
		}
		return paquetes;
	}
}
