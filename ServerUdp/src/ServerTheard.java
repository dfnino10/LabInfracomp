
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerTheard extends Thread {

	public final int packetsize = 1000000;

	private Socket socket = null;
	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;

	private int idSession;
	
	private InetAddress clientAddress;
	private UDP udp;

	String sourcePath="";

	private String messageServer ="";
	private String messageClient ="";

	File log;

	boolean end = false;

	public ServerTheard(Socket pSocket , int pIdSession) {
		try {
			socket= pSocket;
			udp = new UDP();
			idSession=pIdSession;
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		try {			
			if(!sourcePath.equals("")) {
				clientReady();
				sendFileProtocol();

			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}
	public void setSourcePath(String pPath){
		sourcePath= pPath;
	}
	public void initProtocol() throws Exception{

		// ========== Cliente saluda ==========
		messageClient= (String) inputStream.readObject();
		log("Client: "+messageClient);
		clientAddress = socket.getInetAddress();
		log("Client IP: "+ clientAddress.getHostAddress());
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
		// =============== generacion y envio de descripcion ====================
		generateFileDescription(sourcePath);
		// =============== lectura y envio del archivo ====================
		long startTime = sendFile(sourcePath);;

		// ========== Cliente recibe completo ==========
		messageClient= (String) inputStream.readObject();
		if(messageClient.split(":")[0].equals("RECIBED")){
			log("Client: Archivo recibido");
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

		sourcePath = "";

	}
	public void endProtocol() throws Exception{
		// ========== Servidor finaliza  ==========
		messageServer="END";
		outputStream.writeObject(messageServer);
		log("Server Thread finalizado");
	}
	private void generateFileDescription(String pPath)throws Exception{
		FileDescription fileDesc = new FileDescription();
		String fileName = pPath.substring(pPath.lastIndexOf("/") + 1, pPath.length());
		
		fileDesc.setFilename(fileName);
		fileDesc.setSourceDirectory(pPath);
		File file = new File(pPath);
		if (file.isFile()) {
			try{
				long len = (long) file.length();
				int chunks = (int) Math.ceil(((double)len/(double) packetsize));
				fileDesc.setNumPaquetes(chunks);
				fileDesc.setFileSize(len);
				// ================ hash de integridad ======================
				MessageDigest md5Digest = MessageDigest.getInstance("MD5");

				FileChecksum fc = new FileChecksum();
				String checksum = fc.getFileChecksum(md5Digest, file);
				fileDesc.setHash(checksum);

				//============================================================	
				fileDesc.setStatus("Success");
			}catch (Exception e) {
				e.printStackTrace();
				fileDesc.setStatus("Error");
			}
		}
		else {
			System.out.println("Ruta del archivo no encontrada");
			fileDesc.setStatus("Error");
		}
		// =============== Envia la descripcion del archivo ====================
		outputStream.writeObject(fileDesc);
		log("Descripcion del Archivo: "+fileDesc );
	}
	private long sendFile(String pPath) throws Exception{

		// ================= envio de archivos ======================
		long startTime = 0;

		File file = new File(pPath);
		if (file.isFile()) {

			
			DataInputStream diStream = new DataInputStream(new FileInputStream(file));
			long len = (long) file.length();
			
			Date date = new Date();
			DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
			log("Fecha y hora de envio: "+hourdateFormat.format(date));
			startTime = System.currentTimeMillis();
			
			long current = 0;
			int read = 0;
			byte[] fileBytes= new byte[packetsize];
			while (current < len)
			{	
				int size = packetsize;
				if (len - current < size){
					size = (int) (len - current);
				}
				if(size!=packetsize){
					fileBytes=new byte[size];
				}
				System.gc();
				int numRead = 0;
				// ================= lectura de chunk ======================
				numRead = diStream.read(fileBytes, 0, size);
				// ================= envio de chunk ======================
				Thread.sleep(50);
				udp.sendFile(fileBytes, clientAddress, 3030);
				
				
				read += numRead;

				current+= size;
			}
			diStream.close();

		} 
		else {
			System.out.println("Ruta del archivo no encontrada");
			
		}
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
