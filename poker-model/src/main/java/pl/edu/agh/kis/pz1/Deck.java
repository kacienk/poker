package pl.edu.agh.kis.pz1;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class Deck is a contains a deck and provides necessary methods for it.
 */
public class Deck {
    /**
     * Deck containing all cards.
     */
    private ArrayList<Card> deck;

    Deck() {
        newDeck();
    }

    /**
     * Creates new sorted deck.
     */
    public void newDeck() {
        deck =  Card.newDeck();
    }

    public void shuffle() {
        Collections.shuffle(deck);
    }

    public ArrayList<Card> deck() { return deck; }

    /**
     * Deals a card and removes it from the deck.
     *
     * @return Card to be dealt.
     */
    public Card dealCard() {
        return deck.remove(0);
    }
}
