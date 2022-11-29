package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when player wants to discard more than four cards.
 */
public class IncorrectNumberOfCardsException extends Exception {
    public IncorrectNumberOfCardsException(String errorMessage) {
        super(errorMessage);
    }
}
