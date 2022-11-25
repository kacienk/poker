package pl.edu.agh.kis.pz1;

import java.util.ArrayList;
import java.util.HashMap;

public class Game {
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<Player, Integer> currentBids = new HashMap<>();
    private final HashMap<Player, Integer> allGameBids = new HashMap<>();
    private final HashMap<Player, Boolean> folded = new HashMap<>();
    private final Deck deck = new Deck();
    private int numberOfPlayers = 2;
    private int ante = 5;
    private int stake = 0;
    private int currentNegotiationStake = 0;


    Game(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    Game(int numberOfPlayers, int ante) {
        this.numberOfPlayers = numberOfPlayers;
        this.ante = ante;
    }

    public void newPlayer(Integer id) throws Exception {
        if (players.size() >= numberOfPlayers)
            throw new Exception();

        Player player = new Player(id);
        players.add(player);
        currentBids.put(player, 0);
        allGameBids.put(player, 0);
        folded.put(player, false);
    }

    public void newGame() throws Exception {
        if (players.size() < numberOfPlayers)
            throw new Exception();

        takeAnte();

        deck.newDeck();
        dealCards();
    }

    public void draw(int playerId, ArrayList<Integer> cardsToDiscard) throws Exception {
        if (cardsToDiscard.size() > 4)
            throw new Exception();

        Player player = players.get(playerIndexFromId(playerId));
        cardsToDiscard.sort(Integer::compareTo);

        for (int i = 0; i < cardsToDiscard.size(); i++) {
            if (cardsToDiscard.get(i) > 4)
                throw new Exception();

            player.discardCard(cardsToDiscard.get(i) - i);
        }

        for (int i = 0; i < cardsToDiscard.size(); i++)
            player.receiveCard(deck.dealCard());
    }

    public void endNegotiation() {
        currentNegotiationStake = 0;
    }

    public void bid(int playerId, int bidValue) throws Exception {
        Player player = players.get(playerIndexFromId(playerId));

        player.bid(bidValue);
        currentBids.put(player, bidValue);
        allGameBids.put(player, bidValue);
    }

    public void fold(int playerId) {
        Player player = players.get(playerIndexFromId(playerId));

        folded.put(player, true);
    }

    public void splitStakeBetweenWinners() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = evaluateHands();
        ArrayList<Integer> ranking = createRanking(handValues);
        ArrayList<Integer> winners = createWinnersList(ranking, handValues);

        for (Player player : players)
            if (winners.contains(player.getId()))
                player.getPrize(stake / winners.size());

    }

    public HashMap<Integer, HandEvaluator.HandValues> evaluateHands() {
        HashMap<Integer, HandEvaluator.HandValues> handValues = new HashMap<>();
        HandEvaluator handEvaluator = new HandEvaluator();

        for (Player player : players)
            handValues.put(player.getId(), handEvaluator.evaluate(player.getHand()));

        return handValues;
    }

    private void takeAnte() throws Exception {
        for (Player player : players) {
            player.bid(ante);
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

        for (Integer key : handValues.keySet()) {
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
