package TcpServer;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;

public class Client {
	private Socket socket = null;

	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;
	private boolean isConnected = false;

	File dstFile = null;
	FileDescription fileDesc=null;
	private String destinationPath = "D:/FFOutput/download";

	private String messageServer ="";
	private String messageClient ="";

	public Client() {

	}

	/**
	 * Connect with server code running in local host or in any other host
	 */
	public void connect() {
		while (!isConnected) {
			try {
				socket = new Socket("localHost", 3010);
				outputStream = new ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
				isConnected = true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Reading the FileEvent object and copying the file to disk.
	 */
	public long downloadFile() throws Exception {


		FileOutputStream fileOutputStream = null;

		long endTime =0 ;

		// =============== recepcion de archivo =============
		fileDesc = (FileDescription) inputStream.readObject();
		System.out.println("Descripcion archivo recibida");
		byte[][] fileBytes = new byte[fileDesc.getNumPaquetes()][1];
		for (int i = 0; i < fileDesc.getNumPaquetes(); i++) {
			fileBytes[i] = (byte[]) inputStream.readObject();
		}

		endTime = System.currentTimeMillis();


		if (fileDesc.getStatus().equalsIgnoreCase("Error")) {
			System.out.println("Error occurred ..So exiting");
			System.exit(0);
		}
		// ================ nombre del archivo ==============
		String outputFile = destinationPath +"/"+ fileDesc.getFilename();
		if (!new File(destinationPath).exists()) {
			new File(destinationPath).mkdirs();
		}
		dstFile = new File(outputFile);
		fileOutputStream = new FileOutputStream(dstFile);
		for (int i = 0; i < fileDesc.getNumPaquetes(); i++) {
			fileOutputStream.write(fileBytes[i]);
		}
		
		fileOutputStream.flush();
		fileOutputStream.close();
		System.out.println("Archivo : " + outputFile + " Recibido ");


		return endTime;
	}
	private boolean verifyIntegrity() throws Exception {

		// ======================= integridad del archivo ===============
		MessageDigest md5Digest = MessageDigest.getInstance("MD5");

		//Get the checksum
		FileChecksum fc = new FileChecksum();
		String checksum = fc.getFileChecksum(md5Digest, dstFile);
		//see checksum
		System.out.println("Hash para verificar integridad:"+checksum);
		String hashServidor= fileDesc.getHash();
		if (hashServidor.compareTo(checksum)==0)
		{
			System.out.println("El archivo enviado por el servidor no ha sufrido problemas de integridad");
			return true;
		}
		else 
		{
			System.out.println("Integridad vulnerada:El archivo enviado por el servidor ha sufrido problemas de integridad");
			return false;
		}

	}

	public void initProtocol(){
		try{

			// ========== Cliente saluda ==========
			messageClient="hola";
			outputStream.writeObject(messageClient);
			// ========== Servidor saluda ==========
			messageServer= (String) inputStream.readObject();
			System.out.println("Server: "+messageServer);
			if(!messageServer.split(":")[1].equals("OK")){
				throw new Exception("Error en el Servidor");
			}
			while(true) {
				// ========== Peticion envio ==========
				messageServer= (String) inputStream.readObject();
				System.out.println("Server: "+messageServer);
				if(messageServer.split(":")[0].equals("END")){
					System.out.println("Cliente finalizado");
					break;
				}
				// ========== Cliente listo para recibir ==========
				messageClient="READY";
				outputStream.writeObject(messageClient);
				// ========== Servidor inicia envio ==========
				messageServer= (String) inputStream.readObject();
				System.out.println("Server: "+messageServer);

				if(messageServer.split(":")[0].equals("START")){
					long endTime = downloadFile();
					System.out.println("Archivo Recibido");
					// ========== Cliente recibe completo ==========
					messageClient="RECIBED:"+endTime;
					outputStream.writeObject(messageClient);
					// ========== verificar integridad =============
					boolean integrity = verifyIntegrity();
					// ========== info log ========================
					if(integrity) {
						messageClient="LOG:OK";
						outputStream.writeObject(messageClient);
					}
					else {
						messageClient="LOG:ERROR:#:#";
						outputStream.writeObject(messageClient);
					}
				}
			}


		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		System.out.println("Cliente iniciado");
		client.connect();
		System.out.println("Conectado al servidor");
		client.initProtocol();
	}
}
