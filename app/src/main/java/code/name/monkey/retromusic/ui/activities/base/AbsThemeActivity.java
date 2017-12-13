package code.name.monkey.retromusic.ui.activities.base;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;

import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;

import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.util.Util;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsThemeActivity extends ATHToolbarActivity {

    private static final String TAG = "AbsThemeActivity";
    private View mDecorView;

    public void hideStatusBar() {
        setFullscreen(PreferenceUtil.getInstance(this).getFullScreenMode());
    }

    private void setFullscreen(boolean fullscreen) {
        final View statusBar = mDecorView.findViewById(R.id.status_bar);
        if (statusBar != null) {
            statusBar.setVisibility(fullscreen ? View.GONE : View.VISIBLE);
        }

        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);

       /* int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }
        if (fullscreen) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            newUiOptions &= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            newUiOptions &= View.SYSTEM_UI_FLAG_FULLSCREEN;
            newUiOptions &= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDecorView = getWindow().getDecorView();
        hideStatusBar();
        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.Theme_RetroMusic_Light)
                    .accentColorRes(R.color.md_green_A200)
                    .commit();
        }
        getSharedPreferences("[[kabouzeid_app-theme-helper]]", 0)
                .edit()
                .putInt("activity_theme", PreferenceUtil.getInstance(this).getGeneralTheme())
                .apply(); // TEMPORARY FIX

        super.onCreate(savedInstanceState);

        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);

        changeBackgroundShape();
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (ThemeStore.coloredNavigationBar(this) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !ATHUtil.isWindowBackgroundDark(this)) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

    }

    private void changeBackgroundShape() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("corner_window", false)) {
            getWindow().setBackgroundDrawableResource(R.drawable.round_window);
        } else {
            getWindow().setBackgroundDrawableResource(R.drawable.square_window);
        }
        View decor = getWindow().getDecorView();
        GradientDrawable gradientDrawable = (GradientDrawable) decor.getBackground();
        gradientDrawable.setColor(ATHUtil.resolveColor(this, android.R.attr.windowBackground));
    }

    protected void setDrawUnderStatusbar(boolean drawUnderStatusbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Util.setAllowDrawUnderStatusBar(getWindow());
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            Util.setStatusBarTranslucent(getWindow());
    }

    /**
     * This will set the color of the view with the id "status_bar" on KitKat and Lollipop.
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    public void setStatusbarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
                    setLightStatusbarAuto(color);
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
                setLightStatusbarAuto(color);
            }
        }
    }

    public void setStatusbarColorAuto() {
        // we don't want to use statusbar color because we are doing the color darkening on our own to support KitKat
        setStatusbarColor(ThemeStore.primaryColor(this));
    }

    public void setTaskDescriptionColor(@ColorInt int color) {
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this));
    }

    public void setNavigationbarColor(int color) {
        if (ThemeStore.coloredNavigationBar(this)) {
            ATH.setNavigationbarColor(this, color);
        } else {
            ATH.setNavigationbarColor(this, Color.BLACK);
        }
    }

    public void setNavigationbarColorAuto() {
        setNavigationbarColor(ThemeStore.navigationBarColor(this));
    }

    public void setLightStatusbar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightStatusbarAuto(int bgColor) {
        setLightStatusbar(ColorUtil.isColorLight(bgColor));
    }
}