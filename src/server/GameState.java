package server;

import java.util.*;

public class GameState {
    private List<Card> deck;
    private List<Card> player1Hand;
    private List<Card> player2Hand;
    private int player1Score;
    private int player2Score;
    private int currentPlayer; // 0 for player1, 1 for player2
    private int roundNumber;
    private boolean player1Passed;
    private boolean player2Passed;
    private String lastLogMessage;
    private boolean gameOver;

    public GameState() {
        player1Hand = new ArrayList<>();
        player2Hand = new ArrayList<>();
        initializeGame();
    }

    /**
     * Initialize the game state
     */
    private void initializeGame() {
        player1Score = 0;
        player2Score = 0;
        roundNumber = 1;
        player1Passed = false;
        player2Passed = false;
        gameOver = false;
        lastLogMessage = "NEW GAME";

        // Randomly determine starting player
        Random random = new Random();
        currentPlayer = random.nextInt(2);

        // Initialize the game without changing the message
        createDeck();
        shuffleDeck();
        dealCards();
    }

    /**
     * Start a new round: shuffle the deck and deal cards
     */
    private void startNewRound() {
        createDeck();
        shuffleDeck();
        dealCards();
        player1Passed = false;
        player2Passed = false;

        // when round is at least 2, then shows new round instead of new game
        if (roundNumber > 1) {
            lastLogMessage = "NEW ROUND";
        }
    }

    /**
     * Create the standard deck of 12 cards
     */
    private void createDeck() {
        deck = new ArrayList<>();

        // Add 4 of each type
        for (int i = 0; i < 4; i++) {
            deck.add(new Card(Card.Type.AXE));
            deck.add(new Card(Card.Type.HAMMER));
            deck.add(new Card(Card.Type.SWORD));
            deck.add(new Card(Card.Type.ARROW));
        }
    }

    /**
     * Shuffle the deck
     */
    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    /**
     * Deal 6 cards to each player
     */
    private void dealCards() {
        player1Hand.clear();
        player2Hand.clear();

        //6 cards for each player
        for (int i = 0; i < 6; i++) {
            player1Hand.add(deck.remove(0));
            player2Hand.add(deck.remove(0));
        }
    }

    /**
     * Process a player move
     * @param playerIndex 0 for player1, 1 for player2
     * @param fromColumn the column index of the attacking card (0-5)
     * @param toColumn the column index of the target card (0-5)
     * @return true if the move was valid, false otherwise
     */
    public boolean makeMove(int playerIndex, int fromColumn, int toColumn) {
        if (gameOver) {
            lastLogMessage = "GAME OVER";
            return false;
        }

        if (fromColumn < 0 || fromColumn >= 6 || toColumn < 0 || toColumn >= 6) {
            lastLogMessage = "INVALID MOVE: OUT OF BOUNDS";
            return false;
        }

        List<Card> attackerHand = (playerIndex == 0) ? player1Hand : player2Hand;
        List<Card> defenderHand = (playerIndex == 0) ? player2Hand : player1Hand;


        Card attacker = attackerHand.get(fromColumn);
        Card defender = defenderHand.get(toColumn);

        if (!attacker.canDefeat(defender)) {
            lastLogMessage = "INVALID MOVE: CANNOT DEFEAT TARGET";
            return false;
        }

        // Valid move, execute it
        if (defender.earnsPoints(attacker)) {
            if (playerIndex == 0) {
                player1Score++;
            } else {
                player2Score++;
            }
        }

        // Remove the defeated card
        defenderHand.set(toColumn, null);

        // Update log message
        lastLogMessage = attacker.getType() + " TAKES " + defender.getType();

        // Switch turn to other player
        currentPlayer = 1 - currentPlayer;

        // Reset passing status for the player who just moved
        if (playerIndex == 0) {
            player1Passed = false;
        } else {
            player2Passed = false;
        }

        // Check if game is over
        checkGameOver();

        return true;
    }

    /**
     * Player passes their turn
     * @param playerIndex 0 for player1, 1 for player2
     * @return true if the pass was valid, false otherwise
     */
    public boolean pass(int playerIndex) {
        if (gameOver) {
            lastLogMessage = "GAME OVER";
            return false;
        }

        if (playerIndex != currentPlayer) {
            lastLogMessage = "NOT YOUR TURN";
            return false;
        }

        // Mark the player as passed
        if (playerIndex == 0) {
            player1Passed = true;
        } else {
            player2Passed = true;
        }

        lastLogMessage = "PLAYER PASSED";

        // Switch turn to other player
        currentPlayer = 1 - currentPlayer;

        // If both players have passed, start a new round
        if (player1Passed && player2Passed) {
            roundNumber++;

            // Check if we've reached max rounds (if we hit 5 rounds or not)
            if (roundNumber >= 5) {
                gameOver = true;
                lastLogMessage = "GAME OVER - DRAW";
            } else {
                startNewRound();
            }
        }

        // Check if game is over
        checkGameOver();

        return true;
    }


    /**
     * Check if any of the player reach 9 (if it is then adjust the log message
     */
    private void checkGameOver() {

        if (player1Score >= 9) {
            gameOver = true;
            lastLogMessage += "; PLAYER 1 WON!";
        } else if (player2Score >= 9) {
            gameOver = true;
            lastLogMessage += "; PLAYER 2 WON!";
        }
    }

    public void setLastLogMessage(String message){
        this.lastLogMessage = message;
    }



    // Getters
    public List<Card> getPlayer1Hand() { return player1Hand; }
    public List<Card> getPlayer2Hand() { return player2Hand; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer2Score() { return player2Score; }
    public int getCurrentPlayer() { return currentPlayer; }
    public int getRoundNumber() { return roundNumber; }
    public String getLastLogMessage() { return lastLogMessage; }
    public boolean isGameOver() { return gameOver; }
}