package pl.edu.agh.kis.pz1;

import pl.edu.agh.kis.pz1.exceptions.IncorrectNumbersOfPlayersException;
import pl.edu.agh.kis.pz1.exceptions.NotEnoughCreditException;
import pl.edu.agh.kis.pz1.exceptions.TooSmallBidException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PokerServer {
    private static Selector selector = null;
    private static final HashMap<Integer, SocketChannel> clients = new HashMap<>();
    private static final Game game = new Game(1, 2);
    static int lastClientId = 0;
    static int lastGameId = 0;

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 31415;

        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(hostname, port));
            serverSocketChannel.configureBlocking(false);

            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);

            waitForConnections(serverSocketChannel);

            carryOutGame();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAccept(ServerSocketChannel serverSocketChannel, SelectionKey key) throws IOException {
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
            handleWrite(game.getId(), clientId, MessageParser.Action.ACCEPT, "", client);
        }
        catch (IncorrectNumbersOfPlayersException e) {
            handleWrite(game.getId(), clientId, MessageParser.Action.DENY, e.getMessage(), client);
            client.close();
        }
    }

    private static MessageParser handleRead(SelectionKey key) throws IOException {
        //System.out.println("Reading...");
        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);

        String message = new String(buffer.array()).trim();
        MessageParser parser = new MessageParser(message);

        try {
            parser.getGameId();
            parser.getPlayerId();
            parser.getActionType();
            parser.getActionParameters();
        }
        catch (Exception e) {
            throw e;
        }

        return parser;
    }

    private static void handleWrite(int gameId, int playerId, MessageParser.Action action, String actionParameter, SocketChannel client) {
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

                if (key.isAcceptable()) {
                    handleAccept(serverSocketChannel, key);
                } else if (key.isReadable()) {
                    MessageParser parser = handleRead(key);

                    if (parser.getActionType() == MessageParser.Action.DISCONNECT)
                        disconnect((SocketChannel) key.channel(), parser);

                }

                it.remove();
            }

            try {
                game.newGame();
                TimeUnit.SECONDS.sleep(1);
                return;
            } catch (IncorrectNumbersOfPlayersException ignored) {

            }
            catch (NotEnoughCreditException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    private static void carryOutGame() throws IOException {
        for (Integer key: clients.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.START, "", clients.get(key));

        sendPlayersHands();
        sendPlayerEvaluations();
        ArrayList<Integer> biddingOrder = game.getBiddingOrder();
        int bidderIndex = 0;

        do {
            sendBiddingRequest(biddingOrder.get(bidderIndex));

            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isReadable()) {
                    MessageParser parser = handleRead(key);

                    switch (parser.getActionType()) {
                        case DISCONNECT -> disconnect((SocketChannel) key.channel(), parser);
                        case HAND -> sendPlayersHands(parser.getPlayerId());
                        case FOLD -> {
                            handleFold(parser.getPlayerId());
                            bidderIndex = (bidderIndex + 1) % biddingOrder.size();
                        }
                        case EVAL -> sendPlayerEvaluations(parser.getPlayerId());
                        case BID -> {
                            boolean biddenProperly = handleBid(parser);

                            if (biddenProperly)
                                bidderIndex = (bidderIndex + 1) % biddingOrder.size();
                        }
                    }
                }

                it.remove();
            }

        } while (!game.biddingOver());
    }

    private static void sendPlayersHands() {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Integer key: playerHands.keySet())
            for (Card card: playerHands.get(key))
                handleWrite(game.getId(), key, MessageParser.Action.HAND, card.toString(), clients.get(key));
    }

    private static void sendPlayersHands(Integer id) {
        HashMap<Integer, ArrayList<Card>> playerHands = game.getPlayerHands();

        for (Card card: playerHands.get(id))
            handleWrite(game.getId(), id, MessageParser.Action.HAND, card.toString(), clients.get(id));
    }

    private static void sendPlayerEvaluations() {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.evaluateHands();

        for (Integer key: handEvaluations.keySet())
            handleWrite(game.getId(), key, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(key)), clients.get(key));
    }

    private static void sendPlayerEvaluations(Integer id) {
        HashMap<Integer, HandEvaluator.HandValues> handEvaluations = game.evaluateHands();

        handleWrite(game.getId(), id, MessageParser.Action.EVAL, String.valueOf(handEvaluations.get(id)), clients.get(id));
    }

    private static void sendBiddingRequest() {
        for (Integer key: clients.keySet())
            sendBiddingRequest(key);
    }

    private static void sendBiddingRequest(Integer id) {
        if (!game.hasFolded(id)) {
            String stakes = game.getCurrentNegotiationStake() +
                    " " +
                    game.howMuchToBid(id);

            handleWrite(game.getId(), id, MessageParser.Action.BID, stakes, clients.get(id));
        }
    }

    private static void handleFold(Integer id) {
        game.fold(id);
    }

    private static boolean handleBid(MessageParser parser) {
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
            handleWrite(game.getId(), playerId, MessageParser.Action.DENY, e.getMessage(), clients.get(playerId));
            return false;
        }
    }

    private static void sendEnd() {
        for (Integer key: clients.keySet()) {
            handleWrite(game.getId(), key, MessageParser.Action.END, "", clients.get(key));
        }
    }
    private static void sendEnd(Integer id) {
        handleWrite(game.getId(), id, MessageParser.Action.END, "", clients.get(id));
    }
}
