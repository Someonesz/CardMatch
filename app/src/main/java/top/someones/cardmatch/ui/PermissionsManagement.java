package top.someones.cardmatch.ui;

import android.app.Activity;
import android.content.pm.PackageManager;

public class PermissionsManagement {

    public static boolean checkPermissions(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    public static void verifyPermissions(Activity activity, String[] permissions, int requestCode) {
        activity.requestPermissions(permissions, requestCode);
    }

}
