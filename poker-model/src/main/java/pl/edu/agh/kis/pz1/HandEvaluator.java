package pl.edu.agh.kis.pz1;

import java.util.ArrayList;

public class HandEvaluator {
    /**
     * All possible values of the hand.
     */
    public enum HandValues { HIGHCARD("High card"), PAIR ("Pair"), TWOPAIR("Two pair"), THREEOFAKIND("Three of a kind"),
        STRAIGHT("Straight"), FLUSH("Flush"), FULLHOUSE("Full house"), FOUROFAKIND("Four of a kind"),
        STRAIGHTFLUSH("Straight flush");

        private final String evaluation;
        private HandValues(String eval) {
            this.evaluation = eval;
        }

        @Override
        public String toString() {
            return evaluation;
        }
    }

    /**
     * Evaluates given hand.
     *
     * @param hand Hand to evaluate.
     * @return Value of the hand.
     */
    public HandValues evaluate(ArrayList<Card> hand) {
        // Returns type of the highest hand value
        CardRankComparator cardRankComparator = new CardRankComparator();
        hand.sort(cardRankComparator);

        if (hasStraightFlush(hand) != -1)
            return HandValues.STRAIGHTFLUSH;
        if (hasFourOfAKind(hand) != -1)
            return HandValues.FOUROFAKIND;
        if (hasFullHouse(hand) != -1)
            return HandValues.FULLHOUSE;
        if (hasFlush(hand) != -1)
            return HandValues.FLUSH;
        if (hasStraight(hand) != -1)
            return HandValues.STRAIGHT;
        if (hasThreeOfAKind(hand) != -1)
            return HandValues.THREEOFAKIND;
        if (hasTwoPair(hand) != -1)
            return HandValues.TWOPAIR;
        if (hasPair(hand) != -1)
            return HandValues.PAIR;

        return HandValues.HIGHCARD;
    }

    /**
     * Settles up given handValue draws. Function works ONLY when draw has occurred.
     * Returns a negative integer, zero, or a positive integer as hand1 is worse, they are dead equal, hand2 is worse.
     *
     * @param hand1 First hand.
     * @param hand2 Second hand.
     * @param handValue Type of hand value that was drawn.
     * @return Integer with evaluation.
     */
    public int settleDraw(ArrayList<Card> hand1, ArrayList<Card> hand2, HandValues handValue) {
        // Settles up given handValue draws. Function works ONLY when draw has occurred.
        // Returns a negative integer, zero, or a positive integer \
        // as hand1 is worse, they are dead equal, hand2 is worse.

        CardRankComparator cardRankComparator = new CardRankComparator();
        hand1.sort(cardRankComparator);
        hand2.sort(cardRankComparator);

        switch (handValue) {
            case STRAIGHTFLUSH, FLUSH, STRAIGHT -> { return settleStraightOrFlushDraw(hand1, hand2); }
            case FOUROFAKIND -> { return settleFourOfAKindDraw(hand1, hand2); }
            case FULLHOUSE -> { return settleFullHouseDraw(hand1, hand2); }
            case THREEOFAKIND -> { return settleThreeOfAKindDraw(hand1, hand2); }
            case TWOPAIR -> { return settleTwoPairDraw(hand1, hand2); }
            case PAIR -> { return settlePairDraw(hand1, hand2); }
            case HIGHCARD -> { return settleHighCardDraw(hand1, hand2); }
            default -> { }
        }

        return 0;
    }

    private int hasStraightFlush(ArrayList<Card> hand) {
        if (hasStraight(hand) == 0 && hasFlush(hand) == 0)
            return 0;

        return -1;
    }

    private int hasFourOfAKind(ArrayList<Card> hand) {
        // Returns index of the first card of the four of a kind. If not found returns -1.
        for (int i = 0; i < hand.size() - 3; i++)
            if (hand.get(i).rank().ordinal() ==  hand.get(i + 1).rank().ordinal())
                if (hand.get(i + 1).rank().ordinal() ==  hand.get(i + 2).rank().ordinal())
                    if (hand.get(i + 2).rank().ordinal() ==  hand.get(i + 3).rank().ordinal())
                        return i;

        return -1;
    }

    private int hasFullHouse(ArrayList<Card> hand) {
        // Returns index of the first card of the three of a kind in full house. If not found returns -1.
        // Creating hand copy for the sake of simplicity. This way some cards can be safely removed from the hand;

        ArrayList<Card> handCopy = new ArrayList<>(hand);
        int indexOfThreeOfAKind = hasThreeOfAKind(handCopy);

        if (indexOfThreeOfAKind == -1)
            return -1;

        handCopy.remove(indexOfThreeOfAKind);
        handCopy.remove(indexOfThreeOfAKind);
        handCopy.remove(indexOfThreeOfAKind);

        if (hasPair(handCopy) == -1)
            return -1;

        return indexOfThreeOfAKind;
    }

    private int hasFlush(ArrayList<Card> hand) {
        // Returns index of the first card of the flush. If not found returns -1.
        // Creating hand copy for sake of simplicity. That way hand is always sorted by card ranks.
        ArrayList<Card> handCopy = new ArrayList<>(hand);

        CardSuitComparator cardSuitComparator = new CardSuitComparator();
        handCopy.sort(cardSuitComparator);

        for (int i = 0; i < handCopy.size() - 1; i++)
            if (handCopy.get(i).suit() != handCopy.get(i + 1).suit())
                return -1;

        return 0;
    }

