package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.NotEnoughCreditException;

import java.util.ArrayList;

/**
 * Class representing player in the poker game.
 *
 * @author Kacper Cienkosz
 */
public class Player {
    private final ArrayList<Card> hand = new ArrayList<>();
    private final Integer id;
    private int currentBid = 0 ;
    private int allGameBid = 0;
    private boolean folded = false;
    private boolean bidden = false;
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
        allGameBid += bidValue;
        currentBid += bidValue;
    }

    /**
     * Method handles winning prize by the player.
     *
     * @param prizeValue Value of the prize won.
     */
    public void receivePrize(int prizeValue) {
        credit += prizeValue;
    }

    public ArrayList<Card> getHand() {
        hand.sort(new CardRankComparator());
        return hand;
    }

    public HandEvaluator.HandValues getHandEvaluation() {
        HandEvaluator handEvaluator = new HandEvaluator();
        return handEvaluator.evaluate(getHand());
    }

    /**
     * Clears players hand.
     */
    public void clearHand() { hand.clear(); }

    public int getCredit() { return credit; }

    public int getId() { return id; }

    @Override
    public int hashCode() {
        return id;
    }

    public void setCurrentBid(int currentBid) {
        this.currentBid = currentBid;
    }

    public int getCurrentBid() {
        return currentBid;
    }

    public int getAllGameBid() {
        return allGameBid;
    }

    public boolean isFolded() {
        return folded;
    }

    public void setFolded(boolean folded) {
        this.folded = folded;
    }

    public boolean hasBidden() {
        return bidden;
    }

    public void setBidden(boolean bidden) {
        this.bidden = bidden;
    }
}
