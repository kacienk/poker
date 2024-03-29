package pl.edu.agh.kis.pz1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class PokerClient is an application allowing connection with PokerServer and playing poker.
 *
 * @author Kacper Cienkosz
 */
public class PokerClient {
    private static SocketChannel client;
    private static int playerId = -1;
    private static int gameId = -1;

    /**
     * Method main handles all client functionality.
     *
     * @param args Arguments passed to the client.
     */
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 31415;

        client = connect(hostname, port);

        handleGames();

        disconnect();
    }

    private static SocketChannel connect(String hostname, int port) {
        SocketChannel client = null;

        try {
            System.out.println("Starting client...");

            client = SocketChannel.open(new InetSocketAddress(hostname, port));

            MessageParser parser = handleRead();

            if (parser.getActionType() == MessageParser.Action.DENY) {
                System.out.println("Unable to establish connection.");
                client.close();
                System.exit(1);
            }

            playerId = parser.getPlayerId();
            gameId = parser.getGameId();

            System.out.println("Connection has been established.\n" +
                    "Your player ID is: " + playerId + ".\n" +
                    "Your game ID is: " + gameId + ".");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return client;
    }

    private static void disconnect() {
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
        String data = "";

        try {
            while (data.isEmpty()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                client.read(buffer);

                data = new String(buffer.array()).trim();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        parser.parse(data);
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
            client.write(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGames() {
        while (true) {
            MessageParser parser = handleRead();

            switch (parser.getActionType()) {
                case START -> System.out.println("Game has started.");
                case HAND -> receiveHand(parser);
                case EVAL -> System.out.println("Your hand value is: " + parser.getActionParameters());
                case DENY -> System.out.println("Incorrect action: " + parser.getActionParameters());
                case CREDIT -> System.out.println("Your current credit is: " + parser.getActionParameters() + ".");
                case PRIZE -> handlePrize(parser);
                case BID -> {
                    System.out.println("It is time for bidding.");
                    List<String> stakes = Arrays.asList(parser.getActionParameters().split(" "));

                    int currentStake = Integer.parseInt(stakes.get(0));
                    int howMuchToBid = Integer.parseInt(stakes.get(1));

                    System.out.println("Current stake is: " + currentStake);
                    System.out.println("You must bid at least: " + howMuchToBid);

                    boolean correctAction = false;

                    while (!correctAction)
                        correctAction = handleBidding(parser, client);
                }
                case DRAW -> {
                    System.out.println("It is time for discarding cards.");
                    boolean correctAction = false;

                    while (!correctAction)
                        correctAction = handleDrawing(client);
                }
                case DISCONNECT -> {
                    serverDisconnected(client);
                    return;
                }
                case END -> {
                    if (!playerWantsToPlayOn(parser, client))
                        return;
                }
                default -> System.out.println("Unknown command from server.");
            }
        }
    }

    private static int gameEnded(MessageParser parser, SocketChannel client) {
        Scanner scanner = new Scanner(System.in);

        int playersMissing = Integer.parseInt(parser.getActionParameters());

        if (playersMissing == 0)
            System.out.println("Game ended!");
        else {
            System.out.println(playersMissing + " player(s) missing.");
            System.out.println("Do you want to wait or disconnect?");
        }

        System.out.println("""
                d - if you want to disconnect.
                p - if you want to play on.""");

        String action = scanner.nextLine().trim();

        if (action.equals("d"))
            return 1;


        if (action.equals("p")) {
            handleWrite(MessageParser.Action.ACCEPT, "", client);
            return 0;
        }

        System.out.println("Incorrect input.");
        return -1;
    }

    private static boolean handleBidding(MessageParser parser, SocketChannel client) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("""
                        What do you want to do?
                        b - bid.
                        c - check.
                        e - get evaluation.
                        f - fold.
                        h - get hand.
                        r - check your credit.
                        """);

        String action = scanner.nextLine().trim();

        switch (action) {
            case "b" -> {
                System.out.println("How much do you want to bid?");
                int bid = -1;

                while (bid == -1) {
                    Scanner s = new Scanner(System.in);
                    try {
                        int scanned = s.nextInt();

                        if (scanned >= 0)
                            bid = scanned;
                        else
                            System.out.println("Incorrect value. Bid should be greater or equal than 0.");
                    } catch (InputMismatchException e) {
                        System.out.println("Incorrect value. Your bid should be an integer greater or equal than 0.");
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                handleWrite(MessageParser.Action.BID, Integer.toString(bid), client);
            }
            case "c" -> {
                List<String> stakes = Arrays.asList(parser.getActionParameters().split(" "));

                handleWrite(MessageParser.Action.BID, stakes.get(1), client);
            }
            case "e" -> handleWrite(MessageParser.Action.EVAL, "", client);
            case "f" -> handleWrite(MessageParser.Action.FOLD, "", client);
            case "h" -> handleWrite(MessageParser.Action.HAND, "", client);
            case "r" -> handleWrite(MessageParser.Action.CREDIT, "", client);
            default -> {
                System.out.println("Incorrect input.");
                return false;
            }
        }

        return true;
    }

    private static boolean handleDrawing(SocketChannel client) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("""
                        What do you want to do?
                        d - discard cards.
                        e - get evaluation.
                        h - get hand.""");

        String action = scanner.nextLine().trim();

        switch (action) {
            case "d" -> {
                System.out.println("""
                        Type in cards you want to discard in format:
                        0 2 3
                        That means you want to discard first, third, and fourth card.
                        If you don't want to discard any card then just press enter.
                        """);

                String scanned = scanner.nextLine();

                handleWrite(MessageParser.Action.DRAW, scanned, client);
            }
            case "e" -> handleWrite(MessageParser.Action.EVAL, "", client);
            case "h" -> handleWrite(MessageParser.Action.HAND, "", client);
            default -> {
                System.out.println("Incorrect input.");
                return false;
            }
        }

        return true;
    }

    private static void handlePrize(MessageParser parser) {
        int prize = Integer.parseInt(parser.getActionParameters());

        if (prize > 0) {
            System.out.println("You won!");
            System.out.println("Your prize is: " + prize);
            return;
        }

        System.out.println("You did not win.");
    }

    private static void receiveHand(MessageParser parser) {
        System.out.println("Your hand:");
        System.out.println(parser.getActionParameters());
    }

    private static boolean playerWantsToPlayOn(MessageParser parser, SocketChannel client) {
        // Stands for user input
        // If -1 then input is incorrect
        // If 1 then player wants to disconnect
        // If 0 than user wants to play on
        int action = -1;

        while (action == -1)
            action = gameEnded(parser, client);

        return action != 1;
    }
}
