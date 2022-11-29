package pl.edu.agh.kis.pz1;

import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Arrays;

public class MessageParser {
    private final ArrayList<String> args = new ArrayList<>();
    public enum Action {ACCEPT, DENY, BID, FOLD, HAND, START, DISCONNECT, EVAL, END, DRAW, CREDIT, PRIZE}

    MessageParser() {

    }
    MessageParser(String message) {
        parse(message);
    }

    public void parse(String message) {
        args.clear();
        args.addAll(Arrays.asList(message.split("/")));
    }

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

    public String getMessage() {
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
