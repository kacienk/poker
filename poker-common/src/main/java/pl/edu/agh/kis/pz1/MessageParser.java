package pl.edu.agh.kis.pz1;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageParser {
    private final ArrayList<String> args = new ArrayList<>();

    public void parse(String message) {
        args.addAll(Arrays.asList(message.split("/")));
    }

    public int getGameId() {
        return Integer.parseInt(args.get(0));
    }

    public int getPlayerId() {
        return Integer.parseInt(args.get(1));
    }

    public String getMoveType() {
        return args.get(2);
    }

    public String getMoveParameters() {
        return args.get(3);
    }
}
