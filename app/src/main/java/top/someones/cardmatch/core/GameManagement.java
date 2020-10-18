package top.someones.cardmatch.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import top.someones.cardmatch.game.FoodGame;
import top.someones.cardmatch.game.Go;

public class GameManagement {

    private static final Map<String, Class<? extends GameObserver>> bean = new HashMap<>();
    private static final Map<String, GameObserver> mod = new HashMap<>();

    static {
        bean.put(Go.getGameName(), Go.class);
        bean.put(FoodGame.getGameName(), FoodGame.class);
    }

    private GameManagement() {
    }

    public static void addMod(Context context) {
        File modDirectory = context.getFileStreamPath("mod");
        for (File files : modDirectory.listFiles()) {
            ModAdaptor modAdaptor = ModAdaptor.loadMod(files);
            if (modAdaptor != null) {
                mod.put(modAdaptor.mGameName, modAdaptor);
            }
        }
    }

    public static GameObserver getBean(String name, Context context, Handler handler) throws InstantiationException, IllegalAccessException {
        return bean.get(name).newInstance().initGameObserver(context, handler);
    }

    public static GameObserver getModBean(String name, Context context, Handler handler) throws InstantiationException, IllegalAccessException {
        return mod.get(name).initGameObserver(context, handler);
    }


    public static Set<String> getAllGameName() {
        return bean.keySet();
    }


}
