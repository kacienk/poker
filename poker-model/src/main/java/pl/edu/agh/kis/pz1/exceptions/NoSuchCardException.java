package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when player wants to discard a card that don't exist.
 */
public class NoSuchCardException extends Exception{
    public NoSuchCardException(String errorMessage) {
        super(errorMessage);
    }
}
