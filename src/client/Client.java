package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * CoExistence GUI Client
 * The Gui client for the game
 */
public class Client extends JFrame {
    // Constants for thhe game frame size and default network settings.
    private static final int DEFAULT_PORT = 35754;

    private Socket socket; //socket connection to the server
    private BufferedReader in;  //to receive data from the server
    private PrintWriter out;    //send commands to the server
    private boolean connected = false;  //if connected to the server ror not


    private JPanel mainPanel;   //server.Main container
    private JPanel gamePanel;   //game board panel
    private JPanel opponentDeckPanel;   //opponent cards display (top)
    private JPanel myDeckPanel;     //display our cards (bottom)
    private JPanel controlPanel;    //right side (scores, round, pass button)
    private JButton[] topColumnButtons = new JButton[6];    //for opponent cards button (defender)
    private JButton[] bottomColumnButtons = new JButton[6]; //our card buttons (attacker)
    private JButton passButton; //to pass turn
    private JLabel statusLabel;     //game log message
    private JLabel roundLabel;  //current round number
    private JLabel myScoreLabel;    //to display our score
    private JLabel opponentScoreLabel;  //display opponent score
    private JTextField serverField;     //input field for server address
    private JTextField portField;   //port
    private JButton connectButton;  //button to connect

    // Game state
    private boolean myTurn = false; //true if our turn false if opponent turn
    private int myScore = 0;    //our score
    private int opponentScore = 0;  //opponent score
    private int roundNumber = 1;        //rounds (default 1)
    private boolean gameOver = false;   //keep track if the game is over or not
    private Card[] myCards = new Card[6];   //our card
    private Card[] opponentCards = new Card[6]; //opponent cards
    private int selectedColumn = -1;    //current selected card (-1 for holder)

    public Client() {
        super("CoExistence Client");
        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);

        mainPanel = new JPanel(new BorderLayout());

