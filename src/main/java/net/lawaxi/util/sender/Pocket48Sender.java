package net.lawaxi.util.sender;

import net.lawaxi.Shitboy;
import net.lawaxi.handler.Pocket48Handler;
import net.lawaxi.model.*;
import net.lz1998.cq.robot.CoolQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pocket48Sender extends Sender {

    //endTime是一个关于roomID的HashMap
    private final HashMap<Long, Long> endTime;
    private final HashMap<Long, List<Long>> voiceStatus;
    private final HashMap<Long, Pocket48SenderCache> cache;

    public Pocket48Sender(CoolQ bot, long group, HashMap<Long, Long> endTime, HashMap<Long, List<Long>> voiceStatus, HashMap<Long, Pocket48SenderCache> cache) {
        super(bot, group);
        this.endTime = endTime;
        this.voiceStatus = voiceStatus;
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            Pocket48Subscribe subscribe = Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group);
            Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();

            List<Pocket48Message[]> totalMessages = new ArrayList<>();


            for (long roomID : subscribe.getRoomIDs()) {
                if (!cache.containsKey(roomID)) {
                    cache.put(roomID, Pocket48SenderCache.create(roomID, endTime));
                }
            }

            for (long roomID : subscribe.getRoomIDs()) {
                if (cache.get(roomID) == null)
                    continue;

                Pocket48RoomInfo roomInfo = cache.get(roomID).roomInfo;

                //房间消息预处理
                Pocket48Message[] a = cache.get(roomID).messages;
                if (a.length > 0) {
                    totalMessages.add(a);
                }

                //房间语音
                List<Long> n = cache.get(roomID).voiceList;
                if (voiceStatus.containsKey(roomID)) {
                    String[] r = handleVoiceList(voiceStatus.get(roomID), n);
                    if (r[0] != null || r[1] != null) {
                        String ownerName = pocket.getOwnerOrTeamName(roomInfo);
                        boolean private_ = ownerName.equals(roomInfo.getOwnerName());
                        String message = "【" + roomInfo.getRoomName() + "(" + ownerName + ")房间语音】\n";

                        if (r[0] != null) {
                            message += private_ ?
                                    "上麦啦~" //成员房间
                                    : "★ 上麦：\n" + r[0] + "\n"; //队伍房间
                        }
                        if (r[1] != null) {
                            message += private_ ?
                                    "下麦了捏~"
                                    : "☆ 下麦：\n" + r[1];
                        }
                        bot.sendGroupMsg(group, message, false);
                    }
                }
                voiceStatus.put(roomID, n);
            }

            //房间消息
            if (totalMessages.size() > 0) {
                if (Shitboy.INSTANCE.getProperties().pocket48_subscribe.get(group).showAtOne()) {

                    for (Pocket48Message[] roomMessage : totalMessages) {
                        String joint = null;
                        String title = null;
                        for (int i = roomMessage.length - 1; i >= 0; i--) {
                            try {
                                Pocket48SenderMessage message1 = pharseMessage(roomMessage[i], group, subscribe.getRoomIDs().size() == 1);
                                if (message1 == null)
                                    continue;

                                if (message1.canJoin() || message1.isSpecific()) {
                                    if (joint == null) {
                                        title = joint = message1.getTitle();
                                    } else if (!title.equals(message1.getTitle())) {
                                        joint += message1.getTitle();
                                        title = message1.getTitle();
                                    }
                                    joint += message1.getMessage()[0] + "\n";
                                }

                                if (!message1.canJoin()) {
                                    //遇到不可组合的消息先发送以前的可组合消息
                                    if (joint != null) {
                                        bot.sendGroupMsg(group, joint, false);
                                        joint = null;
                                        title = null;
                                    }

                                    //不可组合消息的发送需要通过for循环完成
                                    for (int j = (message1.isSpecific() ? 1 : 0); j < message1.getMessage().length; j++) {
                                        bot.sendGroupMsg(group, message1.getMessage()[j], false);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (joint != null) {
                            bot.sendGroupMsg(group, joint, false);
                            joint = null;
                            title = null;
                        }
                    }


                } else {
                    for (Pocket48Message[] roomMessage : totalMessages) {
                        for (int i = roomMessage.length - 1; i >= 0; i--) { //倒序输出
                            try {
                                bot.sendGroupMsg(group, pharseMessage(roomMessage[i], group, subscribe.getRoomIDs().size() == 1).getUnjointMessage(), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] handleVoiceList(List<Long> a, List<Long> b) {
        String zengjia = "";
        String jianshao = "";
        for (Long b0 : b) {
            if (!a.contains(b0))
                zengjia += "，" + b0;
        }
        for (Long a0 : a) {
            if (!b.contains(a0))
                jianshao += "，" + a0;
        }
        return new String[]{(zengjia.length() > 0 ? zengjia.substring(1) : null),
                (jianshao.length() > 0 ? jianshao.substring(1) : null)};
    }

    public Pocket48SenderMessage pharseMessage(Pocket48Message message, long group, boolean single_subscribe) throws IOException {
        Pocket48Handler pocket = Shitboy.INSTANCE.getHandlerPocket48();
        String n = message.getNickName() + (message.getNickName().indexOf(message.getStarName()) != -1 ? "" : "(" + message.getStarName() + ")");
        String r = message.getRoom() + "(" + message.getOwnerName() + ")";
        String name = "【" + n + "@" + r + "】\n";

        switch (message.getType()) {
            case TEXT:
                return new Pocket48SenderMessage(true, name, new String[]{pharsePocketTextWithFace(message.getBody())});
            case AUDIO: {
                return single_subscribe ? new Pocket48SenderMessage(false, null,
                        new String[]{getAudioRes(message.getResLoc())}) : new Pocket48SenderMessage(false, name,
                        new String[]{"发送了一条语音\n", getAudioRes(message.getResLoc())}).setSpecific();
            }
            case IMAGE:
            case EXPRESSIMAGE: {
                return new Pocket48SenderMessage(true, name,
                        new String[]{getImgRes(message.getResLoc())});
            }
            case VIDEO:
                return single_subscribe ? new Pocket48SenderMessage(false, null,
                        new String[]{getVideoRes(message.getResLoc())}) : new Pocket48SenderMessage(false, name,
                        new String[]{"发送了一条视频\n", getVideoRes(message.getResLoc())}).setSpecific();
            case REPLY:
            case GIFTREPLY:
                return new Pocket48SenderMessage(false, null,
                        new String[]{message.getReply().getNameTo() + "：" + pharsePocketTextWithFace(message.getReply().getMsgTo()) + "\n"
                                + name + pharsePocketTextWithFace(message.getReply().getMsgFrom())});
            case LIVEPUSH:
                return new Pocket48SenderMessage(false, null,
                        new String[]{toNotification("【" + (single_subscribe ? "" : message.getOwnerName()) + "口袋48开播啦~】\n"
                                + message.getLivePush().getTitle() + getImgRes(message.getLivePush().getCover()))});
            case FLIPCARD:
                return new Pocket48SenderMessage(false, null, new String[]{"【" + (single_subscribe ? "" : message.getOwnerName()) + "翻牌回复消息】\n"
                        + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                        + "\n------\n"
                        + message.getAnswer().getAnswer()});
            case FLIPCARD_AUDIO:
                return new Pocket48SenderMessage(false, null,
                        new String[]{"【" + (single_subscribe ? "" : message.getOwnerName()) + "语音翻牌回复消息】\n"
                                + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                                + "\n------\n", getAudioRes(message.getAnswer().getResInfo())});
            case FLIPCARD_VIDEO:
                return new Pocket48SenderMessage(false, null,
                        new String[]{"【" + (single_subscribe ? "" : message.getOwnerName()) + "视频翻牌回复消息】\n"
                                + pocket.getAnswerNameTo(message.getAnswer().getAnswerID(), message.getAnswer().getQuestionID()) + "：" + message.getAnswer().getMsgTo()
                                + "------", getVideoRes(message.getAnswer().getResInfo())});
            case PASSWORD_REDPACKAGE:
                return new Pocket48SenderMessage(true, name,
                        new String[]{"红包信息"});
            case VOTE:
                return new Pocket48SenderMessage(true, name,
                        new String[]{"投票信息"});
        }

        return new Pocket48SenderMessage(true, name,
                new String[]{"不支持的消息"});
    }

    public String pharsePocketTextWithFace(String body) {
        String[] a = body.split("\\[.*?\\]", -1);//其余部分，-1使其产生空字符串
        if (a.length < 2)
            return body;

        String out = a[0];
        int count = 1;//从第1个表情后a[1]开始
        Matcher b = Pattern.compile("\\[.*?\\]").matcher(body);
        while (b.find()) {
            out += pharsePocketFace(b.group()) + a[count];
            count++;
        }

        return out;
    }

    //snapshot
    public String pharsePocketFace(String face) {
        if (face.equals("[亲亲]"))
            face = "[左亲亲]";

        return face;
    }

}
