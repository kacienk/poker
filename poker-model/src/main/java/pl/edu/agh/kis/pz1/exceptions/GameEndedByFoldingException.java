package pl.edu.agh.kis.pz1.exceptions;

public class GameEndedByFoldingException extends Exception{
    public GameEndedByFoldingException(String errorMessage) {
        super(errorMessage);
    }
}
