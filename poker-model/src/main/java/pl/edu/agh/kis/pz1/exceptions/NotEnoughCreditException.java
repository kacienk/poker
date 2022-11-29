package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when bidder is trying to bid more credits than they have.
 * Also thrown when player has not enough credit for ante.
 */
public class NotEnoughCreditException extends Exception{
    public NotEnoughCreditException(String errorMessage) {
        super(errorMessage);
    }
}
