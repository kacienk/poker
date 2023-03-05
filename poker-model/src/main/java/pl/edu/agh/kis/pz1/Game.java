package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Class implementing poker game.
 *
 * @author Kacper Cienkosz
 */
public class Game {
    //private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Integer, Player> players = new HashMap<>();
    private final ArrayList<Integer> playersToRemove = new ArrayList<>();
    private final ArrayList<Integer> biddingOrder = new ArrayList<>();
    private final Deck deck = new Deck();
    private final int numberOfPlayers;
    private final int id;
    private int ante = 5;
    private int stake = 0;
    private int currentNegotiationStake = 0;
    private int foldedPlayers = 0;
    private boolean gameOn = false;

    Game(int id, int numberOfPlayers) {
        this.id = id;
        this.numberOfPlayers = numberOfPlayers;
    }

    Game(int id, int numberOfPlayers, int ante) {
        this.id = id;
        this.numberOfPlayers = numberOfPlayers;
        this.ante = ante;
    }

    /**
     *  Adds new player to the game.
     *
     * @param playerId New player ID.
     * @throws IncorrectNumbersOfPlayersException See {@link IncorrectNumbersOfPlayersException}
     */
    public void newPlayer(Integer playerId) throws IncorrectNumbersOfPlayersException {
        if (players.size() >= numberOfPlayers)
            throw new IncorrectNumbersOfPlayersException("No other player can join this game.");

        players.put(playerId, new Player(playerId));
        biddingOrder.add(playerId);
    }

    /**
     * Removes player from the game.
     * If game is running waits for the game to end.
     *
     * @param playerId ID of the player to be removed.
     */
    public void removePlayer(Integer playerId) throws GameEndedByFoldingException {
        if (gameOn)
            fold(playerId);

        playersToRemove.add(playerId);
    }

    /**
     * Checks if the number of players is correct.
     *
     * @return <code>true</code> if correct, <code>false</code> otherwise.
     */
    public boolean canBegin() {
        return players.size() == numberOfPlayers;
    }

    /**
     * Sets up new game.
     * Resets all the values describing player state in the game like folded, bidden, currentBid, etc.
     *
     * @throws IncorrectNumbersOfPlayersException See {@link IncorrectNumbersOfPlayersException}
     * @throws NotEnoughCreditException See {@link NotEnoughCreditException}
     */
    public void newGame() throws IncorrectNumbersOfPlayersException, NotEnoughCreditException {
        if (players.size() < numberOfPlayers)
            throw new IncorrectNumbersOfPlayersException("There is too few players to start a game.");

        takeAnte();
        gameOn = true;
        moveDealer();

        for (Player player: players.values()) {
            player.setBidden(false);
            player.setFolded(false);
        }

        deck.newDeck();
        deck.shuffle();
        dealCards();
    }

    /**
     * Method returning HashMap where player IDs are keys and their hands i.e. ArrayList containing 5 cards.
     *
     * @return HashMap with players hands.
     */
    public HashMap<Integer, ArrayList<Card>> getPlayerHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = new HashMap<>();

        for (Player player: players.values())
            playerHands.put(player.getId(), player.getHand());

