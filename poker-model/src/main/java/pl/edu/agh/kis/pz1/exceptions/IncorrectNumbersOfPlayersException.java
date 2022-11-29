package pl.edu.agh.kis.pz1.exceptions;

/**
 * Thrown when server is trying to start a game with incorrect number of players.
 */
public class IncorrectNumbersOfPlayersException extends Exception{
    public IncorrectNumbersOfPlayersException(String errorMessage) {
        super(errorMessage);
    }
}
