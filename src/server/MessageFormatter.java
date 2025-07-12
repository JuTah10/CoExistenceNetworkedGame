package server;

import common.Constants;

public class MessageFormatter {

    /**
     * Generate the game message frame for a player
     * @param state the current game state
     * @param forPlayer1 true if generating for player1, false for player2
     * @return the formatted 40x19 message frame
     */
    public static String generateMessageFrame(GameState state, boolean forPlayer1) {
        String[] lines = new String[Constants.FRAME_HEIGHT];

        // Initialize all lines with spaces
        for (int i = 0; i < Constants.FRAME_HEIGHT; i++) {
            lines[i] = " ".repeat(Constants.FRAME_WIDTH);
        }

        // Draw the frame border
        drawFrameBorder(lines);

        // Draw column headers (A-F)
        drawColumnHeaders(lines);

        // Draw card slots and separators
        drawCardSlots(lines);

        // Draw turn indicators
        drawTurnIndicators(lines, state, forPlayer1);

        // Draw scores and round
        drawScoreAndRound(lines, state, forPlayer1);

        // Draw cards
        drawCards(lines, state, forPlayer1);

        // Draw log message
        drawLogMessage(lines, state.getLastLogMessage());

        // Combine all lines into the frame
        StringBuilder frame = new StringBuilder();
        for (String line : lines) {
            frame.append(line).append("\n");
        }

        return frame.toString();
    }

    /**
     * Draw the frame border
     */
    private static void drawFrameBorder(String[] lines) {
        // Top border
        lines[0] = "/" + "-".repeat(Constants.FRAME_WIDTH - 2) + "\\";


        // Bottom border
        lines[Constants.FRAME_HEIGHT - 1] = "\\" + "-".repeat(Constants.FRAME_WIDTH - 2) + "/";
    }

    /**
     * Draw column headers (A-F)
     */
    private static void drawColumnHeaders(String[] lines) {
        // Column headers for top row
        lines[1] = replaceSubstring(lines[1], " A     B     C     D     E     F", 2);

        // Column headers for bottom row
        lines[15] = replaceSubstring(lines[15], " A     B     C     D     E     F", 2);
    }

    /**
     * Draw card slots and separators
     */
    private static void drawCardSlots(String[] lines) {
        // Top horizontal separators with slashes
        String topSep = "/---\\";
        for (int i = 0; i < 6; i++) {
            int pos = 1 + i * 6;
            lines[2] = replaceSubstring(lines[2], topSep, pos);
        }

        // Bottom horizontal separators
        String bottomSep = "\\---/";
        for (int i = 0; i < 6; i++) {
            int pos = 1 + i * 6;
            lines[6] = replaceSubstring(lines[6], bottomSep, pos);
            lines[14] = replaceSubstring(lines[14], bottomSep, pos);
        }

        // Top separator for bottom cards
        String topBottomSep = "/---\\";
        for (int i = 0; i < 6; i++) {
            int pos = 1 + i * 6;
            lines[10] = replaceSubstring(lines[10], topBottomSep, pos);
        }

        // Vertical separators
        for (int row = 3; row <= 5; row++) {
            for (int i = 0; i < 6; i++) {
                int pos = 1 + i * 6;
                lines[row] = replaceCharAt(lines[row], pos, '|');
                lines[row] = replaceCharAt(lines[row], pos + 4, '|');
            }
        }

        for (int row = 11; row <= 13; row++) {
            for (int i = 0; i < 6; i++) {
                int pos = 1 + i * 6;
                lines[row] = replaceCharAt(lines[row], pos, '|');
                lines[row] = replaceCharAt(lines[row], pos + 4, '|');
            }
        }

        // Score separator line
        String scoreSep = "<" + "=".repeat(Constants.FRAME_WIDTH - 7) + ">";
        lines[8] = replaceSubstring(lines[8], scoreSep, 1);

        // Divider line below column headers
        String divider = "-".repeat(Constants.FRAME_WIDTH - 2);
        lines[16] = replaceSubstring(lines[16], divider, 1);
    }

