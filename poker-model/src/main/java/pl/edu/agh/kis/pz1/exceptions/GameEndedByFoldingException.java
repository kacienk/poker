package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when game has ended unnaturally when all the players but one folded.
 */
public class GameEndedByFoldingException extends Exception{
    public GameEndedByFoldingException(String errorMessage) {
        super(errorMessage);
    }
}
