package pl.edu.agh.kis.pz1;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Deck deck = new Deck();

        Card card = deck.dealCard();

        System.out.println(card);
        System.out.println();
        for (Card c: deck.deck()) {
            System.out.println(c);
        }
    }
}