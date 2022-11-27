package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.NotEnoughCreditException;

import java.util.ArrayList;

public class Player {
    private final ArrayList<Card> hand = new ArrayList<>();
    private final Integer id;
    private int credit = 1000;

    Player(int id) {
        this.id = id;
    }

    Player(int id, int credit) {
        this.id = id;
        this.credit = credit;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void discardCard(int index) {
        hand.remove(index);
    }

    public void bid(int bidValue) throws NotEnoughCreditException {
        if (bidValue > credit)
            throw new NotEnoughCreditException("Player has less credit than they want to bid. Player credit:" + credit);

        credit -= bidValue;
    }

    public void getPrize(int prizeValue) {
        credit += prizeValue;
    }

    public ArrayList<Card> getHand() { return hand; }

    public int getId() { return id; }

    public int getCredit() { return credit; }

    @Override
    public int hashCode() {
        return id;
    }
}
