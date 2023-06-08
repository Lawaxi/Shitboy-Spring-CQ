package net.lawaxi.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import net.lawaxi.Shitboy;
import net.lawaxi.model.Pocket48Subscribe;
import net.lawaxi.model.WeidianCookie;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigOperator {

    private Setting setting;
    private Properties properties;

    public void load(Properties properties) {

        this.properties = properties;
        File file = properties.configData;
        if (!file.exists()) {
            FileUtil.touch(file);
            Setting setting = new Setting(file, StandardCharsets.UTF_8, false);
            setting.set("enable", "true");
            setting.set("ylg", "true");
            setting.set("admins", "2330234142");
            setting.set("secureGroup", "");

            JSONObject object = new JSONObject();
            object.set("1", 1234567);
            object.set("2", "欢迎新宝宝");
            setting.set("welcome", "[" + object + "]");

            //schedule pattern
            setting.setByGroup("schedule", "pocket48", "* * * * *");
            setting.setByGroup("schedule", "bilibili", "* * * * *");
            setting.setByGroup("schedule", "weibo", "*/5 * * * *");
            setting.setByGroup("schedule_order", "weidian", "*/10 * * * *");
            setting.setByGroup("schedule_item", "weidian", "*/10 * * * *");

            //口袋48
            setting.setByGroup("account", "pocket48", "");
            setting.setByGroup("password", "pocket48", "");
            setting.setByGroup("token", "pocket48", "");

            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("showAtOne", true);
            object.set("starSubs", new long[]{});
            object.set("roomSubs", new long[]{});
            setting.setByGroup("subscribe", "pocket48",
                    "[" + object + "]");

            //bilibili
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("subscribe", new int[]{});
            setting.setByGroup("subscribe", "bilibili",
                    "[" + object + "]");

            //微博
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("userSubs", new long[]{});
            object.set("superTopicSubs", new String[]{});
            setting.setByGroup("subscribe", "weibo",
                    "[" + object + "]");

            //微店
            object = new JSONObject();
            object.set("qqGroup", 1234567);
            object.set("cookie", "");
            object.set("autoDeliver", false);
            object.set("highlight", "[]");
            setting.setByGroup("shops", "weidian", "[" + object + "]");

            setting.store();
            Shitboy.INSTANCE.getLogger().info("首次加载已生成 config/net.lawaxi.shitboy/config.setting 配置文件，请先填写口袋48账号密码用于获取房间消息并重启");
        }

        this.setting = new Setting(file, StandardCharsets.UTF_8, false);
        init();
    }

    public Setting getSetting() {
        return setting;
    }

    public void init() {
        properties.enable = setting.getBool("enable", true);
        properties.ylg = setting.getBool("ylg", true);
        properties.admins = setting.getStrings("admins");
        properties.secureGroup = setting.getStrings("secureGroup");
        if (properties.admins == null)
            properties.admins = new String[]{};
        if (properties.secureGroup == null)
            properties.secureGroup = new String[]{};

        for (Object a :
                JSONUtil.parseArray(setting.getStr("welcome", "[]")).toArray()) {
            JSONObject welcome = JSONUtil.parseObj(a);
            properties.welcome.put(
                    welcome.getLong("1"),
                    welcome.getStr("2")
            );
        }

        //schedule pattern
        properties.pocket48_pattern = setting.getStr("schedule", "pocket48", "* * * * *");
        properties.bilibili_pattern = setting.getStr("schedule", "pocket48", "* * * * *");
        properties.weibo_pattern = setting.getStr("schedule", "weibo", "*/5 * * * *");
        properties.weidian_pattern_order = setting.getStr("schedule_order", "weidian", "*/10 * * * *");
        properties.weidian_pattern_item = setting.getStr("schedule_item", "weidian", "*/10 * * * *");

        //口袋48
        properties.pocket48_account = setting.getStr("account", "pocket48", "");
        properties.pocket48_password = setting.getStr("password", "pocket48", "");
        properties.pocket48_token = setting.getStr("token", "pocket48", "");
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "pocket48")).toArray()) {
            JSONObject sub = JSONUtil.parseObj(a);
            Object rooms = sub.getBeanList("roomSubs", Long.class);
            Object stars = sub.getBeanList("starSubs", Long.class);

            properties.pocket48_subscribe
                    .put(sub.getLong("qqGroup"),
                            new Pocket48Subscribe(
                                    sub.getBool("showAtOne", true),
                                    rooms == null ? new ArrayList<>() : (List<Long>) rooms,
                                    stars == null ? new ArrayList<>() : (List<Long>) stars
                            ));
        }

        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("roomConnection", "pocket48")).toArray()) {
            JSONObject sid = JSONUtil.parseObj(a);
            properties.pocket48_serverID.put(sid.getLong("roomID"), sid.getLong("serverID"));
        }

        //bilibili
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "bilibili")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);

            Object subss = subs.getBeanList("subscribe", Integer.class);
            properties.bilibili_subscribe
                    .put(subs.getLong("qqGroup"),
                            subss == null ? new ArrayList<>() : (List<Integer>) subss);
        }

        //微博
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("subscribe", "weibo")).toArray()) {
            JSONObject subs = JSONUtil.parseObj(a);

            long g = subs.getLong("qqGroup");
            List userSubs = subs.getBeanList("userSubs", Long.class);
            properties.weibo_user_subscribe.put(g, userSubs == null ? new ArrayList<>() : userSubs);

            List sTopicSubs = subs.getBeanList("superTopicSubs", String.class);
            properties.weibo_superTopic_subscribe.put(g, sTopicSubs == null ? new ArrayList<>() : sTopicSubs);

        }

        //微店
        for (Object a :
                JSONUtil.parseArray(setting.getByGroup("shops", "weidian")).toArray()) {
            JSONObject shop = JSONUtil.parseObj(a);

            long g = shop.getLong("qqGroup");
            String cookie = shop.getStr("cookie", "");
            boolean autoDeliver = shop.getBool("autoDeliver", false);
            List<Long> highlight = shop.getBeanList("highlight", Long.class);
            properties.weidian_cookie.put(g, WeidianCookie.construct(cookie, autoDeliver,
                    highlight == null ? new ArrayList<>() : highlight));

        }
    }

    public boolean setAndSaveToken(String token) {
        properties.pocket48_token = token;
        setting.setByGroup("token", "pocket48", token);
        setting.store();
        return true;
    }
}
