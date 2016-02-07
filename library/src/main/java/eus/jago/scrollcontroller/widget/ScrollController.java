package eus.jago.scrollcontroller.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * It's a simple ScrollView that actively sends onScroll updates to the children that implement
 * ScrollController.OnScrollListener
 */
public class ScrollController extends ScrollView {

    /**
     * Indicates whether the scenes have been initialized
     */
    boolean initialized = false;


    /**
     * Listener used by child views to be notified of scroll changes
     */
    public interface SceneDirectorListener {
        /**
         * @param scroll - total amount of pixels that have been scrolled up
         * @param height - the height of the view
         */
        void onScroll(int scroll, int height);

        void onLoad(int verticalScroll, int height);
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

        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof SceneDirectorListener) {
                ((SceneDirectorListener) childAt).onScroll(getScrollY(), getHeight());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //First initialization:
        //TODO: should this be here?
        if (!initialized) {
            callLoadInSubviews();
            initialized = true;
        }
    }

    private void callLoadInSubviews() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof SceneDirectorListener) {
                ((SceneDirectorListener) childAt).onLoad(getScrollY(), getHeight());
            }
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        //allow parent classes to save state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.scrollY = getScrollY();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState)state;
        //allow parent classes restore state
        super.onRestoreInstanceState(ss.getSuperState());

        setScrollY(ss.scrollY);
    }

    static class SavedState extends BaseSavedState {
        int scrollY;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.scrollY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.scrollY);
        }

        //required field that makes Parcelables from a Parcel
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