        return playerHands;
    }

    /**
     * Method handles discarding and drawing cards.
     *
     * @param playerId Player that want to draw a card.
     * @param cardsToDiscard ArrayList of indices of the cards that player wants to discard.
     * @throws IncorrectNumberOfCardsException See {@link IncorrectNumberOfCardsException}
     * @throws NoSuchCardException See {@link NoSuchCardException}
     */
    public void draw(int playerId, ArrayList<Integer> cardsToDiscard) throws IncorrectNumberOfCardsException, NoSuchCardException {
        if (cardsToDiscard.size() > 4)
            throw new IncorrectNumberOfCardsException("Player cannot discard more than 4 cards.");

        Player player = players.get(playerId);
        cardsToDiscard.sort(Integer::compareTo);

        for(Integer card: cardsToDiscard)
            if (card > 4)
                throw new NoSuchCardException("There is no such card that player is trying to discard " + card + ".");

        for (int i = 0; i < cardsToDiscard.size(); i++)
            player.discardCard(cardsToDiscard.get(i) - i);

        for (int i = 0; i < cardsToDiscard.size(); i++)
            player.receiveCard(deck.dealCard());
    }

    /**
     * Method handles bidding performed by player.
     *
     * @param playerId ID of the player that wants to bid.
     * @param bidValue Value of credits that player wants to bid.
     * @throws NotEnoughCreditException See {@link NotEnoughCreditException}
     * @throws TooSmallBidException See {@link TooSmallBidException}
     */
    public void bid(int playerId, int bidValue) throws NotEnoughCreditException, TooSmallBidException {
        Player player = players.get(playerId);

        if (currentNegotiationStake > bidValue + player.getCurrentBid())
            throw new TooSmallBidException("Player cannot bid less than current stake. Player have to bid at least " +
                    (currentNegotiationStake - player.getCurrentBid()) + ".");

        player.bid(bidValue);
        player.setBidden(true);

        if (currentNegotiationStake < player.getCurrentBid() + bidValue)
            currentNegotiationStake = player.getCurrentBid() + bidValue;

        stake += bidValue;
    }

    /**
     * Returns how much credits player needs to bid to even up the current stake.
     *
     * @param playerId ID of the player that want to perform this action.
     * @return credits value.
     */
    public int howMuchToBid(int playerId) {
        return currentNegotiationStake - players.get(playerId).getCurrentBid();
    }

    /**
     * Method handles folding performed by player.
     *
     * @param playerId ID of the player that want to fold.
     * @throws GameEndedByFoldingException See {@link GameEndedByFoldingException}
     */
    public void fold(int playerId) throws GameEndedByFoldingException {
        if (hasFolded(playerId))
            return;

        players.get(playerId).setFolded(true);
        foldedPlayers++;

        if (foldedPlayers + 1 == numberOfPlayers)
            throw new GameEndedByFoldingException("Only one player not folded.");
    }

    /**
     * Check if player has already folded.
     *
     * @param playerId ID of the player to be checked.
     * @return <code>true</code> if folded, <code>false</code> otherwise.
     */
    public boolean hasFolded(int playerId) {
        return players.get(playerId).isFolded();
    }

    /**
     * Method tries to finalize bidding process.
     * If bidding process can be finalized, then the method resets players bidden flags
     * and sets current negotiation stake to 0.
     *
     * @return <code>true</code> if over, <code>false</code> otherwise.
     */
    public boolean biddingOver() {
        for (Player player: players.values())
            if ((player.getCurrentBid() < currentNegotiationStake || !player.hasBidden()) && !player.isFolded())
                return false;

        for (Player player: players.values()) {
            player.setCurrentBid(0);
            player.setBidden(false);
        }

        currentNegotiationStake = 0;
        return true;
    }

    /**
     * Method handles giving winners their prize.
     *
     * @return Returns hash map where key is playerId and value the prize he won.
     */
    public HashMap<Integer, Integer> splitStakeBetweenWinners() {
        ArrayList<Integer> winners = createWinnersList();
        HashMap<Integer, Integer> playerPrizes = new HashMap<>();
        int prize = stake / winners.size();

        for (Integer playerId : winners) {
            players.get(playerId).receivePrize(prize);
            playerPrizes.put(playerId, prize);
        }

        gameOn = false;
        stake = 0;

        for (Integer id: playersToRemove)
            players.remove(id);

        for (Player player: players.values())
            player.clearHand();

        return playerPrizes;
    }

    /**
     * Evaluates player hand.
     *
     * @param playerId ID of the player, whose hand shall be evaluated.
     * @return Evaluation of the hand of the player with the given ID.
     */
    public HandEvaluator.HandValues getPlayerHandEvaluation(int playerId) {
        return players.get(playerId).getHandEvaluation();
    }

    public int getId() {
        return id;
    }

    /**
     * Returns bidding order that depends on the dealer.
     *
     * @return Bidding order.
     */
    public ArrayList<Integer> getBiddingOrder() {
        return biddingOrder;
    }

    /**
     * Get list of players participating in the game.
     *
     * @return Set of players' IDs.
     */
    public Set<Integer> getPlayers() {
        return players.keySet();
    }

    public int getPlayerCredit(int playerId) {
        return players.get(playerId).getCredit();
    }

    public int getCurrentNegotiationStake() {
        return currentNegotiationStake;
    }

    private void moveDealer() {
        biddingOrder.add(biddingOrder.remove(0));
    }

    private void takeAnte() throws NotEnoughCreditException {
        for (Player player: players.values()) {
            try {
                player.bid(ante);
            }
            catch (NotEnoughCreditException e) {
                throw new NotEnoughCreditException("One of the players does not have enough credits to start new game.");
            }

            stake += ante;
        }
    }

    private void dealCards() {
        int numberOfCards = 5;

        for (int i = 0; i < numberOfCards; i++)
            for (Integer id: getBiddingOrder()) {
                Player player = players.get(id);
                player.receiveCard(deck.dealCard());
            }

    }

    private ArrayList<Integer> createRanking() {
        // Create ranking of players' hand values storing their ids. Descending order.
        ArrayList<Integer> ranking = new ArrayList<>(players.keySet());
        HandEvaluator handEvaluator = new HandEvaluator();
        ranking.sort((player1, player2) -> -handEvaluator.compareHands(players.get(player1).getHand(), players.get(player2).getHand()));

        return ranking;
    }

    private ArrayList<Integer> createWinnersList() {
        ArrayList<Integer> winners = new ArrayList<>();
        ArrayList<Integer> ranking = createRanking();
        HandEvaluator handEvaluator = new HandEvaluator();

        for (Integer id : ranking)
            if (!hasFolded(id)) {
                winners.add(id);
                break;
            }

        for (int i = 0; i < ranking.size() - 1; i++) {
            ArrayList<Card> hand1 = players.get(ranking.get(i)).getHand();
            ArrayList<Card> hand2 = players.get(ranking.get(i + 1)).getHand();

            if (handEvaluator.compareHands(hand1, hand2) != 0)
                break;

            if (!hasFolded(ranking.get(i + 1)))
                winners.add(ranking.get(i + 1));
        }

        return winners;
    }
}
