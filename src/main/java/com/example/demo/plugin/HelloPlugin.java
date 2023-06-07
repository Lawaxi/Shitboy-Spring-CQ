package com.example.demo.plugin;

import net.lawaxi.Shitboy;
import net.lz1998.cq.event.message.CQPrivateMessageEvent;
import net.lz1998.cq.robot.CQPlugin;
import net.lz1998.cq.robot.CoolQ;
import org.springframework.stereotype.Component;

@Component
public class HelloPlugin extends CQPlugin {
    @Override
    public int onPrivateMessage(CoolQ cq, CQPrivateMessageEvent event) {
        if(event.getUserId()==2330234142L && event.getMessage().equals("load")){
            Shitboy.INSTANCE.init();
        }

        if(event.getUserId()==2330234142L){
            // 不管收到什么都回复hello
            cq.sendPrivateMsg(event.getUserId(), "test", false);
        }
        return MESSAGE_IGNORE;
    }
}
