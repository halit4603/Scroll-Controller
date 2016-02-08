package eus.jago.scrollcontroller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import eus.jago.scrollcontroller.R;
import eus.jago.scrollcontroller.scenes.Scene;

/**
 * This class is an always vertical LinearLayout that loads the scenes and notifies onScrollChanged
 * updates to its Sections.
 */
@SuppressWarnings("unused")
public class SectionsContainer extends LinearLayout implements ScrollController.SceneDirectorListener {

    private static final String TAG = "SectionsContainer";

    public interface SceneManagerListener {

        /**
         * @param factor - a number between [0..1]
         */
        void onUpdate(float factor);
    }

    public SectionsContainer(Context context) {
        super(context);
        init();
    }

    public SectionsContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public SectionsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
    }


    @Override
    public void onScroll(int verticalScroll, int height) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof Section) {
                Section section = (Section) childAt;
                float childFactor = getSectionFactor(verticalScroll, height, section);

                Scene scene = section.getScene();
                if (scene != null) {
                    childFactor = getNormalizedSectionFactor(childFactor);
                    scene.onUpdate(childFactor);
                }
            }
        }
    }

    @Override
    public void onLoad(int scroll, int height) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof Section) {
                Section section = (Section) childAt;
                float childFactor = getSectionFactor(scroll, height, section);

                Scene scene = section.parseScene();
                if (scene != null) {
                    childFactor = getNormalizedSectionFactor(childFactor);
                    scene.load(childFactor);
                }
            }
        }
    }

    private float getNormalizedSectionFactor(float childFactor) {
        return Math.max(0, Math.min(1, childFactor));
    }

    private float getSectionFactor(int verticalScroll, int height, Section section) {
        int hookedScroll = (int) (verticalScroll + height * section.getHook());
        return (hookedScroll - section.getTop()) / (float) section.getHeight();
    }

}
