package pl.edu.agh.kis.pz1;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest {
    @Test
    public void givenNothing_whenNewDeck_thenReturnProperNewDeck() {
        ArrayList<Card> deck = Card.newDeck();

        for (Card.Suit suit: Card.Suit.values())  {
            for (Card.Rank rank: Card.Rank.values()) {
                int cardIndex = (suit.ordinal() * 13) + rank.ordinal();
                Card testedCard = deck.get(cardIndex);

                assertEquals(testedCard.rank(), rank);
                assertEquals(testedCard.suit(), suit);
            }
        }
    }
}