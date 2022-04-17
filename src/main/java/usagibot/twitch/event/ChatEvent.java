package usagibot.twitch.event;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.helix.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import usagibot.UsagiBot;
import usagibot.osu.objects.Beatmap;
import usagibot.twitch.TwitchClient;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChatEvent {

    private final String prefix = UsagiBot.getConfig().getPrefix();
    private final String channel = UsagiBot.getConfig().getTwitchChannel();
    private final String webHookPath = UsagiBot.getConfig().getGOsuUrlPath();
    private Beatmap beatmap;

    public ChatEvent(SimpleEventHandler eventHandler) {
        eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
    }

    @EventSubscriber
    public void onChannelMessage(ChannelMessageEvent event) {

        if (event.getUser().getName() == "RNRPBot") return;

        if (event.getMessage().equalsIgnoreCase(prefix + "np")) {
            log.info("Sending !np command in " + channel);
            try {
                sendMessage("Here you go! " + getSongFromGosuMemory().getUrl());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (event.getMessage().contains("https://osu.ppy.sh")) {
            log.info("Received possible osu song request. Parsing now...");
            beatmap = UsagiBot.getClient().getBeatmap(parseMessage(event.getMessage()));
            log.info("Beatmap ID Found: " + beatmap.getId());
            sendMessage("[Received] [" + beatmap.getStatus() + "] " + beatmap.getBeatmapset().getTitle() + " - " + beatmap.getBeatmapset().getArtist() +
                    " [" + beatmap.getVersion() + "] " + beatmap.getDifficulty_rating() + " ⭐ ");
            //UsagiBot.getIrcBot().getUserChannelDao().getUser("I_Only_Hit_100s").send().message("Map Received - " +
                    //"http://osu.ppy.sh/b/" + beatmap.getId());
        }

    }

    // Send a message to the chat
    public void sendMessage(String message) {
        TwitchClient.client.getChat().sendMessage(channel, message);
    }

    // Grabs the map digits
    public String parseMessage(String message) {
        String delimiters = "https?:\\/\\/osu.ppy.sh\\/(b|s|beatmapsets)\\/([0-9]*)(#osu|#taiko|#ctb|#maina)\\/([0-9]*)";
        Pattern urlPattern = Pattern.compile(delimiters, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = urlPattern.matcher(message);
        List<String> urlSplit = new ArrayList<String>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                urlSplit.add(matcher.group(i));
            }
        }

        switch (urlSplit.size()) {
            case 1:
            case 2:
            case 3:
            case 4:
                String beatmap = urlSplit.get(3);
                return beatmap;
            default:
                log.warn("Scoin0 has not yet implemented all ways to request a beatmap. Only Beatmap Sets work right now.");
        }
        return null;
    }

    // Grab the map ID from Gosumemory
    public Beatmap getSongFromGosuMemory() throws IOException {
        JSONObject t = (JSONObject) (new JSONTokener(IOUtils.toString((new URL(webHookPath)).openStream()))).nextValue();
        String beatmapID = t.getJSONObject("menu").getJSONObject("bm").get("id").toString();
        beatmap = UsagiBot.getClient().getBeatmap(beatmapID);
        return beatmap;
    }
}
