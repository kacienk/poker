package pl.edu.agh.kis.pz1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private ArrayList<Card> deck;

    Deck() {
        newDeck();
    }

    public void newDeck() {
        deck =  Card.newDeck();
    }

    public ArrayList<Card> shuffle() {
        Collections.shuffle(deck);

        return deck;
    }

    public ArrayList<Card> deck() { return deck; }

    public Card dealCard() {
        return deck.remove(0);
    }
}
