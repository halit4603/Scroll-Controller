package eus.jago.scrollcontroller.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import eus.jago.scrollcontroller.R;
import eus.jago.scrollcontroller.scenes.Scene;

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

    /**
     * The scene of this section
     */
    private Scene scene;
    private boolean mSceneLoaded = false;

    private String DEFAULT_SCENE = Scene.class.getCanonicalName();
    private String sceneClassName;

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

    private void init(final Context context, final AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollController_Section,
                0, 0);

        try {
            hook = a.getFloat(R.styleable.ScrollController_Section_hook, hook);
            sceneClassName = a.getString(R.styleable.ScrollController_Section_scene);
            if (sceneClassName == null) sceneClassName = DEFAULT_SCENE;
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

    @Nullable
    public Scene getScene() {
        return scene;
    }

    public void setScene(@Nullable Scene scene) {
        this.scene = scene;
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams {

        private boolean pin = false;

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


    static final Class<?>[] CONSTRUCTOR_PARAMS = new Class<?>[] {
            Context.class,
            Section.class
    };

    static final ThreadLocal<Map<String, Constructor<Scene>>> sConstructors = new ThreadLocal<>();

    private static final String WIDGET_PACKAGE_NAME;

    static {
        final Package pkg = ScrollController.class.getPackage();
        WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
    }


    /**
     * Instantiates the Scene
     * @return the Scene of this section
     */
    public Scene parseScene() {
        if (!mSceneLoaded) {
            mSceneLoaded = true;
            setScene(parseScene(getContext(), sceneClassName, Section.this));
        }
        return getScene();
    }


    @SuppressWarnings("unchecked")
    private static Scene parseScene(Context context, String name, Section section) {
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
            return c.newInstance(context, section);
        } catch (Exception e) {
            throw new RuntimeException("Could not inflate Scene subclass " + fullName, e);
        }
    }


}
