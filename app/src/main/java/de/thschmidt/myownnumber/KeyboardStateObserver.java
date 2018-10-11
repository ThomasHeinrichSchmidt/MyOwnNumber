/*
 * Created by Thomas H. Schmidt, Linden on 11.10.18 19:11
 * Copyright (c) 2018 . No rights reserved - have fun!
 * Last change: 11.10.18 19:11
 *
 */

package de.thschmidt.myownnumber;

/**
 * Created by --thomas. on 11.10.2018.
 */
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Detecting keyboard visibility.
 * https://gist.github.com/vitkidd/b4a8c465f1071964bacbe49d9555f67b
 */

public class KeyboardStateObserver implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int MIN_KEYBOARD_HEIGHT_PX = 150;

    @Nullable private KeyboardStateListener keyboardStateListener;
    @Nullable private View decorView;
    private final Rect windowVisibleDisplayFrame = new Rect();
    private int lastVisibleDecorViewHeight;
    private int lastKeyboardHeight = 0;

    public KeyboardStateObserver(@NonNull Activity activity) {
        this.decorView = activity.getWindow().getDecorView();
        this.decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (decorView == null) return;

        // Retrieve visible rectangle inside window.
        decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
        final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

        // Decide whether keyboard is visible from changing decor view height.
        if (lastVisibleDecorViewHeight != 0) {
            if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                // Calculate current keyboard height (this includes also navigation bar height when in fullscreen mode).
                int currentKeyboardHeight = decorView.getHeight() - windowVisibleDisplayFrame.bottom;
                // Notify listener about keyboard being shown.
                if (keyboardStateListener != null) {
                    keyboardStateListener.onKeyboardShown(currentKeyboardHeight);
                }
                lastKeyboardHeight = currentKeyboardHeight;
            } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                // Notify listener about keyboard being hidden.
                if (keyboardStateListener != null) {
                    keyboardStateListener.onKeyboardHidden(lastKeyboardHeight);
                }
            }
        }

        // Save current decor view height for the next call.
        lastVisibleDecorViewHeight = visibleDecorViewHeight;
    }

    public void onDestroy() {
        if (decorView == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        decorView = null;
        keyboardStateListener = null;
    }

    public void addKeyboardStateListener(@Nullable KeyboardStateListener listener) {
        this.keyboardStateListener = listener;
    }

    public void removeKeyboardStateListener() {
        this.keyboardStateListener = null;
    }

    public interface KeyboardStateListener {
        void onKeyboardShown(int keyboardHeight);
        void onKeyboardHidden(int keyboardHeight);
    }

}