package eus.jago.scrollcontroller.scenes;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;

import eus.jago.scrollcontroller.widget.Section;


/**
 * This class represents a Scene.
 *
 * <p>It contains the code that animates and handles actions that happen during a Scene.</p>
 */
public class Scene {

    private final Context mContext;
    private final Section mSection;

    public Scene(Context context, Section section) {
        mContext = context;
        mSection = section;
    }


    /**
     * Initialize scene and its child's initial positions.
     * Scene might already be started.
     *
     * If you are going to change view size, padding or margin be sure to call super AFTER
     * doing your own changes.
     * @param childFactor the current scroll factor
     */
    @CallSuper
    public void load(float childFactor) {
        updateViews(childFactor);
    }

    /**
     * Handles basic view animations, always call super before doing your stuff
     * @param childFactor The factor represents the current time of the scene (a float [0..1])
     */
    @CallSuper
    public void onUpdate(float childFactor) {
        updateViews(childFactor);
    }


    /**
     * Updates pinned views
     * @param childFactor the scroll factor
     */
    private void updateViews(float childFactor) {
        for (int i = 0; i < getSection().getChildCount(); i++) {
            View childAt = getSection().getChildAt(i);
            Section.LayoutParams params = (Section.LayoutParams) childAt.getLayoutParams();
            if (params.isPinned()) {
                handlePin(childAt, childFactor);
            }
        }
    }

    private void handlePin(View child, float childFactor) {
        int from = getSection().getTop();
        int to = getSection().getBottom();

        int translationY = (int) ((to - from) * childFactor);
        int maxTranslationY = (int) (getSection().getHeight()
                - getSection().getPaddingBottom()
                - getSection().getPaddingTop()
                - child.getY()
                - child.getHeight()
                + child.getTranslationY());

        maxTranslationY = Math.max(maxTranslationY, 0);
        child.setTranslationY(Math.min(translationY, maxTranslationY));
    }


    public Context getContext() {
        return mContext;
    }

    public Section getSection() {
        return mSection;
    }
}
