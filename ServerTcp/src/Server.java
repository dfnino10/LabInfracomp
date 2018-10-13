
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;


public class Server {
	private final String sourceFilePath250 = "250mb.dat";
	private final String sourceFilePath500 = "500mb.dat";
	
	private InputStreamReader input= null;
	private BufferedReader br= null;
	private String inputLine = "";
	private ArrayList<ServerTheard> threads = null;

	private ServerSocket serverSocket = null;

	private byte[][] fileBytes;
	private FileDescription fileDesc;
	

	public Server() {
		try {
			input= new InputStreamReader(System.in);
			br= new BufferedReader(input);
			serverSocket = new ServerSocket(3010);
			threads = new ArrayList<ServerTheard>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	private void init() {
		try {
			// =============== inicia el servidor ====================
			System.out.println("Servidor Iniciado");
			int idSession = 0;
			// =============== especifica numero de clientes ====================
			System.out.println("Numero de clientes?");
			inputLine = br.readLine();
			int numClients = Integer.parseInt(inputLine);
			// =============== acepta a todos los clientes ====================
			while(idSession < numClients) {
				System.out.println("Esperando clientes...");
				Socket socket = serverSocket.accept();
				System.out.println("Cliente Conectado" + (idSession+1));

				ServerTheard serverT = new ServerTheard(socket,idSession);
				serverT.initProtocol();
				threads.add(serverT);

				idSession++;
			}
			while(true){
				// =============== caracteristicas del envio ====================
				System.out.println("Enviar archivo? (Y/N)");
				inputLine = br.readLine();
				if(inputLine.equalsIgnoreCase("y")) {
					// =============== Cantidad de clientes ====================
					System.out.println("Cantidad de Clientes? max: " + numClients);
					inputLine = br.readLine();
					int cantidad = Integer.parseInt(inputLine);
					// =============== Tamanio del archivo ====================
					System.out.println("Que archivo desea enviar? (250/500)mb");
					inputLine = br.readLine();
					String path = "";
					if(inputLine.equalsIgnoreCase("250mb"))
						path=sourceFilePath250;
					else if(inputLine.equalsIgnoreCase("500mb"))
						path=sourceFilePath500;
					// =============== inicia el envio a la cantidad de clientes ====================
					for (int i = 0; i < cantidad; i++)  {
						threads.get(i).setSourcePath(path);
						threads.get(i).start();

					}
					
					fileBytes = null;
					fileDesc= null;
				}
				else{
					for (ServerTheard serverTheard : threads) {
						serverTheard.endProtocol();
					}
					break;
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public static void main(String[] args) {

		Server server = new Server();
		server.init();

	}
}