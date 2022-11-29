package pl.edu.agh.kis.pz1;

import java.util.ArrayList;
import java.util.List;

public class Card  {
    /**
     * Card ranks.
     */
    public enum Rank{ DEUCE, THREE, FOUR, FIVE, SIX,SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }

    /**
     * Card suits.
     */
    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    private final Rank rank;
    private final Suit suit;

    private static final List<Card> protoDeck = new ArrayList<>();

    static {
        for (Suit suit: Suit.values())  {
            for (Rank rank: Rank.values()) {
                protoDeck.add(new Card(rank, suit));
            }
        }
    }

    private Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank rank() { return rank; }
    public Suit suit() { return suit; }

    public String toString() {
        return rank + " of " + suit;
    }

    /**
     * Returns copy of statically created protoDeck.
     *
     * @return Deck.
     */
    public static ArrayList<Card> newDeck() {
        return new ArrayList<>(protoDeck);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Card c = (Card) o;

        if (!rank.equals(c.rank))
            return false;

        return suit.equals(c.suit);
    }

    @Override
    public int hashCode() {
        int result = rank.hashCode();
        result = 31 * result + suit.hashCode();

        return result;
    }
}

