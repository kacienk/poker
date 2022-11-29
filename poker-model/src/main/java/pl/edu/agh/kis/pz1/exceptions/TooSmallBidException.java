package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when player wants to bid less than current negotiation stake.
 */
public class TooSmallBidException extends Exception{
    public TooSmallBidException(String errorMessage) {
        super(errorMessage);
    }
}
