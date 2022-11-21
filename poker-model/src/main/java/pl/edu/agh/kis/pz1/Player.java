package pl.edu.agh.kis.pz1;

import java.util.ArrayList;

public class Player {
    private ArrayList<Card> cards = new ArrayList<>();


    public void addCard(Card card) {
        cards.add(card);
    }

    public ArrayList<Card> cards() { return cards; }
}
