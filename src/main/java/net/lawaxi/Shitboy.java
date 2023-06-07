package net.lawaxi;

import cn.hutool.cron.CronUtil;
import net.lawaxi.handler.*;
import net.lawaxi.model.EndTime;
import net.lawaxi.model.Pocket48SenderCache;
import net.lawaxi.util.ConfigOperator;
import net.lawaxi.util.Properties;
import net.lawaxi.util.sender.*;
import net.lz1998.cq.CQGlobal;
import net.lz1998.cq.robot.CoolQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class Shitboy {
    public static Shitboy INSTANCE = new Shitboy();
    private final ConfigOperator configOperator = new ConfigOperator();
    private final Properties properties = new Properties();
    public Pocket48Handler handlerPocket48;
    public BilibiliHandler handlerBilibili;
    public WeiboHandler handlerWeibo;
    public WeidianHandler handlerWeidian;
    public WeidianSenderHandler handlerWeidianSender;

    public Shitboy() {
    }

    public void init(){
        initProperties();
        loadConfig();
        listenBroadcast();
    }

    private void initProperties() {
        properties.configData = new File("/www/wwwroot/mirai/config/net.lawaxi.shitboy/config.setting");
        handlerPocket48 = new Pocket48Handler();
        handlerBilibili = new BilibiliHandler();
        handlerWeibo = new WeiboHandler();
        handlerWeidian = new WeidianHandler();
        handlerWeidianSender = new WeidianSenderHandler();
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(Shitboy.class);
    }

    public ConfigOperator getConfig() {
        return configOperator;
    }

    public Properties getProperties() {
        return properties;
    }

    public Pocket48Handler getHandlerPocket48() {
        return handlerPocket48;
    }

    public BilibiliHandler getHandlerBilibili() {
        return handlerBilibili;
    }

    public WeiboHandler getHandlerWeibo() {
        return handlerWeibo;
    }

    public WeidianHandler getHandlerWeidian() {
        return handlerWeidian;
    }

    public WeidianSenderHandler getHandlerWeidianSender() {
        return handlerWeidianSender;
    }

    private void loadConfig() {
        configOperator.load(properties);
    }

    public void listenBroadcast() {
        CronUtil.getScheduler().clear();
        //------------------------------------------------

        //口袋48登录
        boolean pocket48_has_login = this.handlerPocket48.login(
                properties.pocket48_account,
                properties.pocket48_password
        );

        boolean weibo_has_login = false;
        try {
            this.handlerWeibo.updateLoginToSuccess();
            weibo_has_login = true;
            getLogger().info("微博Cookie更新成功");

        } catch (Exception e) {
            getLogger().info("微博Cookie更新失败");
        }

        //服务

        //endTime: 已发送房间消息的最晚时间
        HashMap<Long, HashMap<Long, Long>> pocket48RoomEndTime = new HashMap<>();
        HashMap<Long, HashMap<String, Long>> weiboEndTime = new HashMap<>(); //同时包含超话和个人(long -> String)
        HashMap<Long, EndTime> weidianEndTime = new HashMap<>();
        //status: 上次检测的开播状态
        HashMap<Long, HashMap<Long, List<Long>>> pocket48VoiceStatus = new HashMap<>();
        HashMap<Long, HashMap<Integer, Boolean>> bilibiliLiveStatus = new HashMap<>();

        //服务
        for (CoolQ b : CQGlobal.robots.values()) {
            if (pocket48_has_login) {
                handlerPocket48.setCronScheduleID(CronUtil.schedule(properties.pocket48_pattern, new Runnable() {
                            @Override
                            public void run() {
                                HashMap<Long, Pocket48SenderCache> cache = new HashMap();

                                for (long group : properties.pocket48_subscribe.keySet()) {
                                    if (b.getGroupInfo(group, true) == null)
                                        continue;

                                    if (!pocket48RoomEndTime.containsKey(group))//放到Runnable里面是因为可能实时更新新的群
                                    {
                                        pocket48RoomEndTime.put(group, new HashMap<>());
                                        pocket48VoiceStatus.put(group, new HashMap<>());
                                    }

                                    new Pocket48Sender(b, group, pocket48RoomEndTime.get(group), pocket48VoiceStatus.get(group), cache).start();

                                }

                            }
                        }
                ));
            }

            handlerBilibili.setCronScheduleID(CronUtil.schedule(properties.bilibili_pattern, new Runnable() {
                        @Override
                        public void run() {
                            for (long group : properties.bilibili_subscribe.keySet()) {
                                if (b.getGroupInfo(group, true) == null)
                                    continue;

                                if (!bilibiliLiveStatus.containsKey(group))
                                    bilibiliLiveStatus.put(group, new HashMap<>());

                                new BilibiliSender(b, group, bilibiliLiveStatus.get(group)).start();
                            }
                        }
                    }
            ));

            if (weibo_has_login) {
                handlerWeibo.setCronScheduleID(CronUtil.schedule(properties.weibo_pattern, new Runnable() {
                            @Override
                            public void run() {
                                for (long group : properties.weibo_user_subscribe.keySet()) {
                                    if (b.getGroupInfo(group, true) == null)
                                        continue;

                                    if (!weiboEndTime.containsKey(group))
                                        weiboEndTime.put(group, new HashMap<>());

                                    new WeiboSender(b, group, weiboEndTime.get(group)).start();
                                }
                            }
                        }
                ));
            }

            //微店订单播报
            CronUtil.schedule(properties.weidian_pattern_order, new Runnable() {
                        @Override
                        public void run() {
                            getLogger().info("10");
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (b.getGroupInfo(group, true) == null)
                                    continue;

                                if (!weidianEndTime.containsKey(group))
                                    weidianEndTime.put(group, new EndTime(new Date().getTime()));

                                new WeidianOrderSender(b, group, weidianEndTime.get(group), handlerWeidianSender).start();
                            }
                        }
                    }
            );

            //微店排名统计
            handlerWeidian.setCronScheduleID(CronUtil.schedule(properties.weidian_pattern_item, new Runnable() {
                        @Override
                        public void run() {
                            getLogger().info("5");
                            for (long group : properties.weidian_cookie.keySet()) {
                                if (b.getGroupInfo(group, true) == null)
                                    continue;

                                new WeidianSender(b, group, handlerWeidianSender).start();
                            }
                        }
                    }
            ));
        }

        //------------------------------------------------
        if (properties.enable) {
            CronUtil.start();
        } else {
            //停止
        }
    }
}