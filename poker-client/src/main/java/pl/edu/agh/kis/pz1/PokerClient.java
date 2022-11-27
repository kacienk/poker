package pl.edu.agh.kis.pz1;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PokerClient {
    private static Selector selector = null;
    static int playerId = -1;
    static int gameId = -1;

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 31415;

        SocketChannel client = connect(hostname, port);

        handleGame(client);

        disconnect(client);
    }

    private static SocketChannel connect(String hostname, int port) {
        SocketChannel client = null;

        try {
            System.out.println("Starting client...");
            selector = Selector.open();

            client = SocketChannel.open(new InetSocketAddress(hostname, port));
            client.configureBlocking(false);
            int ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            client.register(selector, ops);

            MessageParser parser = handleRead();

            if (parser.getActionType() == MessageParser.Action.DENY) {
                System.out.println("Unable to establish connection.");
                client.close();
                System.exit(1);
            }

            playerId = parser.getPlayerId();
            gameId = parser.getGameId();

            System.out.println("Connection has been established.\n" +
                    "Your playerId is: " + playerId + ".\n" +
                    "Your gameId is: " + gameId + ".");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    private static void disconnect(SocketChannel client) {
        handleWrite(MessageParser.Action.DISCONNECT, "", client);

        try {
            client.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Disconnected.");
    }

    private static void serverDisconnected(SocketChannel client) {
        try {
            client.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server disconnected.");
    }

    private static MessageParser handleRead() {
        MessageParser parser = new MessageParser();

        try {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer);

                    String data = new String(buffer.array()).trim();
                    parser.parse(data);
                }

                it.remove();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

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

    private static void handleWrite(MessageParser.Action action, String actionParameter, SocketChannel client) {
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

    public static boolean handleGame(SocketChannel client) {
        MessageParser.Action lastAction = MessageParser.Action.ACCEPT;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            MessageParser parser = handleRead();

            switch (parser.getActionType()) {
                case START -> {
                    System.out.println("Game has started.");
                    lastAction = MessageParser.Action.START;
                }
                case HAND -> {
                    if (lastAction != MessageParser.Action.HAND)
                        System.out.println("Your hand:");

                    System.out.println(parser.getActionParameters());
                    lastAction = MessageParser.Action.HAND;
                }
                case EVAL -> {
                    System.out.println("Your hand value is: " + parser.getActionParameters());
                    lastAction = MessageParser.Action.EVAL;
                }
                case DENY -> {
                    System.out.println("Incorrect action: " + parser.getActionParameters());
                    lastAction = MessageParser.Action.DENY;
                }
                case BID -> {
                    System.out.println("It is time for bidding.");
                    ArrayList<String> stakes = new ArrayList<>(Arrays.asList(parser.getActionParameters().split(" ")));

                    int currentStake = Integer.parseInt(stakes.get(0));
                    int howMuchToBid = Integer.parseInt(stakes.get(1));

                    System.out.println("Current stake is: " + currentStake);
                    System.out.println("You must bid at least: " + howMuchToBid);

                    lastAction = MessageParser.Action.BID;
                }
                case DISCONNECT -> {
                    serverDisconnected(client);
                    return false;
                }
                case FOLD, ACCEPT -> { }
                case END -> {
                    return gameEnded(client);
                }
            }

            if (lastAction == MessageParser.Action.BID) {
                System.out.println("What do you want to do?\n" +
                        "b - bid.\n" +
                        "c - check.\n" +
                        "e - get evaluation.\n" +
                        "f - fold.\n" +
                        "h - get hand.\n");

                String action = scanner.nextLine().trim();

                switch (action) {
                    case "b" -> {
                        System.out.println("How much do you want to bid?");
                        int bid = -1;
                        while (bid == -1) {
                            try {
                                int scanned = scanner.nextInt();

                                if (scanned >= 0) {
                                    bid = scanned;
                                } else
                                    System.out.println("Incorrect value. Bid should be greater or equal 0.");
                            } catch (InputMismatchException e) {
                                System.out.println("Incorrect value. Your bid should be integer.");
                            }
                        }
                        handleWrite(MessageParser.Action.BID, Integer.toString(bid), client);
                    }
                    case "c" -> {
                        ArrayList<String> stakes = new ArrayList<>(Arrays.asList(parser.getActionParameters().split(" ")));

                        handleWrite(MessageParser.Action.BID, stakes.get(1), client);
                    }
                    case "e" -> handleWrite(MessageParser.Action.EVAL, "", client);
                    case "f" -> handleWrite(MessageParser.Action.FOLD, "", client);
                    case "h" -> handleWrite(MessageParser.Action.HAND, "", client);
                }

            }
        }
    }

    public static boolean gameEnded(SocketChannel client) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Game has ended.\n" +
                "d - if you want to disconnect.\n" +
                "p - if you want to play on.");

        String action = scanner.nextLine().trim();

        if (action.equals("d")) {
            disconnect(client);
            return false;
        }

        if (action.equals("p"))
            return true;

        System.out.println("Incorrect input.");
        return gameEnded(client);
    }


    public static void clientNegotiation() {

    }


}
