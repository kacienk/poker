package pl.edu.agh.kis.pz1.exceptions;

public class TooSmallBidException extends Exception{
    public TooSmallBidException(String errorMessage) {
        super(errorMessage);
    }
}
