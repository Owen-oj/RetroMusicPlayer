package code.name.monkey.retromusic.views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;

import code.name.monkey.retromusic.R;

/**
 * @author Hemanth S (h4h13).
 */

public class SansFontCollapsingToolbarLayout extends CollapsingToolbarLayout {
    public SansFontCollapsingToolbarLayout(Context context) {
        super(context);
        init(context);
    }

    public SansFontCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SansFontCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Typeface typefaceBold = Typeface.createFromAsset(context.getAssets(), getResources().getString(R.string.sans_bold));
        setExpandedTitleTypeface(typefaceBold);

        Typeface typefaceNormal = Typeface.createFromAsset(context.getAssets(), getResources().getString(R.string.sans_bold));
        setCollapsedTitleTypeface(typefaceBold);

    }
}
