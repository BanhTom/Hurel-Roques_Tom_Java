package univ.nc.fx.network.tcp.tchat.client;

import univ.nc.fx.network.tcp.tchat.ITchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Client de tchat
 */
public class Client extends Thread implements ITchat {
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

	/**
	 * Constructeur du client
	 *
	 * @param socket   Socket pour la connexion
	 * @param username Nom d'utilisateur du client
	 */
	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	/**
	 * Envoie un message
	 */
	public void sendMessage() {
		try {
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();

			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();
				bufferedWriter.write(username + ": " + messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	/**
	 * Écoute les messages
	 */
	public void listenForMessage() {
		new Thread(new Runnable() {
			public void run() {
				String msgFromGroupChat;

				while (socket.isConnected()) {
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(msgFromGroupChat);
					} catch (IOException e) {
						closeEverything(socket, bufferedReader, bufferedWriter);
					}
				}
			}
		}).start();
	}

	/**
	 * Ferme les flux et la socket du client
	 *
	 * @param socket         Socket pour la connexion
	 * @param bufferedReader Flux de lecture pour les données reçues
	 * @param bufferedWriter Flux d'écriture pour envoyer des données
	 */
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode principale pour exécuter le client
	 *
	 * @param args Paramètres de la ligne de commande
	 * @throws IOException Si une erreur d'E/S survient
	 */
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Entrez votre nom: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 1234);
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
	}

}
