package com.yshs.hytcheckrecord.commands;

import com.google.gson.Gson;
import com.yshs.hytcheckrecord.message.GUIMessage;
import com.yshs.hytcheckrecord.records.BedWarsRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckRecord {

    public static BedWarsRecord getRecord(String questUrl) {
        try {
            URL url = new URL(questUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_NOT_FOUND) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                conn.disconnect();
                Gson gson = new Gson();
                return gson.fromJson(content.toString(), BedWarsRecord.class);
            } else {
                return null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void printPlayerRecord(BedWarsRecord bedWarsRecord) {
        String playerName = bedWarsRecord.getPlayerName();
        Minecraft mc = Minecraft.getMinecraft();
        GUIMessage.printMessage(TextFormatting.GREEN, "玩家" + playerName + "的起床战争记录如下：");
        //总场次
        int playNum = bedWarsRecord.getPlayNum();
        //胜场
        int winNum = bedWarsRecord.getWinNum();
        //胜率
        double winRatePercent = bedWarsRecord.getWinRatePercent();
        //MVP次数
        int mvpNum = bedWarsRecord.getMvpNum();
        //MVP率
        double mvpRatePercent = bedWarsRecord.getMvpRatePercent();
        //击杀/死亡
        double killDead = bedWarsRecord.getKillDead();
        //破坏床总数
        int beddesNum = bedWarsRecord.getBeddesNum();

        GUIMessage.printMessage(TextFormatting.RED, "总场次：" + playNum);
        GUIMessage.printMessage(TextFormatting.RED, "胜场/胜率：" + winNum + "/" + String.format("%.2f", winRatePercent) + "%");
        GUIMessage.printMessage(TextFormatting.RED, "MVP次数/MVP率：" + mvpNum + "/" + String.format("%.2f", mvpRatePercent) + "%");
        GUIMessage.printMessage(TextFormatting.RED, "击杀/死亡：" + String.format("%.2f", killDead));
        GUIMessage.printMessage(TextFormatting.RED, "破坏床总数：" + beddesNum);
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
    }

    @SubscribeEvent
    public void execute(ClientChatEvent event) {

        Minecraft mc = Minecraft.getMinecraft();
        String message = event.getMessage();
        Pattern pattern = Pattern.compile("/cr(.*)|(.*)/cr");
        Matcher matcher = pattern.matcher(message);
        String playerName;
        if (matcher.matches()) {
            event.setCanceled(true);
            if (matcher.group(1) != null) {
                playerName = matcher.group(1);
            } else {
                playerName = matcher.group(2);
            }
            playerName = playerName.replaceAll(" ", "");
        } else {
            return;
        }
        if (playerName.isEmpty()) {
            GUIMessage.printMessage(TextFormatting.RED, "格式：「/cr 玩家名」 或 「玩家名/cr」(有没有空格都可以)");
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_ANVIL_PLACE, 1.0f));
            return;
        }
        String encodedPlayerName;
        try {
            encodedPlayerName = URLEncoder.encode(playerName, String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String finalPlayerName = playerName;
        CompletableFuture.runAsync(()->{
            String bedWarsQuestUrl = "https://mc-api.16163.com/search/bedwars.html?uid=" + encodedPlayerName;
            BedWarsRecord bedWarsRecord = getRecord(bedWarsQuestUrl);
            if (bedWarsRecord != null) {
                bedWarsRecord.setPlayerName(finalPlayerName);
            }
            if (bedWarsRecord == null) {
                GUIMessage.printMessage(TextFormatting.RED, "玩家" + finalPlayerName + "没有记录，可能为新玩家");
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_ANVIL_PLACE, 1.0f));
            } else {
                printPlayerRecord(bedWarsRecord);
            }
        });
    }
}

