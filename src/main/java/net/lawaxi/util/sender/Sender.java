package net.lawaxi.util.sender;

import net.lz1998.cq.robot.CoolQ;

import java.util.List;

public class Sender extends Thread { //异步进程
    public final CoolQ bot;
    public final long group;

    public Sender(CoolQ bot, long group) {
        this.bot = bot;
        this.group = group;
    }

    //snapshot
    public String toNotification(String ori) {
        return ori;
    }

    //snapshot
    public String getImgRes(String resLoc) {
        return "[CQ:image,file=" + resLoc + "]";
    }

    public String getAudioRes(String resLoc) {
        return "[CQ:record,file=" + resLoc + "]";
    }

    public String getVideoRes(String resLoc) {
        return "[CQ:video,file=" + resLoc + "]";
    }

    public String combine(List<String> messages) {
        if(messages.size() == 0)
            return null;

        String a = messages.get(0);
        for (int i = 1; i < messages.size(); i++) {
            a += "\n+++++++++" + messages.get(i);
        }
        return a;
    }
}
