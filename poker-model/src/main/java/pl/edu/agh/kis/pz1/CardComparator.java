package pl.edu.agh.kis.pz1;

import java.util.Comparator;

public class CardComparator implements Comparator<Card> {
    public int compare(Card card1, Card card2) {
        if (card1.suit().compareTo(card2.suit()) != 0)
            return card1.suit().compareTo(card2.suit());

        return card1.rank().compareTo(card2.rank());
    }
}
