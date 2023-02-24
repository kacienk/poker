package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

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

    public int getId() {
        return id;
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

    public int getCurrentNegotiationStake() {
        return currentNegotiationStake;
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
     * Checks if bidding is over.
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

        endNegotiation();
        return true;
    }

    /**
     * Method handles giving winners their prize.
     *
     * @return Returns hash map where key is playerId and value the prize he won.
     */
    public HashMap<Integer, Integer> splitStakeBetweenWinners() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = evaluateHands();
        ArrayList<Integer> ranking = createRanking(handValues);
        ArrayList<Integer> winners = createWinnersList(ranking, handValues);
        HashMap<Integer, Integer> playerPrizes = new HashMap<>();

        if (foldedPlayers + 1 == numberOfPlayers) {
            for (Player player : players.values()) {
                if (!player.isFolded()) {
                    player.getPrize(stake);
                    playerPrizes.put(player.getId(), stake);
                }
            }
        }
        else {
            for (Player player : players.values()) {
                if (winners.contains(player.getId()) && !player.isFolded()) {
                    player.getPrize(stake / winners.size());
                    playerPrizes.put(player.getId(), stake / winners.size());
                }
            }
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
     * Evaluates players hands.
     *
     * @return HashMap map where key is playerId and value the evaluation.
     */
    public HashMap<Integer, HandEvaluator.HandValues> evaluateHands() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = new HashMap<>();

        for (Player player: players.values())
            handValues.put(player.getId(), player.getHandEvaluation());

        return handValues;
    }

    /**
     * Creates and returns bidding order that depends on the dealer.
     *
     * @return Bidding order.
     */
    public ArrayList<Integer> getBiddingOrder() {
        return biddingOrder;
    }

    public int getPlayerCredit(int playerId) {
        return players.get(playerId).getCredit();
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

    private ArrayList<Integer> createRanking(HashMap<Integer, HandEvaluator.HandValues> handValues) {
        // Create ranking of players' hand values storing their ids. Descending order.

        ArrayList<Integer> ranking = initializeRanking(handValues);

        Integer leaderId = ranking.get(0);
        Player leadingPlayer = players.get(leaderId);
        HandEvaluator.HandValues leaderHandValue = handValues.get(leaderId);

        // Order ranking by settling draws between hands with the same hand value.
        for (int i = 1; i < ranking.size(); i++) {
            Integer currentlyCheckedId = ranking.get(i);
            Player currentlyCheckedPlayer = players.get(currentlyCheckedId);
            HandEvaluator.HandValues currentlyCheckedHandValue = handValues.get(currentlyCheckedId);

            if (leaderHandValue == currentlyCheckedHandValue)

                // If currently checked players hand is at least as good as current leader.
                if (HandEvaluator.settleDraw(leadingPlayer.getHand(), currentlyCheckedPlayer.getHand(), leaderHandValue) <= 0) {
                    // Make currently checked player the leader.
                    ranking.add(0, ranking.remove(i));

                    leaderId = ranking.get(0);
                    leadingPlayer = players.get(leaderId);
                    leaderHandValue = handValues.get(leaderId);
                }
        }

        return ranking;
    }

    private ArrayList<Integer> createWinnersList(ArrayList<Integer> ranking, HashMap<Integer, HandEvaluator.HandValues> handValues) {
        // Return number of draws that were irresolvable.
        ArrayList<Integer> winners = new ArrayList<>();

        for (Integer id : ranking)
            if (!hasFolded(id)) {
                winners.add(id);
                break;
            }

        for (int i = 0; i < ranking.size() - 1; i++)
        {
            if (handValues.get(ranking.get(i + 1)) == handValues.get(ranking.get(i))) {
                ArrayList<Card> hand1 = players.get(ranking.get(i)).getHand();
                ArrayList<Card> hand2 = players.get(ranking.get(i + 1)).getHand();

                if (HandEvaluator.settleDraw(hand1, hand2, handValues.get(ranking.get(i))) == 0)
                    winners.add(ranking.get(i + 1));
                else
                    return winners;
            }
            else
                return winners;
        }

        return winners;
    }

    private void endNegotiation() {
        for (Player player: players.values())
            player.setCurrentBid(0);

        currentNegotiationStake = 0;
    }

    private ArrayList<Integer> initializeRanking(HashMap<Integer, HandEvaluator.HandValues> handValues) {
        ArrayList<Integer> ranking = new ArrayList<>(handValues.keySet());
        ranking.sort(Comparator.comparing(handValues::get, Comparator.reverseOrder()));

        return ranking;
    }
}
