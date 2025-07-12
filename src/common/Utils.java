package common;

public class Utils {
    /**
     * Checks if a move command is valid (two alphabetic characters)
     */
    public static boolean isValidMoveFormat(String command) {
        return command.length() == 2 &&
                Character.isLetter(command.charAt(0)) &&
                Character.isLetter(command.charAt(1));
    }

    /**
     * Converts column letter to index (A=0, B=1, etc.)
     */
    public static int columnToIndex(char column) {
        return Character.toUpperCase(column) - 'A';
    }

    /**
     * Converts index to column letter (0=A, 1=B, etc.)
     */
    public static char indexToColumn(int index) {
        return (char)('A' + index);
    }

}