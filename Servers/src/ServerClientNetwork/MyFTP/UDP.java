package ServerClientNetwork.MyFTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.NoSuchAlgorithmException;

public class UDP
{
	private static DatagramSocket udpSocket;
	private static int port;
	private static int packetsize = 60000;
	private static int timeout = 10000;

	/*
	 * This method constructs a UDP object and assigns the port value.
	 */
	public UDP(int p) throws SocketException
	{
		port = p;
	}

	/*
	 * This method sends a file via UDP. It receives a filename to send and an
	 * IP address for the destination. The first packet send is always the
	 * checksum of the file. After which, it breaks the file into a
	 * predetermined packet size and encapsulates those packets in the
	 * DatagramPacket object and sends them via UDP.
	 */
	public static void send(String filename, InetAddress address)
			throws IOException, NoSuchAlgorithmException
	{
		udpSocket = new DatagramSocket();
		File file = new File(filename);
		DataInputStream diStream = new DataInputStream(new FileInputStream(file));
		

		long filelen = file.length();
		long current = 0;

		new ChecksumGen();
		String checksum = "CHECKSUM: " + ChecksumGen.getChecksum(filename);
		udpSocket.send(new DatagramPacket(checksum.getBytes(),checksum.getBytes().length, InetAddress.getByName("localhost"), port));

		while (current != filelen)
		{
			int size = packetsize;
			if (filelen - current >= size)
				current += size;
			else
			{
				size = (int) (filelen - current);
				current = filelen;
			}
			byte[] packet = new byte[size];
			diStream.read(packet, 0, size);
			DatagramPacket p = new DatagramPacket(packet, size, InetAddress.getByName("localhost"), port);
			udpSocket.send(p);
			System.out.println("sent packet");
		}
		System.out.println(filename + " sent");
		diStream.close();
	}

	/*
	 * This method receives a file via UDP. It receives a filename for it to
	 * write into. We have set a timeout, so that we can check the file after we
	 * have stopped receiving packets for a while.
	 * 
	 * While this timeout is not reached, the DatagramSocket keeps listening to
	 * receive packets. If any of those packets have a flag that says "checksum"
	 * then it does not write that to the file and instead saves that as the
	 * checksum of the file being sent.
	 * 
	 * Once it has stopped receiving packets for a while, it will stop listening
	 * and generate a checksum for received file. If the new generated checksum
	 * is equal to the checksum it was sent, then it will say so, otherwise it
	 * will let the user know that the checksum does not match.
	 */
	public static void receive(String filename)
			throws IOException, NoSuchAlgorithmException
	{
		String checksum = null;
		udpSocket = new DatagramSocket(port);
		FileOutputStream fout = new FileOutputStream("filetest/"+filename);

		udpSocket.setSoTimeout(timeout);
		byte[] buffer = new byte[packetsize];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (true)
		{
			try
			{
				udpSocket.receive(packet);
				System.out.println("received packet");
				String s = new String(packet.getData());

				if (s.startsWith("CHECKSUM:"))
					checksum = s;
				else
					fout.write(packet.getData(), 0, packet.getData().length);
			} catch (SocketTimeoutException e)
			{
				new ChecksumGen();
				String newchecksum = "CHECKSUM: "
						+ ChecksumGen.getChecksum("filetest/"+filename);

				if (checksum.equals(newchecksum))
				{
					System.out.println("Checksum Matches");
					fout.flush();
					fout.close();
					return;
				}
				else
				{
					System.out.println("Checksum Doesn't Match");
					fout.flush();
					fout.close();
					return;
				}
			}
		}

	}

}
