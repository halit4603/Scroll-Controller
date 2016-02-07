package eus.jago.scrollcontroller.scenes;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.View;

import eus.jago.scrollcontroller.widget.Section;
import eus.jago.scrollcontroller.widget.SectionsContainer;


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
     * Initialize scene and its child's initial positions
     * @param section
     */
    public void load(Section section) {}

    /**
     * Handles basic view animations, always call super before doing your stuff
     * @param childFactor The factor represents the current time of the scene (a float [0..1])
     * @param parent The SectionContainer that holds this Scene
     * @param section The section that is using this Scene
     */
    @CallSuper
    public void onUpdate(float childFactor, SectionsContainer parent, Section section) {
        for (int i = 0; i < section.getChildCount(); i++) {
            View childAt = section.getChildAt(i);
            Section.LayoutParams params = (Section.LayoutParams) childAt.getLayoutParams();
            if (params.isPinned()) {
                handlePin(section, childAt, childFactor);
            }
        }
    }

    private void handlePin(Section section, View child, float childFactor) {
        int from = section.getTop();
        int to = section.getBottom();

        int translationY = (int) ((to - from) * childFactor);
        int maxTranslationY = (int) (section.getHeight() - section.getPaddingBottom() - section.getPaddingTop() - child.getY() - child.getHeight() + child.getTranslationY());

        maxTranslationY = Math.max(maxTranslationY, 0);
        child.setTranslationY(Math.min(translationY, maxTranslationY));
    }


    public Context getContext() {
        return mContext;
    }

}
