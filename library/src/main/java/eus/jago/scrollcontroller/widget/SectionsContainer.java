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

    static final Class<?>[] CONSTRUCTOR_PARAMS = new Class<?>[] {
            Context.class
    };

    static final ThreadLocal<Map<String, Constructor<Scene>>> sConstructors = new ThreadLocal<>();

    static final String WIDGET_PACKAGE_NAME;

    static {
        final Package pkg = ScrollController.class.getPackage();
        WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
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

                Scene scene = ((LayoutParams) section.getLayoutParams()).getScene();
                if (scene != null) {
                    childFactor = getNormalizedSectionFactor(childFactor);
                    scene.onUpdate(childFactor, section);
                }
            }
        }
    }

    @Override
    public void onLoad(int verticalScroll, int height) {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof Section) {
                Section section = (Section) childAt;
                float childFactor = getSectionFactor(verticalScroll, height, section);

                Scene scene = ((LayoutParams) section.getLayoutParams()).getScene();
                if (scene != null) {
                    childFactor = getNormalizedSectionFactor(childFactor);
                    scene.load(section, childFactor);
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

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
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

    public static class LayoutParams extends LinearLayout.LayoutParams {

        private String DEFAULT_SCENE = Scene.class.getCanonicalName();

        private String sceneClassName;
        private Scene scene;
        public boolean mSceneResolved = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ScrollController_SectionsContainer_LayoutParams,
                    0, 0);

            try {
                sceneClassName = a.getString(R.styleable.ScrollController_SectionsContainer_LayoutParams_scene);
                if (sceneClassName == null) sceneClassName = DEFAULT_SCENE;
            } finally {
                a.recycle();
            }

            setScene(parseScene(c, attrs, sceneClassName));
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public Scene getScene() {
            return scene;
        }

        public void setScene(Scene scene) {
            this.scene = scene;
        }
    }

    @SuppressWarnings("unchecked")
    private static Scene parseScene(Context context, AttributeSet attrs, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        final String fullName;
        if (name.startsWith(".")) {
            // Relative to the app package. Prepend the app package name.
            fullName = context.getPackageName() + name;
        } else if (name.indexOf('.') >= 0) {
            // Fully qualified package name.
            fullName = name;
        } else {
            // Assume stock behavior in this package (if we have one)
            fullName = !TextUtils.isEmpty(WIDGET_PACKAGE_NAME)
                    ? (WIDGET_PACKAGE_NAME + '.' + name)
                    : name;
        }

        try {
            Map<String, Constructor<Scene>> constructors = sConstructors.get();
            if (constructors == null) {
                constructors = new HashMap<>();
                sConstructors.set(constructors);
            }
            Constructor<Scene> c = constructors.get(fullName);
            if (c == null) {
                final Class<Scene> clazz = (Class<Scene>) Class.forName(fullName, true, context.getClassLoader());
                c = clazz.getConstructor(CONSTRUCTOR_PARAMS);
                c.setAccessible(true);
                constructors.put(fullName, c);
            }
            return c.newInstance(context);
        } catch (Exception e) {
            throw new RuntimeException("Could not inflate Scene subclass " + fullName, e);
        }
    }

}
