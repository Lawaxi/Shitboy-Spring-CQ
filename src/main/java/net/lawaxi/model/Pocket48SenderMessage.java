package net.lawaxi.model;

public class Pocket48SenderMessage {

    private final boolean canJoin;
    private final String title;
    private final String[] message;
    private boolean specific = false;//第一条消息可以合并


    public Pocket48SenderMessage(boolean canJoin, String title, String[] message) {
        this.canJoin = canJoin;
        this.title = title;
        this.message = message;
    }

    public boolean canJoin() {
        return canJoin;
    }

    public String[] getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String getUnjointMessage() {
        String o = title;
        for (String m : message) {
            o += m;
        }
        return o;
    }

    public boolean isSpecific() {
        return specific;
    }

    public Pocket48SenderMessage setSpecific() {
        this.specific = true;
        return this;
    }
}
