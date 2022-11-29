package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when game ended unnaturally when all players but one have folded.
 */
public class EmptyMessageException extends Exception{
    public EmptyMessageException(String errorMessage) {
        super(errorMessage);
    }
}
