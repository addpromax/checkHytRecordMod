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
        Collection<NetworkPlayerInfo> playerInfoMap = Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap();
        int size = playerInfoMap.size();
        if (size > 32) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.RED + "当前服务器人数为" + size + "人。人数过多，为防止误操作，已取消查询"));
            return;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // 创建一个新的ArrayList来存放没有记录的玩家
        List<String> noRecordPlayers = new ArrayList<>();
        // 创建一个新的ArrayList来存放mvp率大于50%，或者胜率大于70%的玩家
        List<BedWarsRecord> highRecordPlayersRecord = new ArrayList<>();


        String selfName = mc.player.getName();

        for (NetworkPlayerInfo networkPlayerInfo : playerInfoMap) {
            String playerName = networkPlayerInfo.getGameProfile().getName();
            LOGGER.info("now check " + playerName);
            if (selfName.equals(playerName)) {
                continue;
            }
            String encodedPlayerName;
            try {
                encodedPlayerName = URLEncoder.encode(playerName, String.valueOf(StandardCharsets.UTF_8));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String bedWarsQuestUrl = "https://mc-api.16163.com/search/bedwars.html?uid=" + encodedPlayerName;
            futures.add(CompletableFuture.runAsync(() -> {
                BedWarsRecord bedWarsRecord = CheckRecord.getRecord(bedWarsQuestUrl);
                if (bedWarsRecord != null) {
                    bedWarsRecord.setPlayerName(playerName);
                }
                if (bedWarsRecord == null) {
                    // 如果没有查询到记录，就将玩家添加到列表中
                    noRecordPlayers.add(playerName);
                    LOGGER.info(playerName + " no record");
                } else if (bedWarsRecord.getMvpRatePercent() > 50 || bedWarsRecord.getWinRatePercent() > 70) {
                    highRecordPlayersRecord.add(bedWarsRecord);
                    LOGGER.info(playerName + " high record");
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (!noRecordPlayers.isEmpty()) {
                GUIMessage.printMessage(TextFormatting.RED, "以下玩家没有记录，可能为新玩家：" + String.join(", ", noRecordPlayers));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_ANVIL_PLACE, 1.0f));
            }
            if (noRecordPlayers.isEmpty()) {
                GUIMessage.printMessage(TextFormatting.GREEN, "大家都是绿色玩家呢!\n");
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
            }
            if (!highRecordPlayersRecord.isEmpty()) {
                GUIMessage.printMessage(TextFormatting.RED, "以下玩家可能是高手：");
                for (BedWarsRecord record : highRecordPlayersRecord) {
                    CheckRecord.printPlayerRecord(record);
                }
            }
        });
    }
}