        // Not in the example he gave but I think it would be helper (better)
        // fields to enter server address and port, with field already filled.
        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel("Server:"));
        serverField = new JTextField("localhost", 10);
        connectionPanel.add(serverField);
        connectionPanel.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(DEFAULT_PORT), 5);
        connectionPanel.add(portField);
        connectButton = new JButton("Connect");
        connectionPanel.add(connectButton);

        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        // for card display and select button
        gamePanel = new JPanel(new BorderLayout());

        //Top row buttons (A-F) for selecting opponent's cards (defender)
        JPanel topButtonPanel = new JPanel(new GridLayout(1, 6));
        for (int i = 0; i < 6; i++) {
            char columnLetter = (char)('A' + i);
            topColumnButtons[i] = new JButton(String.valueOf(columnLetter));
            topColumnButtons[i].setEnabled(false);  //button is disabled by default
            topButtonPanel.add(topColumnButtons[i]);
        }
        gamePanel.add(topButtonPanel, BorderLayout.NORTH);

        // Game cards display, 2 layout with top being the opponent side and bottom is ours
        JPanel cardsPanel = new JPanel(new GridLayout(2, 1));

        // Opponent's deck
        opponentDeckPanel = new JPanel(new GridLayout(1, 6));
        for (int i = 0; i < 6; i++) {
            JPanel cardSlot = new JPanel();
            cardSlot.setBackground(Color.LIGHT_GRAY);
            cardSlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            opponentDeckPanel.add(cardSlot);
        }
        cardsPanel.add(opponentDeckPanel);

        // Our deck
        myDeckPanel = new JPanel(new GridLayout(1, 6));
        for (int i = 0; i < 6; i++) {
            JPanel cardSlot = new JPanel();
            cardSlot.setBackground(Color.LIGHT_GRAY);
            cardSlot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            myDeckPanel.add(cardSlot);
        }
        cardsPanel.add(myDeckPanel);

        gamePanel.add(cardsPanel, BorderLayout.CENTER);

        // Bottom row buttons (A-F) to select our card (attacker)
        JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 6));
        for (int i = 0; i < 6; i++) {
            char columnLetter = (char)('A' + i);
            bottomColumnButtons[i] = new JButton(String.valueOf(columnLetter));
            bottomColumnButtons[i].setEnabled(false);
            bottomButtonPanel.add(bottomColumnButtons[i]);
        }
        gamePanel.add(bottomButtonPanel, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, BorderLayout.CENTER);

        // Right side
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(100, getHeight()));

        //  opponent score at top of right side
        opponentScoreLabel = new JLabel("0");
        opponentScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(opponentScoreLabel);

        controlPanel.add(Box.createVerticalStrut(100)); // for space

        // Pass button
        passButton = new JButton("PS");
        passButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        passButton.setEnabled(false);
        controlPanel.add(passButton);

        controlPanel.add(Box.createVerticalStrut(30)); //space

        // Round number label
        roundLabel = new JLabel("Round: 1");
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(roundLabel);

        // Add spacing and my score at bottom
        controlPanel.add(Box.createVerticalStrut(100));
        myScoreLabel = new JLabel("0");
        myScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(myScoreLabel);

        mainPanel.add(controlPanel, BorderLayout.EAST);

        // Status bar/log at the bottom left
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Not connected");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JScrollPane scrollPane = new JScrollPane(statusLabel);
        scrollPane.setPreferredSize(new Dimension(500, 30));
        statusPanel.add(scrollPane, BorderLayout.WEST);

        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }
    /**
     * Sets up action listeners for all buttons
     */
    private void setupListeners() {
        connectButton.addActionListener(e -> {
            if (!connected) {
                connectToServer();
            } else {
                disconnectFromServer();
            }
        });
        //pass button
        passButton.addActionListener(e -> {
            if (connected && myTurn) {
                sendCommand("PS");
                resetSelection();
            }
        });

        // Setup column button listeners
        for (int i = 0; i < 6; i++) {
            final int column = i;

            // Top column buttons (opponent's cards)
            topColumnButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (connected && myTurn && selectedColumn != -1 && opponentCards[column] != null) {
                        // Create the move command (from selected card to this opponent card)
                        char fromCol = (char)('A' + selectedColumn);
                        char toCol = (char)('A' + column);
                        String command = String.valueOf(fromCol) + String.valueOf(toCol);

                        // Send the command
                        sendCommand(command);

                        // Reset selection after move
                        resetSelection();
                    }

                }
            });

            //Bottom column buttons (my cards) - for selecting a card to use
            bottomColumnButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Only allow selection if it's player's turn and card exists
                    if (connected && myTurn && myCards[column] != null) {
                        if (selectedColumn == column) {
                            // Deselect if click one more time
                            resetSelection();
                        } else {
                            // Select
                            selectedColumn = column;
                            highlightSelectedColumn();
                        }
                    }
                }
            });
        }
    }

    //Resets the card selection state
    private void resetSelection() {
        selectedColumn = -1;
        updateCardDisplay();
        updateButtonStates();
    }

    //Highlights the selected column
    private void highlightSelectedColumn() {
        updateCardDisplay();
        updateButtonStates();
    }

    //Initiates connection to the server
    private void connectToServer() {
        try {
            String server = serverField.getText();
            int port = Integer.parseInt(portField.getText());

            socket = new Socket(server, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            connected = true;
            connectButton.setText("Disconnect");
            statusLabel.setText("Connected to server. Waiting for game to start...");

            // Start a thread to receive messages from the server
            new Thread(this::receiveMessages).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //disconnect from the server
    private void disconnectFromServer() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

            connected = false;
            myTurn = false;
            resetGameState();

            connectButton.setText("Connect");
            statusLabel.setText("Not connected");
            passButton.setEnabled(false);
            updateButtonStates();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error disconnecting: " + e.getMessage(),
                    "Disconnection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Resets all game state variables to initial values
    private void resetGameState() {
        myScore = 0;
        opponentScore = 0;
        roundNumber = 1;
        gameOver = false;
        myCards = new Card[6];
        opponentCards = new Card[6];
        selectedColumn = -1;

        myScoreLabel.setText("0");
        opponentScoreLabel.setText("0");
        roundLabel.setText("Round: 1");
        updateCardDisplay();
    }

    //Background thread method to receive messages from the server
    private void receiveMessages() {
        try {
            StringBuilder messageFrame = new StringBuilder();
            boolean inFrame = false;

            String line;
            while ((line = in.readLine()) != null) {
                // Check if this is the start of a new frame
                if (line.startsWith("/")) {
                    // Start collecting a new frame
                    messageFrame = new StringBuilder();
                    inFrame = true;
                }

                if (inFrame) {
                    messageFrame.append(line).append("\n");

                    // Check if this is the end of the frame
                    if (line.endsWith("/")) {
                        // Process the complete frame
                        processMessageFrame(messageFrame.toString());
                        inFrame = false;
                    }
                }
            }
        } catch (IOException e) {
            if (connected) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection lost: " + e.getMessage());
                    disconnectFromServer();
                });
            }
        }
    }

    //Processes a complete message frame received from the server
    private void processMessageFrame(String frame) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] lines = frame.split("\n");

                // Parse game state from the frame
                parseGameState(lines);

                // Update the UI
                updateUI();

            } catch (Exception e) {
                statusLabel.setText("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    //Parses game state information from the server message frame
    private void parseGameState(String[] lines) {
        try {
            // Parse round number - improved to handle different formats
            String roundLine = lines[8];
            int roundValue = extractRoundNumber(roundLine);
            if (roundValue > 0) {
                roundNumber = roundValue;
            }

            // Parse scores - extract numbers from brackets [X]
            String myScoreStr = "";
            String opponentScoreStr = "";

            // Look for [X] pattern in lines[9] for my score
            for (int i = 0; i < lines[9].length(); i++) {
                if (lines[9].charAt(i) == '[') {
                    StringBuilder sb = new StringBuilder();
                    i++;
                    while (i < lines[9].length() && Character.isDigit(lines[9].charAt(i))) {
                        sb.append(lines[9].charAt(i));
                        i++;
                    }
                    myScoreStr = sb.toString();
                    break;
                }
            }

            // Look for [X] pattern in lines[7] for opponent score
            for (int i = 0; i < lines[7].length(); i++) {
                if (lines[7].charAt(i) == '[') {
                    StringBuilder sb = new StringBuilder();
                    i++;
                    while (i < lines[7].length() && Character.isDigit(lines[7].charAt(i))) {
                        sb.append(lines[7].charAt(i));
                        i++;
                    }
                    opponentScoreStr = sb.toString();
                    break;
                }
            }

            // Convert extracted score strings to integers
            try {
                if (!myScoreStr.isEmpty()) {
                    myScore = Integer.parseInt(myScoreStr);
                }
                if (!opponentScoreStr.isEmpty()) {
                    opponentScore = Integer.parseInt(opponentScoreStr);
                }
            } catch (NumberFormatException e) {

            }

            // Parse turn indicators
            char turnIndicator = lines[5].charAt(38);
            if (turnIndicator == 'v') {
                myTurn = true;
                gameOver = false;
            } else if (turnIndicator == '^') {
                myTurn = false;
                gameOver = false;
            } else if (turnIndicator == '-') {
                myTurn = false;
                // Only consider the game over if we've reached round 5 or someone has 9+ points
                gameOver = (roundNumber >= 5 || myScore >= 9 || opponentScore >= 9);
            }

            // Parse my cards (bottom row)
            myCards = new Card[6];
            for (int i = 0; i < 6; i++) {
                myCards[i] = parseCard(i, lines, 11, 12, 13);
            }

            // Parse opponent cards (top row)
            opponentCards = new Card[6];
            for (int i = 0; i < 6; i++) {
                opponentCards[i] = parseCard(i, lines, 3, 4, 5);
            }

            // Parse log message
            String logMessage = lines[17].trim();

            // Check if the log message indicates game over
            if (logMessage.contains("GAME OVER") ||
                    logMessage.contains("WON!") ||
                    logMessage.toLowerCase().contains("draw")) {
                gameOver = true;
            }


            statusLabel.setText(logMessage);


        } catch (Exception e) {
            System.err.println("Error in parseGameState: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //Enhanced method to extract round number from various possible formats

    private int extractRoundNumber(String line) {
        try {
            // Look for 'R' followed by a number
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == 'R' && i + 1 < line.length() && Character.isDigit(line.charAt(i + 1))) {
                    StringBuilder sb = new StringBuilder();
                    i++;
                    while (i < line.length() && Character.isDigit(line.charAt(i))) {
                        sb.append(line.charAt(i));
                        i++;
                    }
                    if (sb.length() > 0) {
                        return Integer.parseInt(sb.toString());
                    }
                }
            }

            // Fallback to looking for a number near the right end of the line
            if (line.length() >= 3) {
                String end = line.substring(line.length() - 3).trim();
                for (int i = 0; i < end.length(); i++) {
                    if (Character.isDigit(end.charAt(i))) {
                        StringBuilder sb = new StringBuilder();
                        while (i < end.length() && Character.isDigit(end.charAt(i))) {
                            sb.append(end.charAt(i));
                            i++;
                        }
                        if (sb.length() > 0) {
                            return Integer.parseInt(sb.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing round number: " + e.getMessage());
        }
        return -1; // Return -1 to indicate parsing failure
    }

    //Parses a card from the ASCII art in the message frame
    private Card parseCard(int column, String[] lines, int row1, int row2, int row3) {
        int startCol = 2 + column * 6;
        int endCol = startCol + 3;

        String art1 = getSubstring(lines[row1], startCol, endCol);
        String art2 = getSubstring(lines[row2], startCol, endCol);
        String art3 = getSubstring(lines[row3], startCol, endCol);

        if (art1.trim().isEmpty() && art2.trim().isEmpty() && art3.trim().isEmpty()) {
            return null;
        }

        // Determine card type based on ASCII art
        if (art1.contains("<7>") || art1.contains("<7 ") || (art1.contains("<") && art3.contains("L"))) {
            return new Card(Card.Type.AXE);
        } else if (art1.contains("[=]") || art1.contains("[=") || (art1.contains("[") && art2.contains("I") && art3.contains("I"))) {
            return new Card(Card.Type.HAMMER);
        } else if (art1.contains("/") || (art1.contains(" /") && art3.contains("X"))) {
            return new Card(Card.Type.SWORD);
        } else if (art1.contains("^") || (art1.contains(" ^") && art3.contains("/^\\"))) {
            return new Card(Card.Type.ARROW);
        }

        // Default to null if we couldn't determine the type
        return null;
    }

    private String getSubstring(String line, int start, int end) {
        if (line.length() >= end) {
            return line.substring(start, end);
        } else if (line.length() > start) {
            return line.substring(start);
        } else {
            return "";
        }
    }

    //Updates all UI components based on current game state
    private void updateUI() {
        // Update score and round display
        myScoreLabel.setText("You: " + myScore);
        opponentScoreLabel.setText("Opponent: " + opponentScore);
        roundLabel.setText("Round: " + roundNumber);

        // Reset selection when receiving new game state
        selectedColumn = -1;

        // Update card display
        updateCardDisplay();

        // Update button states
        updateButtonStates();

        // Update game over status in the UI
        if (gameOver) {
            if (myScore > opponentScore) {
                statusLabel.setText("Game over - You won!");
            } else if (opponentScore > myScore) {
                statusLabel.setText("Game over - You lost!");
            } else if (roundNumber >= 5) {
                statusLabel.setText("Game over - Draw (5 rounds completed)");
            }
        }
    }

    //Updates the card display panels based on current game state
    private void updateCardDisplay() {
        // Update opponent's cards
        for (int i = 0; i < 6; i++) {
            JPanel cardSlot = (JPanel) opponentDeckPanel.getComponent(i);
            cardSlot.removeAll();

            if (opponentCards[i] != null) {
                cardSlot.setLayout(new BorderLayout());
                JLabel cardLabel = new JLabel(opponentCards[i].getType().toString(), SwingConstants.CENTER);
                cardSlot.add(cardLabel, BorderLayout.CENTER);
                cardSlot.setBackground(Color.WHITE);
            } else {
                cardSlot.setLayout(new BorderLayout());
                cardSlot.setBackground(Color.LIGHT_GRAY);
            }

            cardSlot.revalidate();
            cardSlot.repaint();
        }

        // Update my cards
        for (int i = 0; i < 6; i++) {
            JPanel cardSlot = (JPanel) myDeckPanel.getComponent(i);
            cardSlot.removeAll();

            if (myCards[i] != null) {
                cardSlot.setLayout(new BorderLayout());
                JLabel cardLabel = new JLabel(myCards[i].getType().toString(), SwingConstants.CENTER);
                cardSlot.add(cardLabel, BorderLayout.CENTER);

                // Highlight selected card
                if (i == selectedColumn) {
                    cardSlot.setBackground(Color.YELLOW);
                } else {
                    cardSlot.setBackground(Color.WHITE);
                }
            } else {
                cardSlot.setLayout(new BorderLayout());
                cardSlot.setBackground(Color.LIGHT_GRAY);
            }

            cardSlot.revalidate();
            cardSlot.repaint();
        }
    }


    //Updates button enabled states based on current game state
    private void updateButtonStates() {
        // Only allow interaction if connected, it's your turn, and game is not over
        boolean canInteract = connected && myTurn && !gameOver;

        // Update pass button
        passButton.setEnabled(canInteract);

        // Update top column buttons (opponent's cards)
        for (int i = 0; i < 6; i++) {
            // Can only click opponent cards if we have a card selected and there's a card to attack
            boolean canAttack = canInteract && selectedColumn != -1 && opponentCards[i] != null;
            topColumnButtons[i].setEnabled(canAttack);

            // Give visual feedback about which opponent cards can be attacked
            if (canAttack) {
                topColumnButtons[i].setBackground(new Color(255, 200, 200)); // Light red for attackable
            } else {
                topColumnButtons[i].setBackground(null); // Default button color
            }
        }

        // Update bottom column buttons (my cards)
        for (int i = 0; i < 6; i++) {
            // Can only click my cards if it's my turn and the card exists
            boolean canSelect = canInteract && myCards[i] != null;
            bottomColumnButtons[i].setEnabled(canSelect);

            // Highlight the selected card's button
            if (i == selectedColumn) {
                bottomColumnButtons[i].setBackground(Color.YELLOW);
            } else {
                bottomColumnButtons[i].setBackground(null); // Default button color
            }
        }
    }

    //Sends a command to the server
    private void sendCommand(String command) {
        if (connected && out != null) {
            out.println(command);
        }
    }

    /**
     * Card class for client-side representation
     */
    private static class Card {
        public enum Type {
            AXE, HAMMER, SWORD, ARROW
        }

        private Type type;

        public Card(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }
    }

    /**
     * server.Main method to start the client
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}