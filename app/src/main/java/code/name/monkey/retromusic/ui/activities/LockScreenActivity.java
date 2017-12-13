package code.name.monkey.retromusic.ui.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.retro.musicplayer.backend.swipebtn.SwipeButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerPlaybackControlsFragment;
import code.name.monkey.retromusic.util.PreferenceUtil;

/**
 * Created by hemanths on 20/08/17.
 */

public class LockScreenActivity extends AbsMusicServiceActivity {
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.swipe_btn)
    SwipeButton mSwipeButton;

    private PlayerPlaybackControlsFragment mPlayerPlaybackControlsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setDrawUnderStatusbar(true);
        setContentView(R.layout.activity_lock_screen);

        hideStatusBar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        ButterKnife.bind(this);

        mPlayerPlaybackControlsFragment = (PlayerPlaybackControlsFragment) getSupportFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        mPlayerPlaybackControlsFragment.hideVolumeIfAvailable();

        mSwipeButton = findViewById(R.id.swipe_btn);
        mSwipeButton.setDisabledDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp));
        mSwipeButton.setEnabledDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_white_24dp));
        mSwipeButton.setOnActiveListener(this::finish);

    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadSong();
    }

    private void loadSong() {
        SongGlideRequest.Builder.from(Glide.with(this), MusicPlayerRemote.getCurrentSong())
                .checkIgnoreMediaStore(this)
                .generatePalette(this).build()
                .into(new RetroMusicColoredTarget(image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
//                        mPlayerPlaybackControlsFragment.setDark(getDefaultFooterColor());
                    }

                    @Override
                    public void onColorReady(int color) {
                        mPlayerPlaybackControlsFragment.setDark((color));
                        if (PreferenceUtil.getInstance(LockScreenActivity.this).getAdaptiveColor())
                            changeColor(color);
                        else {
                            changeColor(ThemeStore.accentColor(LockScreenActivity.this));
                        }
                    }
                });
    }

    private void changeColor(int color) {
        Drawable drawable = ContextCompat.getDrawable(LockScreenActivity.this, R.drawable.shape_rounded_edit);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            mSwipeButton.setBackground(drawable);
        }

        int colorPrimary = MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color));
        mSwipeButton.setCenterTextColor(colorPrimary);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadSong();
    }
}
