package code.name.monkey.retromusic.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.ResultReceiver;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.kabouzeid.appthemehelper.util.TintHelper;

import java.lang.reflect.Method;

public class Util {
    private static final int[] TEMP_ARRAY = new int[1];

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }

    public static String EncodeString(String string) {
        return string.replace("%", "%25")
                .replace(".", "%2E")
                .replace("#", "%23")
                .replace("$", "%24")
                .replace("/", "%2F")
                .replace("[", "%5B")
                .replace("]", "%5D");
    }

    public static String DecodeString(String string) {
        return string.replace("%25", "%")
                .replace("%2E", ".")
                .replace("%23", "#")
                .replace("%24", "$")
                .replace("%2F", "/")
                .replace("%5B", "[")
                .replace("%5D", "]");
    }

    public static int getStatusBarHeight(final Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void openUrl(AppCompatActivity context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(str));
        intent.setFlags(268435456);
        context.startActivity(intent);
    }

    public static Point getScreenSize(@NonNull Context c) {
        Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    @TargetApi(19)
    public static void setStatusBarTranslucent(@NonNull Window window) {
        window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public static void setAllowDrawUnderStatusBar(@NonNull Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static void showIme(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService
                (Context.INPUT_METHOD_SERVICE);
        // the public methods don't seem to work for me, so… reflection.
        try {
            Method showSoftInputUnchecked = InputMethodManager.class.getMethod(
                    "showSoftInputUnchecked", int.class, ResultReceiver.class);
            showSoftInputUnchecked.setAccessible(true);
            showSoftInputUnchecked.invoke(imm, 0, null);
        } catch (Exception e) {
            // ho hum
        }
    }

    public static boolean isTablet(@NonNull final Resources resources) {
        return resources.getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isLandscape(@NonNull final Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public static Drawable getVectorDrawable(@NonNull Resources res, @DrawableRes int resId, @Nullable Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawable(resId, theme);
        }
        return VectorDrawableCompat.create(res, resId, theme);
    }

    public static Drawable getTintedVectorDrawable(@NonNull Context context, @DrawableRes int id, @ColorInt int color) {
        return TintHelper.createTintedDrawable(getVectorDrawable(context.getResources(), id, context.getTheme()), color);
    }

    public static Drawable getTintedVectorDrawable(@NonNull Resources res, @DrawableRes int resId, @Nullable Resources.Theme theme, @ColorInt int color) {
        return TintHelper.createTintedDrawable(getVectorDrawable(res, resId, theme), color);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRTL(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else return false;
    }

    public static boolean isAllowedToDownloadMetadata(final Context context) {
        switch (PreferenceUtil.getInstance(context).autoDownloadImagesPolicy()) {
            case "always":
                return true;
            case "only_wifi":
                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnectedOrConnecting();
            case "never":
            default:
                return false;
        }
    }


}
