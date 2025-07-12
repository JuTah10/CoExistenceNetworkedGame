package server;

public class Card {
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

    /**
     * Checks if this card can defeat the target card
     * @return true if this card can defeat the target, false otherwise
     */
    public boolean canDefeat(Card target) {
        // Arrow can defeat any unit
        if (this.type == Type.ARROW) {
            return true;
        }

        // Any unit can defeat an Arrow
        if (target.type == Type.ARROW) {
            return true;
        }

        switch (this.type) {
            case AXE:
                return target.type == Type.HAMMER;
            case HAMMER:
                return target.type == Type.SWORD;
            case SWORD:
                return target.type == Type.AXE;
            default:
                return false;
        }
    }

    /**
     * Checks if defeating this card with the given attacker earns points
     */
    public boolean earnsPoints(Card attacker) {
        return this.type != Type.ARROW && attacker.type != Type.ARROW;
    }

    /**
     * Gets the 3x3 ASCII representation of the card
     */
    public String[] getAsciiArt() {
        String[] art = new String[3];

        switch (type) {
            case AXE:
                art[0] = "<7>";
                art[1] = " I ";
                art[2] = " L ";
                break;
            case HAMMER:
                art[0] = "[=]";
                art[1] = " I  ";
                art[2] = " I  ";
                break;
            case SWORD:
                art[0] = "  /";
                art[1] = " / ";
                art[2] = "X  ";
                break;
            case ARROW:
                art[0] = " ^ ";
                art[1] = " I ";
                art[2] = "/^\\";
                break;
        }

        return art;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}