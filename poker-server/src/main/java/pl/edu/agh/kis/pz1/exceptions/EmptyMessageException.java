package pl.edu.agh.kis.pz1.exceptions;

public class EmptyMessageException extends Exception{
    public EmptyMessageException(String errorMessage) {
        super(errorMessage);
    }
}
