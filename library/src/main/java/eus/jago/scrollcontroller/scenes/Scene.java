package eus.jago.scrollcontroller.scenes;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;
import android.view.ViewGroup;

import eus.jago.scrollcontroller.widget.Section;


/**
 * This class represents a Scene.
 *
 * <p>It contains the code that animates and handles actions that happen during a Scene.</p>
 */
public class Scene {

    private final Context mContext;

    public Scene (Context context) {
        mContext = context;
    }


    /**
     * Initialize scene and its child's initial positions.
     * Scene might already be started.
     *
     * If you are going to change view size, padding or margin be sure to call super AFTER
     * doing your own changes.
     * @param section The section of the scene
     * @param childFactor the current scroll factor
     */
    @CallSuper
    public void load(Section section, float childFactor) {
        updateViews(section, childFactor, true);
    }

    /**
     * Handles basic view animations, always call super before doing your stuff
     * @param childFactor The factor represents the current time of the scene (a float [0..1])
     * @param section The section that is using this Scene
     */
    @CallSuper
    public void onUpdate(float childFactor, Section section) {
        updateViews(section, childFactor, false);
    }


    /**
     * Updates pinned views
     * @param section the section of the scene
     * @param childFactor the scroll factor
     * @param load whether is the first time views are updated (load) or not (update)
     */
    private void updateViews(Section section, float childFactor, boolean load) {
        for (int i = 0; i < section.getChildCount(); i++) {
            View childAt = section.getChildAt(i);
            Section.LayoutParams params = (Section.LayoutParams) childAt.getLayoutParams();
            if (params.isPinned()) {
                handlePin(section, childAt, childFactor, load);
            }
        }
    }

    private void handlePin(Section section, View child, float childFactor, boolean load) {
        int from = section.getTop();
        int to = section.getBottom();

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

        int translationY = (int) ((to - from) * childFactor);
        int maxTranslationY = (int) (section.getHeight()
                - section.getPaddingBottom()
                - section.getPaddingTop()
                - child.getY()
                - child.getHeight()
                + child.getTranslationY());

        //I do not understand why do I have to take these into account only on load
        //for the pinning to work on device rotation: might be because of initialization
        //callback call order?? (load is called in onLayout)
        if (load) maxTranslationY -= layoutParams.topMargin + layoutParams.bottomMargin;

        maxTranslationY = Math.max(maxTranslationY, 0);
        child.setTranslationY(Math.min(translationY, maxTranslationY));
    }


    public Context getContext() {
        return mContext;
    }

}
