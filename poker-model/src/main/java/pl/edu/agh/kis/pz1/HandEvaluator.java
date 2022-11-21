package pl.edu.agh.kis.pz1;

import java.util.ArrayList;

public class HandEvaluator {
    public enum HandValues { HIGHCARD, PAIR, TWOPAIR, THREEOFAKIND, STRAIGHT, FLUSH, FULLHOUSE,
        FOUROFAKIND, STRAIGHTFLUSH }

    public HandValues evaluate(ArrayList<Card> hand) {
        CardComparator cardComparator = new CardComparator();
        hand.sort(cardComparator);

        if (hasStraightFlush(hand)) {
            return HandValues.STRAIGHTFLUSH;
        }

        return HandValues.HIGHCARD;
    }

    private boolean hasStraightFlush(ArrayList<Card> hand) {
        int max_same_suit = 0;
        int last_same_suit = 0;

        for (int i = 1; i < hand.size(); i++) {
            if (hand.get(i - 1).suit() == hand.get(i).suit()) {
                last_same_suit += 1;
            }
            else {
                last_same_suit = 0;
            }

            if (last_same_suit > max_same_suit) {
                max_same_suit = last_same_suit;
            }
        }
    }

}
