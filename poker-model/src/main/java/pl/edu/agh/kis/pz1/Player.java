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

    /**
     * Adds card to hand.
     *
     * @param card Card to be added.
     */
    public void receiveCard(Card card) {
        hand.add(card);
    }

    /**
     * Removes card from hand.
     * @param index Index of the card to be removed.
     */
    public void discardCard(int index) {
        hand.remove(index);
    }

    /**
     * Handles player bidding.
     *
     * @param bidValue Value of credits that player wants to bid.
     * @throws NotEnoughCreditException See {@link  pl.edu.agh.kis.pz1.exceptions.NoSuchCardException}
     */
    public void bid(int bidValue) throws NotEnoughCreditException {
        if (bidValue > credit)
            throw new NotEnoughCreditException("Player has less credit than they want to bid. Player credit:" + credit);

        credit -= bidValue;
    }

    /**
     * Method handles winning prize by the player.
     *
     * @param prizeValue Value of the prize won.
     */
    public void getPrize(int prizeValue) {
        credit += prizeValue;
    }

    public ArrayList<Card> getHand() { return hand; }

    public int getId() { return id; }

    /**
     * Clears players hand.
     */
    public void clearHand() { hand.clear(); }

    public int getCredit() { return credit; }

    @Override
    public int hashCode() {
        return id;
    }
}
