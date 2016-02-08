package eus.jago.scrollcontroller.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

/**
 * It's a simple ScrollView that actively sends onScroll updates to the children that implement
 * ScrollController.OnScrollListener
 */
public class ScrollController extends ScrollView {

    private static final String TAG = "ScrollController";
    private boolean initialized = false;

    /**
     * Listener used by child views to be notified of scroll changes
     */
    public interface SceneDirectorListener {
        /**
         * @param scroll - total amount of pixels that have been scrolled up
         * @param height - the height of the view
         */
        void onScroll(int scroll, int height);

        /**
         * Will be called every time onLayout gets called
         * @param scroll - total amount of pixels that have been scrolled up
         * @param height - the height of the view
         */
        void onLoad(int scroll, int height);
    }

    public ScrollController(Context context) {
        super(context);
        init();
    }

    public ScrollController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public ScrollController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScrollbarFadingEnabled(false);
        setSaveEnabled(true);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (int i = 0; i < getChildCount() && initialized; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof SceneDirectorListener) {
                ((SceneDirectorListener) childAt).onScroll(getScrollY(), getHeight());
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        callLoadInSubviews();
        initialized = true;
    }

    private void callLoadInSubviews() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof SceneDirectorListener) {
                ((SceneDirectorListener) childAt).onLoad(getScrollY(), getHeight());
            }
        }
    }
}
