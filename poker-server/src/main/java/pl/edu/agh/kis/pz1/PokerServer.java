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

/**
 * Class PokerServer is the main class that provides clients a way to play the poker game.
 *
 * @author Kacper Cienkosz
 */
public class PokerServer {
    /**
     * Selector takes care for handling non-blocking IO while server communicates with clients.
     */
    private static Selector selector = null;
    /**
     * HashMap clients keeps information about currently connected clients.
     * Keys are clients IDs and values are SocketChannels assigned to clients.
     */
    private static final HashMap<Integer, SocketChannel> clients = new HashMap<>();
    /**
     * Number of players stands for the number of players that can join the game on the PokerServer.
     * It is a starting parameter of the server and varies from 2 to 4.
     */
    private static int numberOfPlayers = 3;
    private static final int ANTE = 20;
    /**
     * Actual game running on the server.
     * Please see {@link  pl.edu.agh.kis.pz1.Game } for information about Game class.
     */
    private static Game game = null;
    /**
     * ID of the next client when they connect to the server.
     */
    private static int nextClientId = 0;

    /**
     * Method main provide all functionality of the PokerServer.
     * @param args Arguments passed to the PokerServer while executing.
     */

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 31415;

        try {
            int customNumberOfPlayers = Integer.parseInt(args[0]);

            if(customNumberOfPlayers > 4 || customNumberOfPlayers < 2)
                System.out.println("Argument is not between 2 and 4, thus numberOfPlayers is set to default value: 3");
            else
                numberOfPlayers = customNumberOfPlayers;
        }
        catch (NumberFormatException e) {
            System.out.println("Argument is not an integer so numberOfPlayers is set to default value: 3");
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No argument provided so numberOfPlayers is set to default value: 3");
        }

        System.out.println("Number of players: " + numberOfPlayers);
        game = new Game(1, numberOfPlayers, ANTE);

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

    /**
     * Method handleAccept accepts or refuses connections to the PokerServer.
     * If connection was accepted method sends message with action ACCEPT. Sends message with action DENY otherwise.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param serverSocketChannel ServerSocketChannel that is responsible for accepting connections.
     * @return Returns ID of the client that has connected. If <code>-1</code> than it means that connection has not been accepted.
     * @throws IOException Something goes wrong while accepting connection or while receiving or sending message.
     * For further information please see {@link java.nio.channels.ServerSocketChannel}, {@link java.nio.channels.SocketChannel}.
     */
    private static int handleAccept(ServerSocketChannel serverSocketChannel) throws IOException {
        System.out.println("Connection Accepted...");

        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);

        int ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        client.register(selector, ops);

        int clientId = nextClientId;
        nextClientId++;
        clients.put(clientId, client);

        try {
            game.newPlayer(clientId);
            handleWrite(game.getId(), clientId, MessageParser.Action.ACCEPT, "");
            return clientId;
        }
        catch (IncorrectNumbersOfPlayersException e) {
            handleWrite(game.getId(), clientId, MessageParser.Action.DENY, e.getMessage());
            client.close();
            clients.remove(clientId);
        }

