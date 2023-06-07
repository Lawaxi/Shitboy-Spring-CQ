package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.WeidianHandler;
import net.lawaxi.handler.WeidianSenderHandler;
import net.lawaxi.model.WeidianCookie;
import net.lawaxi.model.WeidianItem;
import net.lz1998.cq.robot.CoolQ;

import java.util.ArrayList;
import java.util.List;

public class WeidianSender extends Sender {

    private final WeidianSenderHandler handler;

    public WeidianSender(CoolQ bot, long group, WeidianSenderHandler handler) {
        super(bot, group);
        this.handler = handler;
    }


    @Override
    public void run() {
        WeidianHandler weidian = Shitboy.INSTANCE.getHandlerWeidian();
        WeidianCookie cookie = Shitboy.INSTANCE.getProperties().weidian_cookie.get(group);

        WeidianItem[] items = weidian.getItems(cookie);
        if (items == null)
            return;

        //合并发送（仅特殊链）
        List<String> messages = new ArrayList<>();
        for (WeidianItem item : items) {
            if (cookie.highlightItem.contains(item.id)) {
                messages.add(handler.executeItemMessages(item, group));
            }
        }
        String t = combine(messages);
        if (t != null)
            bot.sendGroupMsg(group, t, false);
    }

}
