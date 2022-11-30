package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class implementing poker game.
 *
 * @author Kacper Cienkosz
 */
public class Game {
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Player, Integer> currentBids = new HashMap<>();
    private final HashMap<Player, Integer> allGameBids = new HashMap<>();
    private final HashMap<Player, Boolean> folded = new HashMap<>();
    private final HashMap<Player, Boolean> bidden = new HashMap<>();
    private final ArrayList<Integer> playersToRemove = new ArrayList<>();
    private final Deck deck = new Deck();
    private final int numberOfPlayers;
    private final int id;
    private int ante = 5;
    private int stake = 0;
    private int currentNegotiationStake = 0;
    private boolean gameOn = false;

    private int dealerId = -1;


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
     * @param id New player ID.
     * @throws IncorrectNumbersOfPlayersException See {@link IncorrectNumbersOfPlayersException}
     */
    public void newPlayer(Integer id) throws IncorrectNumbersOfPlayersException {
        if (players.size() >= numberOfPlayers)
            throw new IncorrectNumbersOfPlayersException("No other player can join this game.");

        Player player = new Player(id);
        players.add(player);
        currentBids.put(player, 0);
        allGameBids.put(player, 0);
        folded.put(player, false);
        bidden.put(player, false);

        if (dealerId == -1)
            dealerId = id;
    }

    /**
     * Removes player from the game.
     * If game is running waits for the game to end.
     *
     * @param id ID of the player to be removed.
     */
    public void removePlayer(Integer id) {
        Player player = players.get(playerIndexFromId(id));

        if (gameOn) {
            playersToRemove.add(id);
            folded.put(player, true);
            return;
        }

        currentBids.remove(player);
        allGameBids.remove(player);
        folded.remove(player);
        players.remove(playerIndexFromId(id));
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
     * Resets all the values like folded, bidden, currentBids, etc.
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
        folded.replaceAll((p, v) -> false);
        bidden.replaceAll((p, v) -> false);
        deck.newDeck();
        deck.shuffle();
        dealCards();
    }

    public HashMap<Integer, ArrayList<Card>> getPlayerHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = new HashMap<>();

        for (Player player: players)
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

        Player player = players.get(playerIndexFromId(playerId));
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
        Player player = players.get(playerIndexFromId(playerId));

        if (currentNegotiationStake > bidValue + currentBids.get(player))
            throw new TooSmallBidException("Player cannot bid less than current stake. Player have to bid at least " +
                    (currentNegotiationStake - currentBids.get(player)) + ".");

        player.bid(bidValue);

        if (currentNegotiationStake < currentBids.get(player) + bidValue)
            currentNegotiationStake = currentBids.get(player) + bidValue;

