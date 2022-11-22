package pl.edu.agh.kis.pz1;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Deck deck = new Deck();
        ArrayList<Card> cards = new ArrayList<>(deck.deck());

        cards.sort(new CardRankComparator());

        test(cards);

        for (Card c : cards) {
            System.out.println(c);
        }

    }

    private static void test(ArrayList<Card> cards) {
        cards.sort(new CardSuitComparator());
        for (Card c : cards) {
            System.out.println(c);
        }
    }
}