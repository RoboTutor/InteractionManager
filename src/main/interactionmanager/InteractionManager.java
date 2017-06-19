package main.interactionmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.interactionmanager.gui.GUI;
import rise.core.utils.tecs.Behaviour;
import rise.core.utils.tecs.TECSClient;

/**
 *
 */
public class InteractionManager {

	public static final List<String> holder = new LinkedList<String>();

	private static final boolean USE_NAO = true;
	private static final boolean USE_TEST_QUESTION = true;

	private static String userName = null;

	private static boolean answeringQuestion = false;
	private static boolean isFirstQuestion = false;

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 1111;

	public static final String START_COMMAND = "START";
	private static final String EXIT_COMMAND = "EXIT";

	private static Socket socket = null;
	private static PrintWriter out = null;
	private static BufferedReader in = null;
	private static TECSClient tc = null;

	private static GUI gui = null;

	private static Logger logger = null;

	private static String[] confirmations = { "Je vroeg", "De vraag is", "Jouw vraag was" };
	private static String[] prompts = new String[] { "Wil je nog een vraag stellen?", "Heb je een vraag?" };
	private static String[] updates = new String[] { "Even nadenken.", "Even kijken.",
			"Daar moet ik even naar kijken." };
	private static String[] negativeResponses = new String[] { "Daar kon ik geen antwoord op vinden.",
			"Sorry, daar kon ik niets over vinden." };

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		logger = LoggerFactory.getLogger(InteractionManager.class);
		logger.info("NEW SESSION");

		initStreams();
		if (USE_NAO) {
			tc = new TECSClient("192.168.1.147", "TECSClient", 1234);
			tc.startListening();
			logger.info("Connected to TECS server");
		}
		if (USE_TEST_QUESTION) {
			sendTestQuestion();
		}

		// Wait for input to continue
		try {
			System.in.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		introduce();

		while (true) {
			// Ask for a question
			if (!isFirstQuestion) {
				String prompt = prompts[(int) (Math.random() * prompts.length)];
				if (USE_NAO) {
					tc.send(new Behaviour(1, "Propose", prompt));
					tc.send(new Behaviour(1, "StandHead", ""));
				}
			}

			synchronized (holder) {
				while (holder.isEmpty()) {
					try {
						holder.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			String question = holder.remove(0);
			if (question != null) {
				if (question.equals(START_COMMAND)) {
					introduce();
					continue;
				} else if (question.equals(EXIT_COMMAND)) {
					gui.dispose();
					break;
				}
			}

			sendQuestion(question);
			answeringQuestion = true;
			isFirstQuestion = false;
			setTimeout();

			// Return response
			String answer;
			try {
				answer = in.readLine();
				answeringQuestion = false;
				logger.info("Received answer: " + answer);

				String negativeResponse = negativeResponses[(int) (Math.random() * negativeResponses.length)];
				logger.info("Negative response: " + negativeResponse);
				gui.showAnswer(negativeResponse);
				if (USE_NAO) {
					tc.send(new Behaviour(1, "Propose", negativeResponse));
					tc.send(new Behaviour(1, "StandHead", ""));
				}
			} catch (IOException ex) {

			}
		}

		// Clean up on receiving exit command
		closeStreams();
		logger.info("END OF SESSION");
	}

	private static void introduce() {
		if (gui != null) {
			gui.setVisible(false);
		}
		if (USE_NAO) {
			tc.send(new Behaviour(1, "XWaving", "Hallo! Hoe heet je?"));
		}
		userName = JOptionPane.showInputDialog("Wat is je naam?");
		if (USE_NAO) {
			tc.send(new Behaviour(1, "Me", "Dag " + userName + "! Je kan nu je vragen aan mij stellen!"));
			tc.send(new Behaviour(1, "Propose", "Zullen we beginnen?"));
		}
		isFirstQuestion = true;

		if (gui == null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					gui = new GUI();
				}
			});
		} else {
			gui.setVisible(true);
		}
	}

	private static void setTimeout() {
		new Thread(() -> {
			try {
				Thread.sleep(10000);
				if (answeringQuestion) {
					String update = updates[(int) (Math.random() * updates.length)];
					tc.send(new Behaviour(1, "Think", update));
					tc.send(new Behaviour(1, "StandHead", ""));
					setTimeout();
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		}).start();
	}

	private static void sendTestQuestion() {
		String question = "Wat is een computer?";
		out.println(question);
		logger.info("Sending question (test): " + question);

		try {
			String answer = in.readLine();
			logger.info("Received answer (test): " + answer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendQuestion(String question) {
		out.println(question);
		logger.info("Sending question: " + question);

		String confirmation = confirmations[(int) (Math.random() * confirmations.length)];
		if (USE_NAO) {
			tc.send(new Behaviour(1, "Me", confirmation + ": " + question));
			tc.send(new Behaviour(1, "State", ""));
			tc.send(new Behaviour(1, "StandHead", ""));
		}
	}

	private static void initStreams() {
		try {
			socket = new Socket(HOST, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException ex) {

		}

		if (out == null || in == null) {
			logger.error("Failed to initialize streams");
			System.exit(1);
		}
	}

	private static void closeStreams() {
		out.close();
		try {
			socket.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (USE_NAO) {
			tc.disconnect();
		}
		logger.info("Closed streams");
	}
}
