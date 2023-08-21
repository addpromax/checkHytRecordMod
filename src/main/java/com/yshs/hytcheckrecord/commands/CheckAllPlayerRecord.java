package com.yshs.hytcheckrecord.commands;

import com.yshs.hytcheckrecord.message.GUIMessage;
import com.yshs.hytcheckrecord.records.BedWarsRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CheckAllPlayerRecord {
    private static final Logger LOGGER = LogManager.getLogger();

    //入口
    @SubscribeEvent
    public void execute(ClientChatEvent event) {

        Minecraft mc = Minecraft.getMinecraft();
        String message = event.getMessage();
        if (!message.contains("/ca")) {
            return;
        }
        event.setCanceled(true);
        mc.ingameGUI.getChatGUI().addToSentMessages(message);
        Collection<NetworkPlayerInfo> playerInfoMap = Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap();
        int size = playerInfoMap.size();
        if (size > 32) {
            GUIMessage.printMessage(TextFormatting.RED + "当前服务器人数为" + size + "人。人数过多，为防止误操作，已取消查询");
            return;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 创建一个新的ArrayList来存放没有记录的玩家
        List<String> noRecordPlayers = new ArrayList<>();
        // 创建一个新的ArrayList来存放kd>1的基础上，满足以下任意一个条件：mvp率大于50%，胜率大于70%的
        List<BedWarsRecord> highRecordPlayersList = new ArrayList<>();
        // 创建一个新的ArrayList来存放kd>1，总场次<50的基础上，满足以下任意一个条件：mvp率大于50%，胜率大于70%的，取名为dangerousPlayersList
        List<BedWarsRecord> dangerousPlayersList = new ArrayList<>();

        String selfName = mc.player.getName();

        // 循环检查每个玩家
        for (NetworkPlayerInfo networkPlayerInfo : playerInfoMap) {
            String playerName = networkPlayerInfo.getGameProfile().getName();
            LOGGER.info("now check " + playerName);
            // 如果是自己，就跳过
            if (selfName.equals(playerName)) {
                continue;
            }

            // url编码玩家名用来查询
            String encodedPlayerName;
            try {
                encodedPlayerName = URLEncoder.encode(playerName, String.valueOf(StandardCharsets.UTF_8));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String bedWarsQuestUrl = "https://mc-api.16163.com/search/bedwars.html?uid=" + encodedPlayerName;

            // 异步查询玩家记录
            futures.add(CompletableFuture.runAsync(() -> {
                //开始查询
                BedWarsRecord bedWarsRecord = CheckRecord.getRecord(bedWarsQuestUrl);
                if (bedWarsRecord != null) {
                    bedWarsRecord.setPlayerName(playerName);
                }
                // 如果没有查询到记录，就将玩家添加到无战绩列表中
                if (bedWarsRecord == null) {
                    noRecordPlayers.add(playerName);
                    LOGGER.info(playerName + " no record");
                    return;
                }
                if (bedWarsRecord.getKillDead() > 1 && (bedWarsRecord.getMvpRatePercent() > 50 || bedWarsRecord.getWinRatePercent() > 70)) {
                    //看看是不是危险玩家
                    if (bedWarsRecord.getPlayNum() < 50) {
                        dangerousPlayersList.add(bedWarsRecord);
                        LOGGER.info(playerName + " dangerous");
                        return;
                    }
                    if (bedWarsRecord.getPlayNum() >= 50) {
                        highRecordPlayersList.add(bedWarsRecord);
                        LOGGER.info(playerName + " high record");
                    }
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (!noRecordPlayers.isEmpty()) {
                GUIMessage.printMessage(TextFormatting.WHITE + TextFormatting.BOLD.toString() + "以下玩家没有记录，可能为新玩家: " + TextFormatting.RED + String.join(", ", noRecordPlayers));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_ANVIL_PLACE, 1.0f));
            }
            if (noRecordPlayers.isEmpty()) {
                GUIMessage.printMessage(TextFormatting.GREEN + TextFormatting.BOLD.toString() + "大家都是绿色玩家呢!");
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
            }
            //高手信息
            if (!highRecordPlayersList.isEmpty()) {
                GUIMessage.printSplitLine();
                ArrayList<String> highRecordPlayers = new ArrayList<>();
                for (BedWarsRecord record : highRecordPlayersList) {
                    highRecordPlayers.add(record.getPlayerName());
                }
                mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.WHITE + TextFormatting.BOLD.toString() + "以下玩家可能是高手: " + TextFormatting.LIGHT_PURPLE + String.join(", ", highRecordPlayers)));
                for (BedWarsRecord record : highRecordPlayersList) {
                    CheckRecord.printPlayerRecord(record);
                    if (highRecordPlayersList.indexOf(record) != highRecordPlayersList.size() - 1) {
                        GUIMessage.printSplitLine();
                    }
                }
            }
            //危险玩家信息
            if (!dangerousPlayersList.isEmpty()) {
                GUIMessage.printSplitLine();
                ArrayList<String> dangerousPlayers = new ArrayList<>();
                for (BedWarsRecord record : dangerousPlayersList) {
                    dangerousPlayers.add(record.getPlayerName());
                }
                GUIMessage.printMessage(TextFormatting.WHITE + TextFormatting.BOLD.toString() + "以下玩家可能是危险玩家: " + TextFormatting.DARK_RED + String.join(", ", dangerousPlayers));
                for (BedWarsRecord record : dangerousPlayersList) {
                    CheckRecord.printPlayerRecord(record);
                    if (dangerousPlayersList.indexOf(record) != dangerousPlayersList.size() - 1) {
                        GUIMessage.printSplitLine();
                    }
                }
            }
        });
    }
}