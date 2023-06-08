package com.example.demo.plugin;

import net.lawaxi.Shitboy;
import net.lz1998.cq.event.message.CQGroupMessageEvent;
import net.lz1998.cq.event.message.CQPrivateMessageEvent;
import net.lz1998.cq.robot.CQPlugin;
import net.lz1998.cq.robot.CoolQ;
import org.springframework.stereotype.Component;

@Component
public class ShitBoyPlugin extends CQPlugin {

    @Override
    public int onPrivateMessage(CoolQ cq, CQPrivateMessageEvent event) {
        if(event.getSender().getUserId() == 2330234142L && event.getMessage().equals("load"))
            Shitboy.INSTANCE.init();
        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(CoolQ cq, CQGroupMessageEvent event) {
        return MESSAGE_IGNORE;
    }
}
