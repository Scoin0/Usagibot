package usagibot.osu.memreaders.gosu;

import com.fasterxml.jackson.databind.ObjectMapper;
import usagibot.UsagiBot;
import usagibot.osu.api.Beatmap;
import usagibot.osu.memreaders.IMemoryReader;
import usagibot.osu.memreaders.MemoryReaderConnections;

import java.io.IOException;

public class GOsuMemoryReader implements IMemoryReader {

    static Beatmap beatmap;

    @Override
    public Beatmap getSong() {
        try {
            String json = MemoryReaderConnections.fetchJsonData(MemoryReaderConnections.webHookPath);
            ObjectMapper mapper = new ObjectMapper();
            GOsuModel model = mapper.readValue(json, GOsuModel.class);
            String beatmapId = String.valueOf(model.menu.beatmap.id);
            return beatmap = UsagiBot.getClient().getBeatmap(beatmapId);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getMods() {
        try {
            String json = MemoryReaderConnections.fetchJsonData(MemoryReaderConnections.webHookPath);
            ObjectMapper mapper = new ObjectMapper();
            GOsuModel model = mapper.readValue(json, GOsuModel.class);
            String mods = String.valueOf(model.menu.mods.str);
            return mods.replaceAll("^\"|\"$", "");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getSkin() {
        try {
            String json = MemoryReaderConnections.fetchJsonData(MemoryReaderConnections.webHookPath);
            ObjectMapper mapper = new ObjectMapper();
            GOsuModel model = mapper.readValue(json, GOsuModel.class);
            return String.valueOf(model.settings.folders.skin);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getPP(int percentage) {
        try {
            String json = MemoryReaderConnections.fetchJsonData(MemoryReaderConnections.webHookPath);
            ObjectMapper mapper = new ObjectMapper();
            GOsuModel model = mapper.readValue(json, GOsuModel.class);
            float a;
            if (percentage == 100) {
                a = model.menu.pp.perfect;
            } else if (percentage == 99) {
                a = model.menu.pp.ninetyNine;
            } else if (percentage == 98) {
                a = model.menu.pp.ninetyEight;
            } else if (percentage == 97) {
                a = model.menu.pp.ninetySeven;
            } else if (percentage == 96) {
                a = model.menu.pp.ninetySix;
            } else if (percentage == 95) {
                a = model.menu.pp.ninetyFive;
            } else {
                return null;
            }
            return String.valueOf(a);
        } catch (IOException e) {
            return null;
        }
    }
}