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
     */
    private static MessageParser handleRead(SelectionKey key) throws IOException {
        MessageParser parser = new MessageParser();

        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);

        String message = new String(buffer.array()).trim();

        if (!message.isEmpty()) {
            System.out.println("Received: " + message);
            parser.parse(message);
        }
        else {
            System.out.println("Received: {empty message}");
            parser.setInvalidMessageTemplate();
        }

        parser.getGameId();
        parser.getPlayerId();
        parser.getActionType();
        parser.getActionParameters();

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
            System.out.printf("Sending Message: %s\n bufferBytes: %d%n", message, bytesWritten);
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
        while (!game.canBegin()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable())
                    handleAccept(serverSocketChannel);
                else if (key.isReadable()) {
                    MessageParser parser = handleRead(key);

                    if (parser.getActionType() == MessageParser.Action.DISCONNECT)
                        disconnect(parser);
                }

                it.remove();
            }
        }
    }

    /**
     * Method handles user disconnecting from the server after receiving or sending DISCONNECT action in message.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param parser MessageParser containing message from client.
     *
     */
    private static void disconnect(MessageParser parser) {
        System.out.println("Player " + parser.getPlayerId() + " disconnected from game " + parser.getGameId());
        // TODO

        try {
            clients.get(parser.getPlayerId()).close();
            clients.remove(parser.getPlayerId());

            game.removePlayer(parser.getPlayerId());
        }
        catch (GameEndedByFoldingException e) {
            throw new RuntimeException(e);
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
        }
        catch (NotEnoughCreditException | IncorrectNumbersOfPlayersException e) {
            System.out.println(e.getMessage());

            for (Integer key: clients.keySet())
                serverDisconnect(key, e.getMessage());

            return false;
        }

        for (Integer key: clients.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.START, "");

        sendPlayerHands();
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
    private static void sendPlayerHands() {
        for (Integer id: game.getPlayers())
            sendPlayerHands(id);
    }

    /**
     * Method handles sending hand to one player by sending message with action HAND.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that hand should be sent to.
     */
    private static void sendPlayerHands(Integer id) {
        StringBuilder sb = new StringBuilder();

        for (Card card: game.getPlayerHand(id)) {
            sb.append(card);
            sb.append('\n');
        }

        handleWrite(game.getId(), id, MessageParser.Action.HAND, sb.toString());
    }

    /**
     * Method handles sending all players evaluations of their hands by sending message with action EVAL.
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     */
    private static void sendPlayerEvaluations() {
        for (Integer id: game.getPlayers())
            sendPlayerEvaluations(id);
    }

    /**
     * Method handles sending one player evaluations of their hands by sending message with action EVAL
     * For further information about actions see {@link pl.edu.agh.kis.pz1.MessageParser}
     *
     * @param id ID of the player that hand evaluation should be sent to.
     */
    private static void sendPlayerEvaluations(Integer id) {
        handleWrite(game.getId(), id, MessageParser.Action.EVAL, String.valueOf(game.getPlayerHandEvaluation(id)));
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
        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        Iterator<Integer> biddingOrderIterator = biddingOrder.iterator();
        Integer playerId = biddingOrderIterator.next();

        while (!game.biddingOver()) {
            if (!biddingOrderIterator.hasNext())
                biddingOrderIterator = biddingOrder.iterator();

            if (game.hasFolded(playerId)) {
                playerId = biddingOrderIterator.next();
                continue;
            }

            sendBiddingRequest(playerId);

            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (!key.isReadable())
                    continue;

                MessageParser parser = handleRead(key);

                switch (parser.getActionType()) {
                    case DISCONNECT -> disconnect(parser);
                    case HAND -> sendPlayerHands(parser.getPlayerId());
                    case EVAL -> sendPlayerEvaluations(parser.getPlayerId());
                    case CREDIT -> sendPlayerCredit(parser.getPlayerId());
                    case FOLD -> {
                        handleFold(parser.getPlayerId());
                        playerId = biddingOrderIterator.next();
                    }
                    case BID -> {
                        try {
                            int bid = Integer.parseInt(parser.getActionParameters());
                            game.bid(parser.getPlayerId(), bid);

                            playerId = biddingOrderIterator.next();
                        }
                        catch (TooSmallBidException | NotEnoughCreditException e) {
                            handleWrite(game.getId(), playerId, MessageParser.Action.DENY, e.getMessage());
                            sendPlayerCredit(playerId);
                        }
                        catch (NumberFormatException e) {
                            String message = "Incorrect input" + parser.getActionParameters() + ".";
                            handleWrite(game.getId(), parser.getPlayerId(), MessageParser.Action.DENY, message);
                        }
                    }
                    default -> System.out.println("Unexpected action.");
                }

                it.remove();
            }
        }
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
        ArrayList<Integer> cardsToDiscard;

        String[] playerRequestSplit = parser.getActionParameters().split(" ");
        cardsToDiscard = new ArrayList<>(Arrays.stream(playerRequestSplit).map(Integer::parseInt).toList());

        game.draw(parser.getPlayerId(), cardsToDiscard);
    }

    /**
     * Method handles all the drawing process.
     *
     * @throws IOException Something went wrong while receiving or sending message. For further information please see {@link java.nio.channels.SocketChannel}.
     */
    private static void handleDrawingProcess() throws IOException {
        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        Iterator<Integer> biddingOrderIterator = biddingOrder.iterator();
        Integer playerId = biddingOrderIterator.next();

        while (biddingOrderIterator.hasNext()) {
            if (game.hasFolded(playerId)) {
                playerId = biddingOrderIterator.next();
                continue;
            }

            sendDrawRequest(playerId);

            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (!key.isReadable())
                    continue;

                MessageParser parser = handleRead(key);

                switch (parser.getActionType()) {
                    case DISCONNECT -> disconnect(parser);
                    case HAND -> sendPlayerHands(parser.getPlayerId());
                    case EVAL -> sendPlayerEvaluations(parser.getPlayerId());
                    case DRAW -> {
                        try {
                            handleDraw(parser);
                            sendPlayerHands(parser.getPlayerId());
                            sendPlayerEvaluations(parser.getPlayerId());

                            playerId = biddingOrderIterator.next();
                        }
                        catch (NoSuchCardException | IncorrectNumberOfCardsException e) {
                            handleWrite(game.getId(), parser.getPlayerId(), MessageParser.Action.DENY, e.getMessage());
                        }
                        catch (NumberFormatException e) {
                            String message = "Incorrect input" + parser.getActionParameters() + ".";
                            handleWrite(game.getId(), parser.getPlayerId(), MessageParser.Action.DENY, message);
                        }
                    }
                    default -> System.out.println("Unexpected action.");
                }

                it.remove();
            }
        }
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

        for (Map.Entry<Integer, Integer> entry: playerPrizes.entrySet()) {
            handleWrite(game.getId(), entry.getKey(), MessageParser.Action.PRIZE, String.valueOf(entry.getValue()));
            sendPlayerCredit(entry.getKey());
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
        int playersMissing = numberOfPlayers - game.getPlayers().size();

        sendEnd(playersMissing, game.getPlayers());

        while (playersMissing != numberOfPlayers) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                int id;
                SelectionKey key = it.next();

                if (key.isAcceptable() && (id = handleAccept(serverSocketChannel)) != -1) {
                    sendEnd(playersMissing, playersWantToPlay);
                    playersWantToPlay.clear();
                    playersWantToPlay.add(id);
                }
                if (key.isReadable()) {
                    MessageParser parser = handleRead(key);

                    if (parser.getActionType() == MessageParser.Action.DISCONNECT) {
                        disconnect(parser);

                        playersWantToPlay.remove(parser.getPlayerId());
                        sendEnd(playersMissing, playersWantToPlay);
                        playersWantToPlay.clear();
                    }

                    if (parser.getActionType() == MessageParser.Action.ACCEPT)
                        playersWantToPlay.add(parser.getPlayerId());
                }

                it.remove();
            }

            playersMissing = numberOfPlayers - game.getPlayers().size();

            if (playersWantToPlay.size() == numberOfPlayers) {
                TimeUnit.SECONDS.sleep(1);
                return true;
            }
        }

        return false;
    }

    /**
     * Sends players information about game end by sending message with action END and information about how many players are missing.
     *
     * @param playersMissing Number of players needed to start new game.
     */
    private static void sendEnd(int playersMissing, Set<Integer> recipients) {
        for (Integer key: recipients)
                handleWrite(game.getId(), key, MessageParser.Action.END, String.valueOf(playersMissing));
    }
}
