package pl.edu.agh.kis.pz1;
import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    public ArrayList<Card> shuffle(ArrayList<Card> cards) {
        Collections.shuffle(cards);

        return cards;
    }

    public ArrayList<Card> generateDeck() {
        ArrayList<Card> arr = new ArrayList<Card>();

        for (Card.CardRank rank: Card.CardRank.values()) {
            for (Card.CardSuit suit: Card.CardSuit.values()) {
                arr.add(new Card(rank, suit));
            }
        }

        return arr;
    }
}
