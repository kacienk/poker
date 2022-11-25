package pl.edu.agh.kis.pz1.exceptions;

public class NotEnoughCreditException extends Exception{
    public NotEnoughCreditException(String errorMessage) {
        super(errorMessage);
    }
}
