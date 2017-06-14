package interactionmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import rise.core.utils.tecs.Behaviour;
import rise.core.utils.tecs.TECSClient;

/**
 *
 */
public class InteractionManager {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 1111;

	private static String[] confirmations = { "Je vroeg", "De vraag is", "Jouw vraag was" };
	private static String[] prompts = new String[] { "Wil je nog een vraag stellen?", "Heb je een vraag?" };
	private static String[] updates = new String[] { "Even nadenken.", "Even kijken" };
	private static String[] negativeResponses = new String[] { "Daar kon ik geen antwoord op vinden.",
			"Sorry, daar kon ik niets over vinden." };

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

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

		TECSClient tc = new TECSClient("192.168.1.147", "TECSClient", 1234);
		tc.startListening();

		Scanner scanner = new Scanner(System.in);
		String question;
		while (true) {
			String prompt = prompts[(int) (Math.random() * confirmations.length - 1)];
			tc.send(new Behaviour(1, "Propose", prompt));
			tc.send(new Behaviour(1, "StandHead", ""));

			question = scanner.nextLine();
			if (question != null) {
				out.println(question);
				String confirmation = confirmations[(int) (Math.random() * confirmations.length - 1)];
				tc.send(new Behaviour(1, "Me", confirmation + ": " + question));
				tc.send(new Behaviour(1, "State", ""));
				tc.send(new Behaviour(1, "StandHead", ""));
			}

			String answer;
			try {
				answer = in.readLine();
				System.out.println(answer);
				tc.send(new Behaviour(1, "Propose", answer));
				tc.send(new Behaviour(1, "StandHead", ""));
			} catch (IOException ex) {
				Logger.getLogger(InteractionManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
