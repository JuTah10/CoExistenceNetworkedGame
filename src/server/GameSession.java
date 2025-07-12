package server;

import common.Constants;
import common.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession implements Runnable {
    private Socket player1Socket;
    private Socket player2Socket;
    private BufferedReader player1Input;
    private BufferedReader player2Input;
    private PrintWriter player1Output;
    private PrintWriter player2Output;
    private GameState gameState;
    private boolean gameRunning;

    public GameSession(Socket player1Socket, Socket player2Socket) {
        this.player1Socket = player1Socket;
        this.player2Socket = player2Socket;
        this.gameState = new GameState();
        this.gameRunning = true;

        try {
            // Initialize input/output streams
            player1Input = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            player2Input = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));

            player1Output = new PrintWriter(player1Socket.getOutputStream(), true);
            player2Output = new PrintWriter(player2Socket.getOutputStream(), true);

        } catch (IOException e) {
            System.err.println("Error setting up game session: " + e.getMessage());
            closeConnections();
        }
    }

    @Override
    public void run() {
        System.out.println("Starting new game session");

        try {
            // Send initial game state to both players
            sendGameStateToPlayers();

            // Main game loop
            while (gameRunning && !gameState.isGameOver()) {
                // Get current player
                int currentPlayer = gameState.getCurrentPlayer();

                // Wait for input from the current player
                String command;
                if (currentPlayer == 0) {
                    command = player1Input.readLine();
                } else {
                    command = player2Input.readLine();
                }

                // Process the command
                processCommand(currentPlayer, command);

                // Send updated game state to both players
                sendGameStateToPlayers();
            }

            // Game is over, wait a moment before closing
            Thread.sleep(5000);

        } catch (IOException e) {
            System.err.println("Communication error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Game session interrupted: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    /**
     * Process a command from a player
     */
    private void processCommand(int playerIndex, String command) {
        if (command == null) {
            // Player disconnected
            gameRunning = false;
            return;
        }

        // Convert to uppercase
        command = command.toUpperCase();

        // Check if it's a pass command
        if (command.equals("PS")) {
            gameState.pass(playerIndex);
            return;
        }

        // Check if it's a move command (two letters)
        if (Utils.isValidMoveFormat(command)) {
            char fromChar = command.charAt(0);
            char toChar = command.charAt(1);

            int fromColumn = Utils.columnToIndex(fromChar);
            int toColumn = Utils.columnToIndex(toChar);

            gameState.makeMove(playerIndex, fromColumn, toColumn);


            return;
        }
        else{
            gameState.setLastLogMessage("SYNTAX ERROR");
            sendGameStateToPlayers();
        }



    }

    /**
     * Send the current game state to both players
     */
    private void sendGameStateToPlayers() {
        String player1Frame = MessageFormatter.generateMessageFrame(gameState, true);
        String player2Frame = MessageFormatter.generateMessageFrame(gameState, false);

        player1Output.println(player1Frame);
        player2Output.println(player2Frame);
    }

    /**
     * Close all connections
     */
    private void closeConnections() {
        try {
            if (player1Input != null) player1Input.close();
            if (player2Input != null) player2Input.close();
            if (player1Output != null) player1Output.close();
            if (player2Output != null) player2Output.close();
            if (player1Socket != null) player1Socket.close();
            if (player2Socket != null) player2Socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}