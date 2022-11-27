package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Player, Integer> currentBids = new HashMap<>();
    private final HashMap<Player, Integer> allGameBids = new HashMap<>();
    private final HashMap<Player, Boolean> folded = new HashMap<>();
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

    public ArrayList<Integer> getPlayerIds() {
        ArrayList<Integer> ids = new ArrayList<>();

        for (Player player: players)
            ids.add(player.getId());

        return ids;
    }

    public void newPlayer(Integer id) throws IncorrectNumbersOfPlayersException {
        if (players.size() >= numberOfPlayers)
            throw new IncorrectNumbersOfPlayersException("No other player can join this game.");

        Player player = new Player(id);
        players.add(player);
        currentBids.put(player, 0);
        allGameBids.put(player, 0);
        folded.put(player, false);

        if (dealerId == -1)
            dealerId = id;
    }

    public void removePlayer(Integer id) {
        Player player = players.get(playerIndexFromId(id));

        if (gameOn) {
            playersToRemove.add(id);
            folded.put(player, true);
        }

        currentBids.remove(player);
        allGameBids.remove(player);
        folded.remove(player);
        players.remove(playerIndexFromId(id));
    }

    public void newGame() throws IncorrectNumbersOfPlayersException, NotEnoughCreditException {
        if (players.size() < numberOfPlayers)
            throw new IncorrectNumbersOfPlayersException("There is too few players to start a game.");

        takeAnte();

        gameOn = true;

        moveDealer();
        folded.replaceAll((p, v) -> false);
        deck.newDeck();
        dealCards();
    }

    public HashMap<Integer, ArrayList<Card>> getPlayerHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = new HashMap<>();

        for (Player player: players)
            playerHands.put(player.getId(), player.getHand());

        return playerHands;
    }

    public void draw(int playerId, ArrayList<Integer> cardsToDiscard) throws IncorrectNumberOfCardsException, NoSuchCardException {
        if (cardsToDiscard.size() > 4)
            throw new IncorrectNumberOfCardsException("Player cannot discard more than 4 cards.");

        Player player = players.get(playerIndexFromId(playerId));
        cardsToDiscard.sort(Integer::compareTo);

        for(Integer card: cardsToDiscard)
            if (card > 4)
                throw new NoSuchCardException("There is no such card that player is trying to discard.");

        for (int i = 0; i < cardsToDiscard.size(); i++)
            player.discardCard(cardsToDiscard.get(i) - i);

        for (int i = 0; i < cardsToDiscard.size(); i++)
            player.receiveCard(deck.dealCard());
    }

    public void endNegotiation() {
        for (Player player: players)
            currentBids.put(player, 0);

        currentNegotiationStake = 0;
    }

    public int getCurrentNegotiationStake() {
        return currentNegotiationStake;
    }

    public void bid(int playerId, int bidValue) throws NotEnoughCreditException, TooSmallBidException {
        Player player = players.get(playerIndexFromId(playerId));

        if (currentNegotiationStake > bidValue + currentBids.get(player))
            throw new TooSmallBidException("Player cannot bid less than current stake. Player have to bid at least" +
                    (currentNegotiationStake - currentBids.get(player)) + ".");

        player.bid(bidValue);

        if (currentNegotiationStake < bidValue)
            currentNegotiationStake = bidValue;

        currentBids.put(player, bidValue);
        allGameBids.put(player, bidValue);
    }

    public int howMuchToBid(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        return currentNegotiationStake - currentBids.get(player);
    }

    public void fold(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        folded.put(player, true);
    }

    public boolean hasFolded(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        return folded.get(player);
    }

    public boolean biddingOver() {
        for (Player player: players)
            if (currentBids.get(player) < currentNegotiationStake && !folded.get(player))
                return false;

        return true;
    }

    public void splitStakeBetweenWinners() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = evaluateHands();
        ArrayList<Integer> ranking = createRanking(handValues);
        ArrayList<Integer> winners = createWinnersList(ranking, handValues);

        for (Player player: players)
            if (winners.contains(player.getId()))
                player.getPrize(stake / winners.size());

        gameOn = false;
        for (Integer id: playersToRemove)
            removePlayer(id);
    }

    public HashMap<Integer, HandEvaluator.HandValues> evaluateHands() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = new HashMap<>();
        HandEvaluator handEvaluator = new HandEvaluator();

        for (Player player: players)
            handValues.put(player.getId(), handEvaluator.evaluate(player.getHand()));

        return handValues;
    }

    public ArrayList<Integer> getBiddingOrder() {
        ArrayList<Integer> biddingOrder = new ArrayList<>();
        int dealerIndex = playerIndexFromId(dealerId);

        for (int i = 0; i < players.size(); i++)
            biddingOrder.add(players.get((dealerIndex + i + 1) % players.size()).getId());

        return biddingOrder;
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

        for (int i = 0; i < numberOfCards; i++)
            for (Player player: players)
                player.receiveCard(deck.dealCard());
    }

    private int playerIndexFromId(int id) {
        for (int i = 0; i < players.size(); i++)
            if (players.get(i).getId() == id)
                return i;

        return -1;
    }

    private ArrayList<Integer> createRanking(HashMap<Integer, HandEvaluator.HandValues> handValues) {
        // Create ranking of players' hand values storing their ids. Descending order.

        ArrayList<Integer> ranking = new ArrayList<>();
        HandEvaluator handEvaluator = new HandEvaluator();

        for (Integer key: handValues.keySet()) {
            if (ranking.size() == 0) {
                ranking.add(key);
                continue;
            }

            for (int i = 0; i < ranking.size(); i++) {
                Integer playerToCompareKey = ranking.get(i);

                if (handValues.get(playerToCompareKey).compareTo(handValues.get(key)) < 0) {
                    ranking.add(i, key);
                    break;
                }
            }
        }

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
        winners.add(ranking.get(0));

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
}
