package pl.edu.agh.kis.pz1.exceptions;

public class NoSuchCardException extends Exception{
    public NoSuchCardException(String errorMessage) {
        super(errorMessage);
    }
}