        return -1;
    }

    /**
     * Method handleRead parses message received by PokerServer.
     *
     * @param key SelectionKey given by Selector.
     * @return Message parsed in MessageParser. For further information please see {@link pl.edu.agh.kis.pz1.MessageParser}.
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     * @throws EmptyMessageException EmptyMessageException is thrown when message received by the server is empty.
     */
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

    /**
     * Method handleWrite handles sending messages from PokerServers to clients.
     *
     * @param gameId ID of the game that message is related to.
     * @param playerId ID of the player that message is directed to.
     * @param action  Type of action that is sent by server.
     * @param actionParameter Additional parameters sent with message.
     */
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
            System.out.printf("Sending Message: %s\nbufferBytes: %d%n", message, bytesWritten);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is used when awaiting connections after starting server.
     *
     * @param serverSocketChannel ServerSocketChannel that is responsible for accepting connections.
     * @throws IOException Something goes wrong while accepting connection or while receiving or sending message.
     * For further information please see {@link java.nio.channels.ServerSocketChannel}, {@link java.nio.channels.SocketChannel}.
     */
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

    /**
     * Method handles user disconnecting from the server after receiving or sending DISCONNECT action in message.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param client SocketChannel of client that sent the message.
     * @param parser MessageParser containing message from client.
     *
     */
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

    /**
     * Method handling connection aborted by server.
     *
     * @param playerId ID of the player with which connection was broken.
     * @param message Message to the client explaining why connection was broken.
     */
    private static void serverDisconnect(int playerId, String message) {
        handleWrite(game.getId(), playerId, MessageParser.Action.DISCONNECT, message);
    }

    /**
     * Method carries out one game.
     *
     * @param serverSocketChannel ServerSocketChannel that is responsible for accepting connections.
     * @return <code>true</code> if new game should be started, <code>false</code> otherwise.
     * @throws IOException Something goes wrong while accepting connection or while receiving or sending message.
     * For further information please see {@link java.nio.channels.ServerSocketChannel}, {@link java.nio.channels.SocketChannel}.
     * @throws InterruptedException Appears if thread was interrupted while executing <code>TimeUnit.SECONDS.sleep()</code>
     * or <code>TimeUnit.MILLISECONDS.sleep()</code>.
     */
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

    /**
     * Method handles sending all players their hands by sending message with action HAND.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     */
    private static void sendPlayersHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Integer key: playerHands.keySet())
            for (Card card: playerHands.get(key))
                handleWrite(game.getId(), key, MessageParser.Action.HAND, card.toString());
    }

    /**
     * Method handles sending hand to one player by sending message with action HAND.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that hand should be sent to.
     */
    private static void sendPlayersHands(Integer id) {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Card card: playerHands.get(id))
            handleWrite(game.getId(), id, MessageParser.Action.HAND, card.toString());
    }

    /**
     * Method handles sending all players evaluations of their hands by sending message with action EVAL.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     */
    private static void sendPlayerEvaluations() {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.getPlayerHandsEvaluations();

        for (Integer key: handEvaluations.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(key)));
    }

    /**
     * Method handles sending one player evaluations of their hands by sending message with action EVAL
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that hand evaluation should be sent to.
     */
    private static void sendPlayerEvaluations(Integer id) {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.getPlayerHandsEvaluations();

        handleWrite(game.getId(), id, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(id)));
    }

    /**
     * Method sends bidding request to the player by sending message with action BID.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that request should be sent to.
     * @param alreadySentBiddingRequest HashMap containing information if bidding request was already sent to players.
     */
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

    /**
     * Method sends bidding request to the player by sending message with action BID.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     * Method is executed only after denial of the bidding request received by server.
     *
     * @param id ID of the player that request should be sent to.
     */
    private static void sendBiddingRequest(Integer id) {
        if (!game.hasFolded(id)) {
            String stakes = game.getCurrentNegotiationStake() +
                    " " +
                    game.howMuchToBid(id);

            handleWrite(game.getId(), id, MessageParser.Action.BID, stakes);
        }
    }

    /**
     * Method handles all the bidding process.
     *
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     * @throws GameEndedByFoldingException See {@link pl.edu.agh.kis.pz1.exceptions.GameEndedByFoldingException}
     */
    private static void handleBiddingProcess() throws IOException, GameEndedByFoldingException {
        HashMap<Integer, Boolean> alreadySentBiddingRequest = new HashMap<>();
        for (Integer key: clients.keySet())
            alreadySentBiddingRequest.put(key, false);

        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        int bidderIndex = 0;

        do {
            int bidderId = biddingOrder.get(bidderIndex);

            if (game.hasFolded(bidderId)) {
                bidderIndex = nextBidder(bidderIndex, biddingOrder, alreadySentBiddingRequest);
                continue;
            }

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

    /**
     * Method handles single bid request received from the player.
     *
     * @param parser MessageParser containing message from client.
     * @return <code>true</code> when bid was accepted, <code>false</code> otherwise.
     */
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

    /**
     * Method sends draw request to all the players by sending message with action DRAW.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that request should be sent to.
     * @param alreadySentDrawRequest HashMap containing information if draw request was already sent to the players.
     */
    private static void sendDrawRequest(Integer id, HashMap<Integer, Boolean> alreadySentDrawRequest) {
        if (alreadySentDrawRequest.get(id))
            return;

        alreadySentDrawRequest.put(id, true);

        if (!game.hasFolded(id))
            handleWrite(game.getId(), id, MessageParser.Action.DRAW, "");
    }

    /**
     * Method sends draw request to one player by sending message with action DRAW.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that request should be sent to.
     */
    private static void sendDrawRequest(Integer id) {
        if (!game.hasFolded(id))
            handleWrite(game.getId(), id, MessageParser.Action.DRAW, "");
    }

    /**
     * Method handles DRAW request received from the player.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param parser MessageParser containing message from client.
     * @throws NoSuchCardException See {@link pl.edu.agh.kis.pz1.exceptions.NoSuchCardException}
     * @throws IncorrectNumberOfCardsException See {@link pl.edu.agh.kis.pz1.exceptions.IncorrectNumberOfCardsException}
     */
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

    /**
     * Method handles all the drawing process.
     *
     * @throws IOException Something went wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
    private static void handleDrawingProcess() throws IOException {
        HashMap<Integer, Boolean> alreadySentDrawRequest = new HashMap<>();
        for (Integer key: clients.keySet())
            alreadySentDrawRequest.put(key, false);

        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        int drawerIndex = 0;

        do {
            int id = biddingOrder.get(drawerIndex);

            if (game.hasFolded(id)) {
                drawerIndex++;
                continue;
            }

            sendDrawRequest(id, alreadySentDrawRequest);

            int keys = selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isReadable()) {
                    drawerIndex = handleReadableKeyInDrawing(key, drawerIndex);
                }

                it.remove();
            }

        } while (drawerIndex < biddingOrder.size());
    }

    /**
     * Method handles FOLD request from the player.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that request should be sent to.
     * @throws GameEndedByFoldingException See {@link pl.edu.agh.kis.pz1.exceptions.GameEndedByFoldingException}
     */
    private static void handleFold(Integer id) throws GameEndedByFoldingException {
        game.fold(id);
    }

    /**
     * Methods sends player their credit by sending message with action CREDIT.
     * ID of the player that request should be sent to.
     *
     * @param playerId ID of the player that request should be sent to.
     */
    private static void sendPlayerCredit(int playerId) {
        handleWrite(game.getId(), playerId, MessageParser.Action.CREDIT ,String.valueOf(game.getPlayerCredit(playerId)));
    }

    /**
     * Method handles player prizing after the game have finished
     */
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

    /**
     * Method handles all the endgame process (also waiting for new players).
     *
     * @param serverSocketChannel ServerSocketChannel that is responsible for accepting connections.
     * @return <code>true</code> if new game should be started, <code>false</code> otherwise.
     * @throws IOException Something goes wrong while accepting connection or while receiving or sending message.
     * For further information please see {@link java.nio.channels.ServerSocketChannel}, {@link java.nio.channels.SocketChannel}.
     * @throws InterruptedException Appears if thread was interrupted while executing <code>TimeUnit.SECONDS.sleep()</code>
     * or <code>TimeUnit.MILLISECONDS.sleep()</code>.
     */
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
                    playersMissing = acceptableKeyInEndgameProcess(serverSocketChannel, playersMissing, playersWantToPlay, acceptedLastRequest);
                if (key.isReadable())
                    playersMissing = readableKeyInEndgameProcess(key, playersMissing, playersWantToPlay, acceptedLastRequest);

                it.remove();
            }

            if (playersWantToPlay.size() == numberOfPlayers && !acceptedLastRequest.containsValue(false)) {
                TimeUnit.SECONDS.sleep(1);
                return true;
            }


        } while (playersMissing != numberOfPlayers);

        return false;
    }

    /**
     * Sends players information about game end by sending message with action END and information about how many players are missing.
     *
     * @param playersMissing Number of players needed to start new game.
     * @param acceptedLastRequest HashMap containing information if players accepted last end request.
     */
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

    /**
     * Method handles readable keys in waiting for connections.
     *
     * @param key SelectionKey given by Selector.
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
    private static void handleReadableKeyInWaitingForConnections(SelectionKey key) throws IOException {
        try {
            MessageParser parser = handleRead(key);

            if (parser.getActionType() == MessageParser.Action.DISCONNECT)
                disconnect((SocketChannel) key.channel(), parser);
        } catch (EmptyMessageException ignored) {

        }
    }

    /**
     * Method returns next bidder index.
     *
     * @param bidderIndex Current bidder index in biddingOrder.
     * @param biddingOrder ArrayList containing biddingOrder. See {@link Game#getBiddingOrder()}.
     * @param alreadySentBiddingRequest HashMap containing information if bidding request was already sent to players.
     *                                  If bidding round has ended all the values are set to false.
     * @return Next bidder id.
     */
    private static int nextBidder(int bidderIndex, ArrayList<Integer> biddingOrder, HashMap<Integer, Boolean> alreadySentBiddingRequest) {
        if (bidderIndex == biddingOrder.size() - 1)
            alreadySentBiddingRequest.replaceAll((p, v) -> false);

        return (bidderIndex + 1) % biddingOrder.size();
    }

    /**
     *  Method handles readable keys in bidding process.
     *
     * @param key SelectionKey given by Selector.
     * @param bidderIndex Current bidder index.
     * @param biddingOrder ArrayList containing biddingOrder. See {@link Game#getBiddingOrder()}.
     * @param sentBiddingRequest HashMap containing information if bidding request was already sent to players.
     *                                  If bidding round has ended all the values are set to false.
     * @return Next bidder index.
     * @throws GameEndedByFoldingException See {@link pl.edu.agh.kis.pz1.exceptions.GameEndedByFoldingException}
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
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

    /**
     *  Handles draw request received by server.
     *
     * @param parser  MessageParser containing message from client.
     * @param drawerIndex Index of current drawer.
     * @return Index of next drawer.
     */
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

    /**
     * Method handles readable key in endgame process. Changes <code>missingPlayers</code> and <code>playersWantToPlay</code>.
     *
     * @param key SelectionKey given by Selector
     * @param playersMissing Number of players needed to start new game.
     * @param playersWantToPlay ArrayList containing indices of players that declared will to play next game.
     * @param acceptedLastRequest HashMap containing information if players accepted last end request.
     * @return Number of still missing players.
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
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

    /**
     * Method handles acceptable keys in endgame process.
     * Changes <code>missingPlayers</code> and <code>playersWantToPlay</code>.
     * Accepts new players.
     *
     * @param serverSocketChannel ServerSocketChannel that is responsible for accepting connections.
     * @param playersMissing Number of players needed to start new game.
     * @param playersWantToPlay ArrayList containing indices of players that declared will to play next game.
     * @param acceptedLastRequest HashMap containing information if players accepted last end request.
     * @throws IOException Something goes wrong while accepting connection or while receiving or sending message.
     * For further information please see {@link java.nio.channels.ServerSocketChannel}, {@link java.nio.channels.SocketChannel}.
     * @return New number of players needed to start new game.
     */
    private static int acceptableKeyInEndgameProcess(ServerSocketChannel serverSocketChannel,
                                                     int playersMissing, Set<Integer> playersWantToPlay,
                                                      HashMap<Integer, Boolean> acceptedLastRequest) throws IOException {
        int id = handleAccept(serverSocketChannel);

        if (id != -1) {
            playersWantToPlay.add(id);
            playersMissing--;

            sendEnd(playersMissing, acceptedLastRequest);
        }

        return playersMissing;
    }

    /**
     * Handles reading keys in drawing process;
     *
     * @param key SelectionKey given by Selector.
     * @param drawerIndex Index of the drawer in the biddingOrder.
     * @return Next drawer index.
     * @throws IOException Something goes wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
    private static int handleReadableKeyInDrawing(SelectionKey key, int drawerIndex) throws IOException {
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

        return drawerIndex;
    }
}
