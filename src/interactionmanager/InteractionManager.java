package interactionmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class InteractionManager {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 1111;

	private static String[] prompts;
	private static String[] updates;
	private static String[] negativeResponses;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		prompts = new String[]{"Wil je nog een vraag stellen?"};
		updates = new String[]{"Even nadenken."};
		negativeResponses = new String[]{"Daar kon ik geen antwoord op vinden."};

		PrintWriter out = null;
		BufferedReader in = null;
		try {
			Socket socket = new Socket(HOST, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException ex) {
			Logger.getLogger(InteractionManager.class.getName()).log(Level.SEVERE, null, ex);
		}

		if (out == null || in == null) {
			System.err.println("Could not initialize communication streams.");
			System.exit(1);
		}

		Scanner scanner = new Scanner(System.in);
		String question;
		while (true) {
			question = scanner.nextLine();
			if (question != null) {
				out.println(question);
			}

			String answer;
			try {
				answer = in.readLine();
				System.out.println("Received answer: " + answer);
			} catch (IOException ex) {
				Logger.getLogger(InteractionManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
