package pl.edu.agh.kis.pz1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class HandEvaluatorTest {
    private HandEvaluator underTest;
    // Correct behaviour of deck is assumed
    private Deck testDeck;

    @BeforeEach
    void setUp() {
        underTest = new HandEvaluator();
        testDeck = new Deck();
    }

    @Test
    void givenSortedHandWithHighStraightFlush_whenEvaluate_thenReturnStraightFlush(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.QUEEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.TEN);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.STRAIGHTFLUSH;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithHighStraightFlush_whenEvaluate_thenReturnStraightFlush(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.QUEEN);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.STRAIGHTFLUSH;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithLowStraightFlush_whenEvaluate_thenReturnStraightFlush(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.THREE);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.FIVE);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.FOUR);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.DEUCE);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.STRAIGHTFLUSH;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithFourOfAKind_whenEvaluate_thenReturnFourOfAKind(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.EIGHT);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.EIGHT);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.EIGHT);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.EIGHT);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.FOUROFAKIND;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithFourOfAKind_whenEvaluate_thenReturnFourOfAKind(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.FOUROFAKIND;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithFullHouse_whenEvaluate_thenReturnFullHouse(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.EIGHT);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.EIGHT);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.FULLHOUSE;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithFullHouse_whenEvaluate_thenReturnFullHouse(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.DEUCE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.ACE);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.FULLHOUSE;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenHandWithFlush_whenEvaluate_thenReturnFlush(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.EIGHT);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.FLUSH;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithStraight_whenEvaluate_thenReturnStraight(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.EIGHT);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.STRAIGHT;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithStraight_whenEvaluate_thenReturnStraight(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.EIGHT);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.STRAIGHT;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithThreeOfAKind_whenEvaluate_thenReturnThreeOfAKind(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.THREEOFAKIND;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithThreeOfAKind_whenEvaluate_thenReturnThreeOfAKind(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.TEN);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.THREEOFAKIND;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithTwoPair_whenEvaluate_thenReturnTwoPair(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.TWOPAIR;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithTwoPair_whenEvaluate_thenReturnTwoPair(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.ACE);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.TWOPAIR;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithPair_whenEvaluate_thenReturnPair(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.PAIR;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithPair_whenEvaluate_thenReturnPair(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.PAIR;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenSortedHandWithHighCard_whenEvaluate_thenReturnHighCard(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.HIGHCARD;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenUnsortedHandWithHighCard_whenEvaluate_thenReturnHighCard(){
        ArrayList<Card> hand = new ArrayList<>();
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand, Card.Suit.HEARTS, Card.Rank.JACK);

        HandEvaluator.HandValues expectedValue = HandEvaluator.HandValues.HIGHCARD;
        HandEvaluator.HandValues actualValue = underTest.evaluate(hand);

        assertEquals(expectedValue, actualValue);
    }


    @Test
    void givenHandsWithStraightFlushAndFourOfAKind_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.FIVE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.FOUR);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.DEUCE);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithFourOfAKindAndFullHouse_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithFullHouseAndFlush_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithFlushAndStraight_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithStraightAndThreeOfAKind_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithThreeOfAKindAndTwoPairs_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithTwoPairsAndPair_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenHandsWithPairAndHighCard_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }


    @Test
    void givenHandsWithFourOfAKindAndStraightFlush_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.FIVE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.FOUR);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.DEUCE);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithFullHouseAndFourOfAKind_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithFlushAndFullHouse_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithStraightAndFlush_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithThreeOfAKindAndStraight_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithTwoPairsAndThreeOfAKind_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithPairAndTwoPairs_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenHandsWithHighCardAndPair_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }


    @Test
    void givenTwoHandsWithStraightFlushLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.EIGHT);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FIVE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FOUR);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithFourOfAKindLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SIX);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithFullHouseLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithFlushLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FOUR);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithStraightLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.QUEEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FIVE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithThreeOfAKindLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.NINE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithTwoPairsLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithPairLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithHighCardLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }


    @Test
    void givenTwoHandsWithStraightFlushRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.EIGHT);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FIVE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FOUR);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithFourOfAKindRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SIX);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithFullHouseRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithFlushRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FOUR);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithStraightRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.QUEEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FIVE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithThreeOfAKindRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.NINE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithTwoPairsRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithPairRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithHighCardRightHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        System.out.println(underTest.compareHands(hand1, hand2));

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }


    @Test
    void givenTwoHandsWithTwoPairsOfTheSameValueLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithPaiOfTheSameValueLeftHigher_whenCompareHands_thenReturnPositive() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.QUEEN);

        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand1, hand2) > 0);
    }

    @Test
    void givenTwoHandsWithTwoPairsOfTheSameValueLeftHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.ACE);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }

    @Test
    void givenTwoHandsWithPaiOfTheSameValueLeftHigher_whenCompareHands_thenReturnNegative() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.QUEEN);

        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertTrue(underTest.compareHands(hand2, hand1) < 0);
    }


    @Test
    void givenTwoHandsWithStraightFlushIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.EIGHT);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.TEN);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.EIGHT);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.TEN);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    @Test
    void givenTwoHandsWithFlushIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.DEUCE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.THREE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.EIGHT);

        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.DEUCE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.THREE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.EIGHT);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    @Test
    void givenTwoHandsWithStraightIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.FIVE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.EIGHT);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SIX);

        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.FIVE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.EIGHT);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    @Test
    void givenTwoHandsWithTwoPairsIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.TEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.CLUBS, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.TEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.ACE);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    @Test
    void givenTwoHandsWithPairIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.KING);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.KING);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    @Test
    void givenTwoHandsWithHighCardIndistinguishable_whenCompareHands_thenReturnZero() {
        ArrayList<Card> hand1 = new ArrayList<>();
        ArrayList<Card> hand2 = new ArrayList<>();

        addCardToHand(hand1, Card.Suit.HEARTS, Card.Rank.SIX);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.SEVEN);
        addCardToHand(hand1, Card.Suit.SPADES, Card.Rank.NINE);
        addCardToHand(hand1, Card.Suit.CLUBS, Card.Rank.ACE);
        addCardToHand(hand1, Card.Suit.DIAMONDS, Card.Rank.JACK);

        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SIX);
        addCardToHand(hand2, Card.Suit.SPADES, Card.Rank.SEVEN);
        addCardToHand(hand2, Card.Suit.DIAMONDS, Card.Rank.NINE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.ACE);
        addCardToHand(hand2, Card.Suit.HEARTS, Card.Rank.JACK);

        assertEquals(0, underTest.compareHands(hand1, hand2));
    }

    private void addCardToHand(ArrayList<Card> hand, Card.Suit suit, Card.Rank rank) {
        int index = (suit.ordinal() * 13) + rank.ordinal();
        hand.add(testDeck.deck().get(index));
    }
}