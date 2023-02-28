package pl.edu.agh.kis.pz1;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckTest {
    private Deck deck;

    @BeforeEach
    public void setUp() {
        deck = new Deck();
    }

    @Test
    public void givenNothing_whenDeckIsConstructed_thenInitializeDeckAttributeWithNewDeck() {
        assertArrayEquals(deck.deck().toArray(), Card.newDeck().toArray());
    }

    @Test
    public void givenNothing_whenNewDeck_thenInitializeDeckAttributeWithNewDeck() {
        deck.newDeck();
        assertArrayEquals(deck.deck().toArray(), Card.newDeck().toArray());
    }

    @Test
    public void givenNothing_whenDealCard_thenReturnFirstCardFromDeck() {
        deck.shuffle();
        Card expectedValue = deck.deck().get(0);
        int expectedDeckSize = deck.deck().size() - 1;
        Card actualValue = deck.dealCard();
        int actualDeckSize = deck.deck().size();

        assertEquals(expectedValue, actualValue);
        assertEquals(expectedDeckSize, actualDeckSize);
    }
}