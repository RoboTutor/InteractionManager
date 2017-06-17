package main.interactionmanager.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import main.interactionmanager.InteractionManager;

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1903142717890981086L;

	private JTextField questionField;
	private JTextField answerField;
	private JButton enterQuestionButton;
	private JButton nextQuestionButton;

	public GUI() {
		setSize(500, 300);
		setLocation(0, 0);

		setResizable(false);
		setTitle("GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(createPanel());

		setVisible(true);
	}

	public JPanel createPanel() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

		jPanel.add(new JLabel("Typ je vraag in"));

		questionField = new JTextField();
		questionField.setPreferredSize(new Dimension(400, 40));
		questionField.setMaximumSize(questionField.getPreferredSize());
		// questionField.setBorder(new LineBorder(Color.BLACK, 1));
		questionField.getDocument().addDocumentListener(new QuestionFieldListener(questionField));
		jPanel.add(questionField);

		JPanel emptyPanel = new JPanel();
		emptyPanel.setPreferredSize(new Dimension(400, 1));
		jPanel.add(emptyPanel);

		answerField = new JTextField();
		answerField.setPreferredSize(new Dimension(400, 50));
		answerField.setMaximumSize(answerField.getPreferredSize());
		answerField.setEditable(false);
		// answerField.setBorder(new EmptyBorder(50, 0, 10, 0));
		jPanel.add(answerField);

		JPanel buttonsPanel = new JPanel();
		enterQuestionButton = new JButton("Stuur vraag");
		enterQuestionButton.setEnabled(false);
		enterQuestionButton.addActionListener(this);
		nextQuestionButton = new JButton("Volgende vraag");
		nextQuestionButton.setEnabled(false);
		nextQuestionButton.addActionListener(this);
		buttonsPanel.add(enterQuestionButton);
		buttonsPanel.add(nextQuestionButton);
		jPanel.add(buttonsPanel);

		return jPanel;
	}

	public void showAnswer(String answer) {
		answerField.setText(answer);
		System.out.println(answerField.getText());
		answerField.repaint();
		answerField.validate();

		nextQuestionButton.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == enterQuestionButton) {
			synchronized (InteractionManager.holder) {
				InteractionManager.holder.add(questionField.getText());
				InteractionManager.holder.notify();
			}
			enterQuestionButton.setEnabled(false);
		} else if (e.getSource() == nextQuestionButton) {
			questionField.setText("");
			answerField.setText("");
			nextQuestionButton.setEnabled(false);
		}
	}

	private class QuestionFieldListener implements DocumentListener {

		private JTextField questionField;

		public QuestionFieldListener(JTextField questionField) {
			this.questionField = questionField;
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (questionField.getText().equals("")) {
				enterQuestionButton.setEnabled(false);
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			enterQuestionButton.setEnabled(true);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	}

}
