//package top.someones.cardmatch.core;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Handler;
//import android.view.View;
//import android.widget.ImageView;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashSet;
//import java.util.Set;
//
//public class ModAdaptor extends BaseGameObserver {
//
//    public String mGameName;
//    private Bitmap mFrontImage;
//    private Bitmap[] mBackImage;
//
//    public ModAdaptor(Context context, Handler handler) {
//        super(context, handler);
//    }
//
//
//    public static ModAdaptor loadMod(File modDirectory) {
//        try {
//            for (File gameConfig : modDirectory.listFiles()) {
//                if (gameConfig.isFile()) {
//                    if (gameConfig.getName().equals("GameConfig.json")) {
//                        ModAdaptor that = new ModAdaptor();
//                        JSONObject config = new JSONObject(readTextFile(new FileInputStream(gameConfig)));
//                        that.mGameName = config.getString("Mod_Name");
//                        JSONArray jarr = config.getJSONArray("BackImageName");
//                        Set<String> backImageName = new HashSet<>();
//                        for (int i = 0; i < jarr.length(); i++) {
//                            backImageName.add(jarr.getString(i));
//                        }
//                        that.addImageBitmap(modDirectory, config.getString("FrontImageName"), backImageName);
//                        return that;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }
//
//    private void addImageBitmap(File modDirectory, String frontImageName, Set<String> backImageName) throws FileNotFoundException {
//        mBackImage = new Bitmap[backImageName.size()];
//        int index = 0;
//        for (File imageFile : modDirectory.listFiles()) {
//            if (imageFile.isFile()) {
//                if (backImageName.contains(imageFile.getName())) {
//                    mBackImage[index] = BitmapFactory.decodeStream(new FileInputStream(imageFile));
//                    index++;
//                } else if (imageFile.getName().equals(frontImageName)) {
//                    mFrontImage = BitmapFactory.decodeStream(new FileInputStream(imageFile));
//                }
//
//            }
//        }
//    }
//
//
//    private static String readTextFile(InputStream input) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
//            String str = br.readLine();
//            while (str != null) {
//                sb.append(str);
//                str = br.readLine();
//            }
//            input.close();
//        }
//        return sb.toString();
//    }
//
//    @Override
//    protected int[] setData() {
//        return super.getRandomResourcesIndex(mBackImage.length);
//    }
//
//    @Override
//    protected View[][] makeGameView(int[] gameData) {
//        View[][] views = new View[16][2];
//        for (int i = 0; i < 16; i++) {
//            views[i][0] = getImageView(mFrontImage);
//            views[i][1] = getImageView(mBackImage[gameData[i]]);
//        }
//        return views;
//    }
//
//    private View getImageView(Bitmap res) {
//        ImageView imageView = new ImageView(super.getContext());
//        imageView.setImageBitmap(res);
//        return imageView;
//    }
//
//}
