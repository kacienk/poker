package pl.edu.agh.kis.pz1.exceptions;

public class IncorrectNumbersOfPlayersException extends Exception{
    public IncorrectNumbersOfPlayersException(String errorMessage) {
        super(errorMessage);
    }
}