    private int hasStraight(ArrayList<Card> hand) {
        // Returns index of the first card of the straight. If not found returns -1.

        // Ace can be treated as the lowest or the highest value card.
        if (hand.get(0).rank() == Card.Rank.ACE) {
            for (int i = 1; i < hand.size() - 1; i++)
                if (hand.get(i).rank().ordinal() - hand.get(i + 1).rank().ordinal() != 1)
                    return -1;

            if (hand.get(1).rank() == Card.Rank.KING || hand.get(hand.size() - 1).rank() == Card.Rank.DEUCE)
                return 0;
        }

        for (int i = 0; i < hand.size() - 1; i++)
            if (hand.get(i).rank().ordinal() - hand.get(i + 1).rank().ordinal() != 1)
                return -1;

        return 0;
    }

    private int hasThreeOfAKind(ArrayList<Card> hand) {
        // Returns index of the first card of the three of a kind. If not found returns -1.
        for (int i = 0; i < hand.size() - 2; i++)
            if (hand.get(i).rank().ordinal() ==  hand.get(i + 1).rank().ordinal())
                if (hand.get(i + 1).rank().ordinal() ==  hand.get(i + 2).rank().ordinal())
                    return i;

        return -1;
    }

    private int hasTwoPair(ArrayList<Card> hand) {
        // Returns index of the first card of the two pair. If not found returns -1.
        int firstPairIndex = hasPair(hand);

        if (firstPairIndex == -1)
            return -1;

        for (int i = firstPairIndex + 2; i < hand.size() - 1; i++)
            if (hand.get(i).rank().ordinal() == hand.get(i + 1).rank().ordinal())
                return firstPairIndex;

        return -1;
    }

    private int hasPair(ArrayList<Card> hand) {
        // Returns index of the first card of the pair. If not found returns -1.
        for (int i = 0; i < hand.size() - 1; i++)
            if (hand.get(i).rank().ordinal() ==  hand.get(i + 1).rank().ordinal())
                return i;

        return -1;
    }

    private int settleStraightOrFlushDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        return hand1.get(0).rank().compareTo(hand2.get(0).rank());
    }

    private int settleFourOfAKindDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with four of a kind (FOaK)
        int indexOfFOaK1 = hasFourOfAKind(hand1);
        int indexOfFOaK2 = hasFourOfAKind(hand2);

        Card.Rank fourOfAKindRank1 = hand1.get(indexOfFOaK1).rank();
        Card.Rank fourOfAKindRank2 = hand2.get(indexOfFOaK2).rank();

        return fourOfAKindRank1.compareTo(fourOfAKindRank2);
    }

    private int settleFullHouseDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with full house.
        // indexOfThreeOfAKind stands for index of the first card of three of a kind in a full house
        int indexOfThreeOfAKind1 = hasFullHouse(hand1);
        int indexOfThreeOfAKind2 = hasFullHouse(hand2);

        Card.Rank highCardRank1 = hand1.get(indexOfThreeOfAKind1).rank();
        Card.Rank highCardRank2 = hand2.get(indexOfThreeOfAKind2).rank();

        return highCardRank1.compareTo(highCardRank2);
    }

    private int settleThreeOfAKindDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with three of a kind.
        int indexOfThreeOfAKind1 = hasThreeOfAKind(hand1);
        int indexOfThreeOfAKind2 = hasThreeOfAKind(hand2);

        Card.Rank highCardRank1 = hand1.get(indexOfThreeOfAKind1).rank();
        Card.Rank highCardRank2 = hand2.get(indexOfThreeOfAKind2).rank();

        return highCardRank1.compareTo(highCardRank2);
    }

    private int settleTwoPairDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with two pairs.

        // Creating hands copy for sake of simplicity. That way is cards can be safely removed from hands.
        ArrayList<Card> handCopy1 = new ArrayList<>(hand1);
        ArrayList<Card> handCopy2 = new ArrayList<>(hand2);

        if (checkPairDraw(handCopy1, handCopy2) != 0)
            return checkPairDraw(handCopy1, handCopy2);

        if (checkPairDraw(handCopy1, handCopy2) != 0)
            return checkPairDraw(handCopy1, handCopy2);

        return handCopy1.get(0).rank().compareTo(handCopy2.get(0).rank());
    }

    private int settlePairDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with a pair.

        // Creating hands copy for sake of simplicity. That way is cards can be safely removed from hands.
        ArrayList<Card> handCopy1 = new ArrayList<>(hand1);
        ArrayList<Card> handCopy2 = new ArrayList<>(hand2);

        if (checkPairDraw(handCopy1, handCopy2) != 0)
            return checkPairDraw(handCopy1, handCopy2);

        return settleHighCardDraw(handCopy1, handCopy2);
    }

    private int checkPairDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Checks relation between the highest pairs in hands.

        // WARNING: this method removes cards form hands.
        // It is meant to be used only in settling a pair and two pair draws.

        int indexOfPair1 = hasPair(hand1);
        int indexOfPair2 = hasPair(hand2);

        Card.Rank pairRank1 = hand1.get(indexOfPair1).rank();
        Card.Rank pairRank2 = hand2.get(indexOfPair2).rank();

        hand1.remove(indexOfPair1);
        hand1.remove(indexOfPair1);
        hand2.remove(indexOfPair2);
        hand2.remove(indexOfPair2);

        return pairRank1.compareTo(pairRank2);
    }

    private int settleHighCardDraw(ArrayList<Card> hand1, ArrayList<Card> hand2) {
        // Settles a draw between two hands with only high card.

        // Since hands are sorted comparing cards at corresponding indices and finding those that don't match works.
        for (int i = 0; i < hand1.size(); i++)
            if (hand1.get(0).rank().compareTo(hand2.get(0).rank()) != 0)
                return hand1.get(0).rank().compareTo(hand2.get(0).rank());

        return 0;
    }
}
