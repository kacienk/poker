package pl.edu.agh.kis.pz1;

import java.util.Comparator;

/**
 * Comparator comparing card first by rank.
 */
public class CardRankComparator implements Comparator<Card>{
    public int compare(Card card1, Card card2) {
        if (card1.rank().compareTo(card2.rank()) != 0)
            return -card1.rank().compareTo(card2.rank());

        return card1.suit().compareTo(card2.suit());
    }
}
