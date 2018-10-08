package TcpServer;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;


public class Server {
	InputStreamReader input= null;
	BufferedReader br= null;
	String inputLine = "";
	ArrayList<ServerTheard> threads = null;

	private ServerSocket serverSocket = null;

	

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

	/**
	 * Accepts socket connection
	 */
	private void init() {
		try {
			System.out.println("Servidor Iniciado");
			int idSession = 0;

			System.out.println("Numero de clientes?");
			inputLine = br.readLine();
			int numClients = Integer.parseInt(inputLine);

			while(idSession < numClients) {
				Socket socket = serverSocket.accept();
				System.out.println("Cliente Conectado" + (idSession+1));

				ServerTheard serverT = new ServerTheard(socket,idSession);
				serverT.initProtocol();
				threads.add(serverT);

				idSession++;
			}
			while(true){
				System.out.println("Enviar archivo? (Y/N)");
				inputLine = br.readLine();
				if(inputLine.equalsIgnoreCase("y")) {
					
					System.out.println("Cantidad de Clientes? max: " + numClients);
					inputLine = br.readLine();
					int cantidad = Integer.parseInt(inputLine);
					System.out.println("Que archivo desea enviar? (250/500)mb");
					inputLine = br.readLine();
					for (int i = 0; i < cantidad; i++)  {
						threads.get(i).setSourceFilePath(inputLine);
						threads.get(i).start();
					}
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