package top.someones.cardmatch.core;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import top.someones.cardmatch.R;



public class GameManagement {

    private static final Map<String, Class<? extends GameObserver>> bean = new HashMap<>();
    private static final Map<String, GameObserver> mod = new HashMap<>();
    private static final Map<String, GameResources<?>> gameRes = new HashMap<>();

    static {
        gameRes.put("扑克",new DefaultResource());
    }

    private GameManagement() {
    }

    public static void addMod(Context context) {
//        File modDirectory = context.getFileStreamPath("mod");
//        for (File files : modDirectory.listFiles()) {
//            ModAdaptor modAdaptor = ModAdaptor.loadMod(files);
//            if (modAdaptor != null) {
//                mod.put(modAdaptor.mGameName, modAdaptor);
//            }
//        }
    }

    public static GameObserver getBean(String name, Context context, Handler handler) throws InstantiationException, IllegalAccessException {
        return GameObserverAdapter.makeGameObserver(context,handler,gameRes.get(name));
    }

//    public static GameObserver getModBean(String name, Context context, Handler handler) throws InstantiationException, IllegalAccessException {
//        return mod.get(name).initGameObserver(context, handler);
//    }


    public static Set<String> getAllGameName() {
        return bean.keySet();
    }

     static class DefaultResource implements GameResources<Integer>{

        public static final int FrontImage = R.drawable.b;
        public static final int[] BackImage = {R.drawable.poker1, R.drawable.poker2, R.drawable.poker3, R.drawable.poker4, R.drawable.poker5, R.drawable.poker6, R.drawable.poker7, R.drawable.poker8, R.drawable.poker9, R.drawable.poker10, R.drawable.poker11, R.drawable.poker12};

        @Override
        public Integer getFrontResource() {
            return FrontImage;
        }

        @Override
        public Integer getBackResources(int index) {
            return BackImage[index];
        }

        @Override
        public String getGameName() {
            return "扑克";
        }

        @Override
        public int size() {
            return BackImage.length;
        }
    }


}
