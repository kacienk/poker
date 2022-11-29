package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PokerServer {
    private static Selector selector = null;
    private static final HashMap<Integer, SocketChannel> clients = new HashMap<>();
    private static final int NUMBER_OF_PLAYERS = 2;
    private static final Game game = new Game(1, NUMBER_OF_PLAYERS);
    private static int lastClientId = 0;
    private static int lastGameId = 0;

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 31415;

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            selector = Selector.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(hostname, port));
            serverSocketChannel.configureBlocking(false);

            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);

            waitForConnections(serverSocketChannel);

            boolean gameShouldStart = true;

            while (gameShouldStart)
                gameShouldStart = carryOutGame(serverSocketChannel);

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static int handleAccept(ServerSocketChannel serverSocketChannel) throws IOException {
        System.out.println("Connection Accepted...");

        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);

        int ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        client.register(selector, ops);

        int clientId = lastClientId;
        lastClientId++;
        clients.put(clientId, client);

        try {
            game.newPlayer(clientId);
            handleWrite(game.getId(), clientId, MessageParser.Action.ACCEPT, "");
            return clientId;
        }
        catch (IncorrectNumbersOfPlayersException e) {
            handleWrite(game.getId(), clientId, MessageParser.Action.DENY, e.getMessage());
            client.close();
        }

        return -1;
    }

    private static MessageParser handleRead(SelectionKey key) throws IOException, EmptyMessageException {
        //System.out.println("Reading...");
        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);

        String message = new String(buffer.array()).trim();

        if (message.isEmpty())
            throw new EmptyMessageException("Message is empty");

        System.out.println("Received: " + message);

        MessageParser parser = new MessageParser(message);

        try {
            parser.getGameId();
            parser.getPlayerId();
            parser.getActionType();
            parser.getActionParameters();
        } catch (Exception e) {
            throw e;
        }


        return parser;
    }

    private static void handleWrite(int gameId, int playerId, MessageParser.Action action, String actionParameter) {
        SocketChannel client = clients.get(playerId);
        MessageParser parser = new MessageParser();
        String message = parser.parse(gameId, playerId, action, actionParameter);

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(message.getBytes());
            buffer.flip();
            int bytesWritten = client.write(buffer);
            System.out.printf("Sending Message: %s\nbufforBytes: %d%n", message, bytesWritten);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitForConnections(ServerSocketChannel serverSocketChannel) throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable())
                    handleAccept(serverSocketChannel);
                else if (key.isReadable())
                    handleReadableKeyInWaitingForConnections(key);

                it.remove();
            }

            if (game.canBegin())
                return;

        }
    }

    private static void disconnect(SocketChannel client, MessageParser parser) {
        System.out.println("Player " + parser.getPlayerId() + " disconnected from game " + parser.getGameId());
        game.removePlayer(parser.getPlayerId());

        try {
            client.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void serverDisconnect(int playerId, String message) {
        handleWrite(game.getId(), playerId, MessageParser.Action.DISCONNECT, message);
    }

    private static boolean carryOutGame(ServerSocketChannel serverSocketChannel) throws IOException, InterruptedException {
        try {
            game.newGame();
            TimeUnit.SECONDS.sleep(1);
        } catch (IncorrectNumbersOfPlayersException ignored) {

        }
        catch (NotEnoughCreditException e) {
            System.out.println(e.getMessage());

            for (Integer key: clients.keySet())
                serverDisconnect(key, e.getMessage());

            return false;
        }

        for (Integer key: clients.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.START, "");

        sendPlayersHands();
        sendPlayerEvaluations();

        try {
            handleBiddingProcess();

            handleDrawingProcess();

            handleBiddingProcess();
        }
        catch (GameEndedByFoldingException ignored) {

        }

        return handleEndgameProcess(serverSocketChannel);
    }

    private static void sendPlayersHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Integer key: playerHands.keySet())
            for (Card card: playerHands.get(key))
                handleWrite(game.getId(), key, MessageParser.Action.HAND, card.toString());
    }

    private static void sendPlayersHands(Integer id) {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Card card: playerHands.get(id))
            handleWrite(game.getId(), id, MessageParser.Action.HAND, card.toString());
    }

    private static void sendPlayerEvaluations() {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.evaluateHands();

        for (Integer key: handEvaluations.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(key)));
    }

    private static void sendPlayerEvaluations(Integer id) {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.evaluateHands();

        handleWrite(game.getId(), id, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(id)));
    }

    private static void sendBiddingRequest(Integer id, HashMap<Integer, Boolean> alreadySentBiddingRequest) {
        if (alreadySentBiddingRequest.get(id) && !game.hasFolded(id))
            return;

        alreadySentBiddingRequest.put(id, true);

        if (!game.hasFolded(id)) {
            String stakes = game.getCurrentNegotiationStake() +
                    " " +
                    game.howMuchToBid(id);

            handleWrite(game.getId(), id, MessageParser.Action.BID, stakes);
        }
    }

    private static void sendBiddingRequest(Integer id) {
        if (!game.hasFolded(id)) {
            String stakes = game.getCurrentNegotiationStake() +
                    " " +
                    game.howMuchToBid(id);

            handleWrite(game.getId(), id, MessageParser.Action.BID, stakes);
        }
    }

    private static void handleBiddingProcess() throws IOException, GameEndedByFoldingException {
        HashMap<Integer, Boolean> alreadySentBiddingRequest = new HashMap<>();
        for (Integer key: clients.keySet())
            alreadySentBiddingRequest.put(key, false);

        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        int bidderIndex = 0;

        do {
            int bidderId = biddingOrder.get(bidderIndex);

            sendBiddingRequest(bidderId, alreadySentBiddingRequest);

            int keys = selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                bidderIndex = handleKeyInBiddingProcess(key, bidderIndex, biddingOrder, alreadySentBiddingRequest);

                it.remove();
            }
        } while (!game.biddingOver());
    }

    private static boolean handleOneBid(MessageParser parser) {
        int playerId = parser.getPlayerId();
        int bid = 0;

        try {
            bid = Integer.parseInt(parser.getActionParameters());
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }

        try {
            game.bid(playerId, bid);
            return true;
        } catch (TooSmallBidException | NotEnoughCreditException e) {
            handleWrite(game.getId(), playerId, MessageParser.Action.DENY, e.getMessage());
            sendPlayerCredit(playerId);
            sendBiddingRequest(playerId);
            return false;
        }
    }

    private static void sendDrawRequest(Integer id, HashMap<Integer, Boolean> alreadySentDrawRequest) {
        if (alreadySentDrawRequest.get(id))
            return;

        alreadySentDrawRequest.put(id, true);

        if (!game.hasFolded(id))
            handleWrite(game.getId(), id, MessageParser.Action.DRAW, "");
    }

    private static void sendDrawRequest(Integer id) {
        if (!game.hasFolded(id))
            handleWrite(game.getId(), id, MessageParser.Action.DRAW, "");
    }

    private static void handleDraw(MessageParser parser) throws NoSuchCardException, IncorrectNumberOfCardsException {
        ArrayList<Integer> cardsToDiscard = new ArrayList<>();

        String playerRequest = parser.getActionParameters();

        if (playerRequest.isEmpty())
            return;

        ArrayList<String> playerRequestSplit = new ArrayList<>(Arrays.asList(playerRequest.split(" ")));

        for (String card: playerRequestSplit)
            cardsToDiscard.add(Integer.parseInt(card));

        game.draw(parser.getPlayerId(), cardsToDiscard);
    }

    private static void handleDrawingProcess() throws IOException {
        HashMap<Integer, Boolean> alreadySentDrawRequest = new HashMap<>();
        for (Integer key: clients.keySet())
            alreadySentDrawRequest.put(key, false);

        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        int drawerIndex = 0;

        do {
            int id = biddingOrder.get(drawerIndex);

            sendDrawRequest(id, alreadySentDrawRequest);

            int keys = selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isReadable()) {
                    try {
                        MessageParser parser = handleRead(key);

                        switch (parser.getActionType()) {
                            case DISCONNECT -> disconnect((SocketChannel) key.channel(), parser);
                            case HAND -> {
                                sendPlayersHands(parser.getPlayerId());
                                sendDrawRequest(parser.getPlayerId());
                            }
                            case EVAL -> {
                                sendPlayerEvaluations(parser.getPlayerId());
                                sendDrawRequest(parser.getPlayerId());
                            }
                            case DRAW -> drawerIndex = handleDrawRequest(parser, drawerIndex);
                            default -> System.out.println("Unexpected action.");
                        }
                    }
                    catch (EmptyMessageException ignored) {

                    }
                }

                it.remove();
            }
        } while (drawerIndex < biddingOrder.size());
    }

    private static void handleFold(Integer id) throws GameEndedByFoldingException {
        game.fold(id);
    }

    private static void sendPlayerCredit(int playerId) {
        handleWrite(game.getId(), playerId, MessageParser.Action.CREDIT ,String.valueOf(game.getPlayerCredit(playerId)));
    }

    private static void handlePrizingProcess() {
        HashMap<Integer, Integer> playerPrizes = game.splitStakeBetweenWinners();

        for (Integer key: clients.keySet()) {
            if (playerPrizes.containsKey(key)) {
                handleWrite(game.getId(), key, MessageParser.Action.PRIZE, String.valueOf(playerPrizes.get(key)));
            }
            else {
                handleWrite(game.getId(), key, MessageParser.Action.PRIZE, String.valueOf(0));
            }

            sendPlayerCredit(key);
        }
    }

    private static boolean handleEndgameProcess(ServerSocketChannel serverSocketChannel) throws IOException, InterruptedException {
        handlePrizingProcess();

        Set<Integer> playersWantToPlay = new HashSet<>();
        HashMap<Integer, Boolean> acceptedLastRequest = new HashMap<>();
        int playersMissing = 0;

        sendEnd(playersMissing, acceptedLastRequest);

        do {
            int keys = selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable())
                    acceptableKeyInEndgameProcess(serverSocketChannel, playersMissing, playersWantToPlay, acceptedLastRequest);
                if (key.isReadable())
                    playersMissing = readableKeyInEndgameProcess(key, playersMissing, playersWantToPlay, acceptedLastRequest);

                it.remove();
            }

            if (playersWantToPlay.size() == NUMBER_OF_PLAYERS && !acceptedLastRequest.containsValue(false)) {
                TimeUnit.SECONDS.sleep(1);
                return true;
            }


        } while (playersMissing != NUMBER_OF_PLAYERS);

        return false;
    }

    private static void sendEnd(int playersMissing, HashMap<Integer, Boolean> acceptedLastRequest) {
        for (Integer key: clients.keySet()) {
            if (acceptedLastRequest.containsKey(key) && acceptedLastRequest.get(key)) {
                acceptedLastRequest.put(key, false);
                handleWrite(game.getId(), key, MessageParser.Action.END, String.valueOf(playersMissing));
            }
            else if (!acceptedLastRequest.containsKey(key)) {
                acceptedLastRequest.put(key, false);
                handleWrite(game.getId(), key, MessageParser.Action.END, String.valueOf(playersMissing));
            }
        }
    }
    private static void sendEnd(Integer id, int playersMissing) {
        handleWrite(game.getId(), id, MessageParser.Action.END, String.valueOf(playersMissing));
    }

    private static void handleReadableKeyInWaitingForConnections(SelectionKey key) throws IOException {
        try {
            MessageParser parser = handleRead(key);

            if (parser.getActionType() == MessageParser.Action.DISCONNECT)
                disconnect((SocketChannel) key.channel(), parser);
        } catch (EmptyMessageException ignored) {

        }
    }

    private static int nextBidder(int bidderIndex, ArrayList<Integer> biddingOrder, HashMap<Integer, Boolean> alreadySentBiddingRequest) {
        if (bidderIndex == biddingOrder.size() - 1)
            alreadySentBiddingRequest.replaceAll((p, v) -> false);

        return (bidderIndex + 1) % biddingOrder.size();
    }

    private static int handleKeyInBiddingProcess(SelectionKey key, int bidderIndex, ArrayList<Integer> biddingOrder,
                                                  HashMap<Integer, Boolean> sentBiddingRequest) throws GameEndedByFoldingException, IOException {
        if (key.isReadable()) {
            try {
                MessageParser parser = handleRead(key);

                switch (parser.getActionType()) {
                    case DISCONNECT -> disconnect((SocketChannel) key.channel(), parser);
                    case HAND -> {
                        sendPlayersHands(parser.getPlayerId());
                        sendBiddingRequest(parser.getPlayerId());
                    }
                    case FOLD -> {
                        handleFold(parser.getPlayerId());

                        bidderIndex = nextBidder(bidderIndex, biddingOrder, sentBiddingRequest);
                    }
                    case EVAL -> {
                        sendPlayerEvaluations(parser.getPlayerId());
                        sendBiddingRequest(parser.getPlayerId());
                    }
                    case BID -> {
                        boolean biddenProperly = handleOneBid(parser);

                        if (biddenProperly)
                            bidderIndex = nextBidder(bidderIndex, biddingOrder, sentBiddingRequest);
                    }
                    case CREDIT -> {
                        sendPlayerCredit(parser.getPlayerId());
                        sendBiddingRequest(parser.getPlayerId());
                    }
                    default -> System.out.println("Unexpected action.");
                }
            } catch (EmptyMessageException ignored) {

            }
        }

        return bidderIndex;
    }

    private static int handleDrawRequest(MessageParser parser, int drawerIndex) {
        try {
            handleDraw(parser);
            sendPlayersHands(parser.getPlayerId());
            sendPlayerEvaluations(parser.getPlayerId());
            drawerIndex++;
        }
        catch (NoSuchCardException | IncorrectNumberOfCardsException e) {
            handleWrite(game.getId(), parser.getPlayerId(), MessageParser.Action.DENY, e.getMessage());
            sendDrawRequest(parser.getPlayerId());
        } catch (NumberFormatException e) {
            String message = "Incorrect input" + parser.getActionParameters() + ".";
            handleWrite(game.getId(), parser.getPlayerId(), MessageParser.Action.DENY, message);
            sendDrawRequest(parser.getPlayerId());
        }

        return drawerIndex;
    }

    private static int readableKeyInEndgameProcess(SelectionKey key, int playersMissing, Set<Integer> playersWantToPlay,
                                                   HashMap<Integer, Boolean> acceptedLastRequest) throws IOException {
        try {
            MessageParser parser = handleRead(key);

            if (parser.getActionType() == MessageParser.Action.DISCONNECT) {
                playersMissing++;

                disconnect((SocketChannel) key.channel(), parser);
                clients.remove(parser.getPlayerId());

                playersWantToPlay.remove(parser.getPlayerId());
                acceptedLastRequest.remove(parser.getPlayerId());

                sendEnd(playersMissing, acceptedLastRequest);
            }

            if (parser.getActionType() == MessageParser.Action.ACCEPT) {
                playersWantToPlay.add(parser.getPlayerId());
                acceptedLastRequest.put(parser.getPlayerId(), true);
            }
        } catch (EmptyMessageException ignored) {

        }

        return playersMissing;
    }

    private static void acceptableKeyInEndgameProcess(ServerSocketChannel serverSocketChannel,
                                                     int playersMissing, Set<Integer> playersWantToPlay,
                                                      HashMap<Integer, Boolean> acceptedLastRequest) throws IOException {
        int id = handleAccept(serverSocketChannel);

        if (id != -1) {
            playersWantToPlay.add(id);
            playersMissing--;

            sendEnd(playersMissing, acceptedLastRequest);
        };
    }
}