    /**
     * Draw turn indicators
     */
    private static void drawTurnIndicators(String[] lines, GameState state, boolean forPlayer1) {
        int currentPlayer = state.getCurrentPlayer();
        boolean gameOver = state.isGameOver();

        char topIndicator, bottomIndicator;

        if (gameOver) {
            // Game over - both indicators are '-'
            lines[3] = replaceCharAt(lines[3], 38,'-');
            lines[4] = replaceCharAt(lines[4], 38,'|');
            lines[5] = replaceCharAt(lines[5], 38, '-');
            //Draw bottom indicator
            lines[12] = replaceCharAt(lines[12], 38,'-');
            lines[13] = replaceCharAt(lines[13], 38,'|');
            lines[14] = replaceCharAt(lines[14], 38, '-');
        } else if ((forPlayer1 && currentPlayer == 0) || (!forPlayer1 && currentPlayer == 1)) {
            // My turn - both indicators point to me (bottom player)
            lines[3] = replaceCharAt(lines[3], 38,'-');
            lines[4] = replaceCharAt(lines[4], 38,'|');
            lines[5] = replaceCharAt(lines[5], 38, 'v');
            //Draw bottom indicator
            lines[12] = replaceCharAt(lines[12], 38,'-');
            lines[13] = replaceCharAt(lines[13], 38,'|');
            lines[14] = replaceCharAt(lines[14], 38, 'v');
        } else {
            // Opponent's turn - both indicators point to them (top player)
            lines[3] = replaceCharAt(lines[3], 38,'^');
            lines[4] = replaceCharAt(lines[4], 38,'|');
            lines[5] = replaceCharAt(lines[5], 38, '-');
            //Draw bottom indicator
            lines[12] = replaceCharAt(lines[12], 38,'^');
            lines[13] = replaceCharAt(lines[13], 38,'|');
            lines[14] = replaceCharAt(lines[14], 38, '-');
        }

    }

    /**
     * Draw scores and round number
     */
    private static void drawScoreAndRound(String[] lines, GameState state, boolean forPlayer1) {
        int player1Score = state.getPlayer1Score();
        int player2Score = state.getPlayer2Score();
        int round = state.getRoundNumber();

        // Format round display
        String roundInfo = "R" + round +"<";
        lines[8] = replaceSubstring(lines[8], roundInfo, Constants.FRAME_WIDTH - 4);

        // Format score display - use brackets to show scores
        String topScoreDisplay, bottomScoreDisplay;

        if (forPlayer1) {
            // Player 1's view - their score on bottom, opponent on top
            topScoreDisplay = "[" + player2Score + "]";
            bottomScoreDisplay = "[" + player1Score + "]";
        } else {
            // Player 2's view - their score on bottom, opponent on top
            topScoreDisplay = "[" + player1Score + "]";
            bottomScoreDisplay = "[" + player2Score + "]";
        }

        lines[7] = replaceSubstring(lines[7], topScoreDisplay, Constants.FRAME_WIDTH - 4);
        lines[9] = replaceSubstring(lines[9], bottomScoreDisplay, Constants.FRAME_WIDTH - 4);
    }

    /**
     * Draw cards on the board
     */
    private static void drawCards(String[] lines, GameState state, boolean forPlayer1) {
        // Get the hands
        java.util.List<Card> myHand = forPlayer1 ? state.getPlayer1Hand() : state.getPlayer2Hand();
        java.util.List<Card> opponentHand = forPlayer1 ? state.getPlayer2Hand() : state.getPlayer1Hand();

        // Draw my cards (bottom)
        for (int i = 0; i < myHand.size(); i++) {
            Card card = myHand.get(i);
            if (card != null) {
                drawCard(lines, card, i, true);
            }
        }

        // Draw opponent cards (top)
        for (int i = 0; i < opponentHand.size(); i++) {
            Card card = opponentHand.get(i);
            if (card != null) {
                drawCard(lines, card, i, false);
            }
        }
    }

    /**
     * Draw a single card
     */
    private static void drawCard(String[] lines, Card card, int column, boolean isBottom) {
        String[] cardArt = card.getAsciiArt();

        int rowStart = isBottom ? 11 : 3;
        int colStart = 2 + column * 6;

        // Draw the card art
        for (int i = 0; i < 3; i++) {
            lines[rowStart + i] = replaceSubstring(lines[rowStart + i], cardArt[i], colStart);
        }
    }

    /**
     * Draw log message
     */
    private static void drawLogMessage(String[] lines, String message) {
        if (message == null) message = "NEW GAME";

        // Center the message in the log area (maximum 30 characters)
        if (message.length() > 30) {
            message = message.substring(0, 30);
        }

        // Place message on the second-to-last line
        lines[17] = replaceSubstring(lines[17], message, 1);
    }

    /**
     * Replace a substring in a string at the specified position
     */
    private static String replaceSubstring(String original, String replacement, int position) {
        if (position < 0 || position + replacement.length() > original.length()) {
            return original;
        }

        return original.substring(0, position) + replacement +
                original.substring(position + replacement.length());
    }

    /**
     * Replace a character in a string at the specified position
     */
    private static String replaceCharAt(String original, int position, char replacement) {
        if (position < 0 || position >= original.length()) {
            return original;
        }

        char[] chars = original.toCharArray();
        chars[position] = replacement;
        return new String(chars);
    }
}