package eus.jago.scrollcontroller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.security.InvalidParameterException;

import eus.jago.scrollcontroller.R;

/**
 * A Section is a simple RelativeLayout that supports Scene animations
 */
public class Section extends RelativeLayout {

    public static float TOP = 0f;
    public static float MIDDLE = .5f;
    public static float BOTTOM = 1f;

    /**
     * The hook is the position in the height of the scroll container where the Scene
     * will be triggered. The trigger is a value that goes from 0 to 1. It works as a
     * timeline value and is sent to the {@link eus.jago.scrollcontroller.scenes.Scene Scene}
     * to update it.
     *
     * When Section's  top   touches the hook the value of the trigger is 0.
     * When Section's bottom touches the hook the value of the trigger is 1.
     */
    private float hook = MIDDLE;


    public Section(Context context) {
        super(context);
        init(context, null);
    }

    public Section(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Section(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollController_Section,
                0, 0);

        try {
            hook = a.getFloat(R.styleable.ScrollController_Section_hook, hook);
        } finally {
            a.recycle();
        }
    }

    public float getHook() {
        return hook;
    }

    public void setHook(float hook) {
        if (hook < 0 || hook > 1) throw new InvalidParameterException("Hook must be a value between 0 and 1");
        this.hook = hook;
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public RelativeLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams {

        private boolean pin = false;
        public boolean mSceneResolved = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ScrollController_Section_LayoutParams,
                    0, 0);

            try {
                pin = a.getBoolean(R.styleable.ScrollController_Section_LayoutParams_pin, pin);
            } finally {
                a.recycle();
            }
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public boolean isPinned() {
            return pin;
        }

        public void setPinned(boolean pinned) {
            pin = pinned;
        }
    }

}