        stake += bidValue;
        currentBids.put(player, currentBids.get(player) + bidValue);
        allGameBids.put(player, allGameBids.get(player) + bidValue);
        bidden.put(player, true);
    }

    /**
     * Returns how much credits player needs to bid to even up the current stake.
     *
     * @param playerId ID of the player that want to perform this action.
     * @return credits value.
     */
    public int howMuchToBid(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        return currentNegotiationStake - currentBids.get(player);
    }

    /**
     * Method handles folding performed by player.
     *
     * @param playerId ID of the player that want to fold.
     * @throws GameEndedByFoldingException See {@link GameEndedByFoldingException}
     */
    public void fold(int playerId) throws GameEndedByFoldingException {
        Player player = players.get(playerIndexFromId(playerId));

        folded.put(player, true);

        if (countFoldedPlayers() + 1 == numberOfPlayers)
            throw new GameEndedByFoldingException("Only one player not folded.");
    }

    /**
     * Check if player has already folded.
     *
     * @param playerId ID of the player to be checked.
     * @return <code>true</code> if folded, <code>false</code> otherwise.
     */
    public boolean hasFolded(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        return folded.get(player);
    }

    /**
     * Checks if bidding is over.
     *
     * @return <code>true</code> if over, <code>false</code> otherwise.
     */
    public boolean biddingOver() {
        for (Player player: players)
            if ((currentBids.get(player) < currentNegotiationStake || !bidden.get(player)) && !folded.get(player))
                return false;


        currentBids.replaceAll((p, v) -> 0);
        bidden.replaceAll((p, v) -> false);
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

        if (countFoldedPlayers() + 1 == numberOfPlayers) {
            for (Player player : players) {
                if (!folded.get(player)) {
                    player.getPrize(stake);
                    playerPrizes.put(player.getId(), stake);
                }
            }
        }
        else {
            for (Player player : players) {
                if (winners.contains(player.getId()) && !folded.get(player)) {
                    player.getPrize(stake / winners.size());
                    playerPrizes.put(player.getId(), stake / winners.size());
                }
            }
        }

        gameOn = false;
        stake = 0;

        for (Integer id: playersToRemove)
            removePlayer(id);

        for (Player player: players)
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
        HandEvaluator handEvaluator = new HandEvaluator();

        for (Player player: players)
            handValues.put(player.getId(), handEvaluator.evaluate(player.getHand()));

        return handValues;
    }

    /**
     * Creates and returns bidding order that depends on the dealer.
     *
     * @return Bidding order.
     */
    public ArrayList<Integer> getBiddingOrder() {
        ArrayList<Integer> biddingOrder = new ArrayList<>();
        int dealerIndex = playerIndexFromId(dealerId);

        for (int i = 0; i < players.size(); i++)
            biddingOrder.add(players.get((dealerIndex + i + 1) % players.size()).getId());

        return biddingOrder;
    }

    public int getPlayerCredit(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        return player.getCredit();
    }

    private void moveDealer() {
        int dealerIndex = playerIndexFromId(dealerId);

        dealerId = players.get((dealerIndex + 1) % players.size()).getId();
    }

    private void takeAnte() throws NotEnoughCreditException {
        for (Player player: players) {
            try {
                player.bid(ante);
            }
            catch (NotEnoughCreditException e) {
                throw new NotEnoughCreditException("One of the players does not have enough credits to start new game.");
            }

            allGameBids.put(player, ante);
            stake += ante;
        }
    }

    private void dealCards() {
        int numberOfCards = 5;
        ArrayList<Integer> biddingOrder = getBiddingOrder();

        for (int i = 0; i < numberOfCards; i++)
            for (Integer playerId: biddingOrder) {
                Player player = players.get(playerIndexFromId(playerId));
                player.receiveCard(deck.dealCard());
            }

    }

    private int playerIndexFromId(int id) {
        for (int i = 0; i < players.size(); i++)
            if (players.get(i).getId() == id)
                return i;

        return -1;
    }

    private ArrayList<Integer> createRanking(HashMap<Integer, HandEvaluator.HandValues> handValues) {
        // Create ranking of players' hand values storing their ids. Descending order.

        ArrayList<Integer> ranking = initializeRanking(handValues);
        HandEvaluator handEvaluator = new HandEvaluator();

        Integer leaderId = ranking.get(0);
        Player leadingPlayer = players.get(playerIndexFromId(leaderId));
        HandEvaluator.HandValues leaderHandValue = handValues.get(leaderId);

        // Order ranking by settling draws between hands with the same hand value.
        for (int i = 1; i < ranking.size(); i++) {
            Integer currentlyCheckedId = ranking.get(i);
            Player currentlyCheckedPlayer = players.get(playerIndexFromId(currentlyCheckedId));
            HandEvaluator.HandValues currentlyCheckedHandValue = handValues.get(currentlyCheckedId);

            if (leaderHandValue == currentlyCheckedHandValue)

                // If currently checked players hand is at least as good as current leader.
                if (handEvaluator.settleDraw(leadingPlayer.getHand(), currentlyCheckedPlayer.getHand(), leaderHandValue) <= 0) {
                    // Make currently checked player the leader.
                    ranking.add(0, ranking.remove(i));

                    leaderId = ranking.get(0);
                    leadingPlayer = players.get(playerIndexFromId(leaderId));
                    leaderHandValue = handValues.get(leaderId);
                }
        }

        return ranking;
    }

    private ArrayList<Integer> createWinnersList(ArrayList<Integer> ranking, HashMap<Integer, HandEvaluator.HandValues> handValues) {
        // Return number of draws that were irresolvable.
        HandEvaluator handEvaluator = new HandEvaluator();
        ArrayList<Integer> winners = new ArrayList<>();

        for (Integer id : ranking)
            if (!hasFolded(id)) {
                winners.add(id);
                break;
            }

        for (int i = 0; i < ranking.size() - 1; i++)
        {
            if (handValues.get(ranking.get(i + 1)) == handValues.get(ranking.get(i))) {
                ArrayList<Card> hand1 = players.get(playerIndexFromId(ranking.get(i))).getHand();
                ArrayList<Card> hand2 = players.get(playerIndexFromId(ranking.get(i + 1))).getHand();

                if (handEvaluator.settleDraw(hand1, hand2, handValues.get(ranking.get(i))) == 0)
                    winners.add(ranking.get(i + 1));
                else
                    return winners;
            }
            else
                return winners;
        }

        return winners;
    }

    private int countFoldedPlayers() {
        int countFolded = 0;

        for (Player player1: folded.keySet())
            if(hasFolded(player1.getId()))
                countFolded++;

        return countFolded;
    }

    private void endNegotiation() {
        for (Player player: players)
            currentBids.put(player, 0);

        currentNegotiationStake = 0;
    }

    private ArrayList<Integer> initializeRanking(HashMap<Integer, HandEvaluator.HandValues> handValues) {
        ArrayList<Integer> ranking = new ArrayList<>();

        for (Integer key: handValues.keySet()) {
            if (ranking.size() == 0) {
                ranking.add(key);
                continue;
            }

            for (int i = 0; i < ranking.size(); i++) {
                Integer playerToCompareKey = ranking.get(i);

                if (handValues.get(playerToCompareKey).compareTo(handValues.get(key)) < 0)
                    if (!ranking.contains(key))
                        ranking.add(i, key);
            }

            if (!ranking.contains(key))
                ranking.add(key);
        }

        return ranking;
    }
}
