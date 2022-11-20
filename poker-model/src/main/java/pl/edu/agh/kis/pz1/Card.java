package pl.edu.agh.kis.pz1;

public class Card  {
    enum CardRank {
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING
    }

    enum CardSuit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES
    }

    CardRank rank;
    CardSuit suit;

    Card(CardRank rank, CardSuit suit) {
        this.rank = rank;
        this.suit = suit;
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

