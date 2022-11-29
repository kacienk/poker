package pl.edu.agh.kis.pz1;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class parsing messages sent between server and client.
 */
public class MessageParser {
    /**
     * Arguments parsed from messages.
     */
    private final ArrayList<String> args = new ArrayList<>();

    /**
     * Actions used in communication.
     * ACCEPT - accepted request received from sender.
     * DENY - denied request received from sender.
     * BID - bid request.
     * FOLD - fold request sent by the client.
     * HAND - client want to get hand or server sends hand.
     * START - server announcing start of the new game.
     * DISCONNECT - server or player disconnected.
     * EVAL - client want to get evaluation or server sends evaluation.
     * END - server announcing that game has ended.
     * DRAW - draw request.
     * CREDIT - client want to get credit or server sends credit.
     * PRIZE - information about the prize from the server.
     */
    public enum Action {ACCEPT, DENY, BID, FOLD, HAND, START, DISCONNECT, EVAL, END, DRAW, CREDIT, PRIZE}

    MessageParser() {

    }

    /**
     * Parses message while created.
     *
     * @param message Message to parse.
     */
    MessageParser(String message) {
        parse(message);
    }

    /**
     * Parses message.
     *
     * @param message Message to parse.
     */
    public void parse(String message) {
        args.clear();
        args.addAll(Arrays.asList(message.split("/")));
    }

    /**
     * Parses arguments to the message format.
     *
     * @param gameId ID of the game related to messsage.
     * @param playerId ID of the player receiving or sending message.
     * @param action Type of action.
     * @param actionParameters Parameters for the action.
     * @return
     */
    public String parse(int gameId, int playerId, Action action, String actionParameters) {
        args.clear();

        args.add(String.valueOf(gameId));
        args.add(String.valueOf(playerId));

        switch (action) {
            case ACCEPT -> args.add("acc");
            case DENY -> args.add("den");
            case BID -> args.add("bid");
            case FOLD -> args.add("fol");
            case HAND -> args.add("han");
            case START -> args.add("srt");
            case DISCONNECT -> args.add("dsc");
            case EVAL -> args.add("evl");
            case END -> args.add("end");
            case DRAW -> args.add("drw");
            case CREDIT -> args.add("crd");
            case PRIZE -> args.add("prz");
        }

        args.add(actionParameters);

        return getMessage();
    }

    public int getGameId() {
        return Integer.parseInt(args.get(0));
    }

    public int getPlayerId() {
        return Integer.parseInt(args.get(1));
    }

    public Action getActionType() {
        switch (args.get(2)) {
            case "acc" -> { return Action.ACCEPT; }
            case "bid" -> { return Action.BID; }
            case "fol" -> { return Action.FOLD; }
            case "han" -> { return Action.HAND; }
            case "srt" -> { return Action.START; }
            case "dsc" -> { return Action.DISCONNECT; }
            case "evl" -> { return Action.EVAL; }
            case "end" -> { return Action.END; }
            case "drw" -> { return Action.DRAW; }
            case "crd" -> { return Action.CREDIT; }
            case "prz" -> { return Action.PRIZE; }
            default -> { return Action.DENY; }
        }
    }

    public String getActionParameters() {
        try {
            return args.get(3);
        }
        catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    private String getMessage() {
        String message = "";
        StringBuilder stringBuilder = new StringBuilder(message);

        for (int i = 0; i < args.size(); i++) {
            stringBuilder.append(args.get(i));

            if (i < args.size() - 1)
                stringBuilder.append("/");
        }

        message = stringBuilder.toString();

        return message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
