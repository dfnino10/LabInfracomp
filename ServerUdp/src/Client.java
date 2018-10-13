
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;

public class Client {
	private Socket socket = null;
	private UDP udp;

	private ObjectOutputStream outputStream = null;
	private ObjectInputStream inputStream = null;
	private boolean isConnected = false;

	File dstFile = null;
	FileDescription fileDesc=null;
	private String destinationPath = "archivos";

	private String messageServer ="";
	private String messageClient ="";

	public Client() {
		try {
			udp=new UDP();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Connect with server code running in local host or in any other host
	 */
	public void connect() {
		while (!isConnected) {
			try {
				socket = new Socket("localhost", 3010);
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

		// =============== recepcion de descripcion del archivo =============
		fileDesc = (FileDescription) inputStream.readObject();
		System.out.println("Descripcion archivo recibida");
		
		if (fileDesc.getStatus().equalsIgnoreCase("Error")) {
			System.out.println("Error occurred ..So exiting");
			System.exit(0);
		}
		// =============== recepcion de archivo =============
		String outputFile = destinationPath +"/"+ fileDesc.getFilename();
		if (!new File(destinationPath).exists()) {
			new File(destinationPath).mkdirs();
		}
		dstFile = new File(outputFile);
		fileOutputStream = new FileOutputStream(dstFile);
		byte[] fileBytes=null;
		for (int i = 0; i < fileDesc.getNumPaquetes(); i++) {
			// =============== recepcion de paquete =============
			try {
				fileBytes = udp.receiveFile(3030);
			} catch (Exception e) {
				break;
			}
			// =============== escritura en el archivo =============
			fileOutputStream.write(fileBytes);
			System.out.println("paquete "+i+" recibido");
			System.gc();
		}
		endTime = System.currentTimeMillis();
		
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
			System.out.println("Integridad: OK");
			return true;
		}
		else 
		{
			System.out.println("Integridad: ERROR");
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
					// ========== envio de informacion log ========================
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
