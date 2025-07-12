package server;

import common.Constants;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CoExistenceServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;

    public CoExistenceServer(int port) {
        this.port = port;
        this.threadPool = Executors.newCachedThreadPool();
        this.running = false;
    }

    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;

            System.out.println("CoExistence Server started on port " + port);
            System.out.println("Waiting for players to connect...");

            // Main server loop
            while (running) {
                // Wait for player 1
                System.out.println("Waiting for player 1...");
                Socket player1Socket = serverSocket.accept();
                System.out.println("Player 1 connected: " + player1Socket.getInetAddress());

                // Wait for player 2
                System.out.println("Waiting for player 2...");
                Socket player2Socket = serverSocket.accept();
                System.out.println("Player 2 connected: " + player2Socket.getInetAddress());

                // Create and start a new game session
                GameSession gameSession = new GameSession(player1Socket, player2Socket);
                threadPool.execute(gameSession);

                System.out.println("New game session started");
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;

        try {
            if (serverSocket != null) {
                serverSocket.close();
            }

            if (threadPool != null) {
                threadPool.shutdown();
            }
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}