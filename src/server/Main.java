package server;

import common.Constants;

public class Main {
    public static void main(String[] args) {
        int port = Constants.DEFAULT_PORT;

        // Allow port override from command line
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: " + port);
            }
        }

        // Start the server
        CoExistenceServer server = new CoExistenceServer(port);
        server.start();
    }
}