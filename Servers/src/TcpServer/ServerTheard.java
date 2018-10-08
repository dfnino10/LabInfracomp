package TcpServer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerTheard extends Thread {
	
	public final String sourceFilePath250 = "250mb.mp4";
	public final String sourceFilePath500 = "500mb.dat";

	private Socket socket = null;
	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;

	private int userId;
	private int idSession;

	String sourceFilePath = "";
	private String messageServer ="";
	private String messageClient ="";
	
	File log;
	
	boolean end = false;

	public ServerTheard(Socket socket , int pIdSession) {
		try {
			idSession=pIdSession;
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void setSourceFilePath(String pSourceFilePath ) {
		if(pSourceFilePath.equalsIgnoreCase("250mb"))
			sourceFilePath = sourceFilePath250;
		else if(pSourceFilePath.equalsIgnoreCase("500mb"))
			sourceFilePath = sourceFilePath250;
	}
	public void run() {
		try {			
			if(!sourceFilePath.equals("")) {
				clientReady();
				sendFileProtocol();
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initProtocol() throws Exception{

		// ========== Cliente saluda ==========
		messageClient= (String) inputStream.readObject();
		log("Client: "+messageClient);
		// ========== Servidor saluda ==========
		messageServer="HOLA:OK";
		outputStream.writeObject(messageServer);

	}
	public void clientReady() throws Exception {

		// ========== Servidor saluda ==========
		messageServer="SENDPETITION";
		outputStream.writeObject(messageServer);
		log("Peticion de envio al cliente");
		// ========== Cliente listo para recibir ==========
		messageClient= (String) inputStream.readObject();
		log("Client: "+messageClient);

	}

	public void sendFileProtocol() throws Exception{

			// ========== Servidor inicia envio ==========
			messageServer="START";
			outputStream.writeObject(messageServer);
			log("Inicio de envio");

			long startTime = sendFile();

			// ========== Cliente recibe completo ==========
			messageClient= (String) inputStream.readObject();
			if(messageClient.split(":")[0].equals("RECIBED")){
				log("Client: Archivo recibido");
			}else{
				
			}
			
			long endTime = Long.parseLong(messageClient.split(":")[1]);
			log("Tiempo de transferencia: "+(endTime-startTime));
			//=========== info log =========================
			messageClient= (String) inputStream.readObject();
			if(messageClient.split(":")[1].equals("OK")){
				log("Client: Integridad del Archivo verificada (Archivo completo)");
			}else{
				
			}
			log("==================== fin de envio ===================");
			

	}
	public void endProtocol() throws Exception{
		// ========== Servidor inicia envio ==========
		messageServer="END";
		outputStream.writeObject(messageServer);
		log("Server Thread finalizado");
	}
	/**
	 * Sending FileEvent object.
	 */
	private long sendFile() throws Exception {
		long startTime = 0;
		
		FileDescription fileDesc = new FileDescription();
		byte[] fileBytes = null;

		String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
		String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf("/") + 1);

		fileDesc.setFilename(fileName);
		fileDesc.setSourceDirectory(sourceFilePath);


		File file = new File(sourceFilePath);
		if (file.isFile()) {
			try {
				// ============= archivo a byte array =====================
				DataInputStream diStream = new DataInputStream(new FileInputStream(file));
				long len = (long) file.length();
				fileBytes = new byte[(int) len];
				int read = 0;
				int numRead = 0;
				while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
					read = read + numRead;
				}
				fileDesc.setNumPaquetes(1);
				fileDesc.setFileSize(len);
				
				// ================ hash de integridad ======================
				MessageDigest md5Digest = MessageDigest.getInstance("MD5");
				
				//Get the checksum
				FileChecksum fc = new FileChecksum();
				String checksum = fc.getFileChecksum(md5Digest, file);
				fileDesc.setHash(checksum);
			
				//============================================================	
				fileDesc.setStatus("Success");

			} catch (Exception e) {
				e.printStackTrace();
				fileDesc.setStatus("Error");
			}
		} 
		else {

			log("Ruta del archivo no encontrada");
			fileDesc.setStatus("Error");
		}
		//Envio del archivo y descripcion por el sockets 

		// ================= envio de archivos ======================
		outputStream.writeObject(fileDesc);
		
		startTime = System.currentTimeMillis();
		outputStream.writeObject(fileBytes);
		Date date = new Date();
		DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
		log("Fecha y hora de envio: "+hourdateFormat.format(date));
		log("Archivo enviado: "+fileDesc );
		return startTime;
	}
	
	public void log(String text)
	{
		try {
			log = new File("logTcp/server" + (idSession+1));
			PrintWriter writer = new PrintWriter(new FileWriter(log,true));
			writer.println(text);
			writer.close();
			consoleLog(text+" (log)");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void consoleLog(String text)
	{
		System.out.println("Server Thread "+ (idSession+1)+ ": "+ text);
	}

}
