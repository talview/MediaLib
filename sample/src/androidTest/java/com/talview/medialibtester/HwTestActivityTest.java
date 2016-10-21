package com.talview.medialibtester;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@android.support.test.filters.LargeTest
@RunWith(AndroidJUnit4.class)
public class HwTestActivityTest {

    @Rule
    public ActivityTestRule<HwTestActivity> mActivityTestRule = new ActivityTestRule<>(HwTestActivity.class);

    private Executor mainThreadExecutor;

    @Before
    public void setUp() {
        mainThreadExecutor = provideMainThreadExecutor(mActivityTestRule
                .getActivity().getMainLooper());
    }

    @Test
    public void hwTestActivityTest() throws UiObjectNotFoundException, ExecutionException, InterruptedException {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_test_camera), withText("Test Camera"), isDisplayed()));
        appCompatButton.perform(click());
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            allowPermissionsIfNeeded(device);
            allowPermissionsIfNeeded(device);
            allowPermissionsIfNeeded(device);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runSynchronouslyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivityTestRule.getActivity().faceDetectErrorDialog
                        .getButton(DialogInterface.BUTTON_POSITIVE).performClick();
            }
        });

        View v = mActivityTestRule.getActivity().findViewById(R.id.btn_test_playback);
        while (v.getVisibility() != View.VISIBLE) {
            Thread.sleep(1000);
        }
        ViewInteraction playbackButtonInteraction = onView(
                allOf(withId(R.id.btn_test_playback), withText("Playback"), isDisplayed()));
        playbackButtonInteraction.perform(click());
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void allowPermissionsIfNeeded(UiDevice device)
            throws UiObjectNotFoundException {
        UiObject allowPermissions = device.findObject(new UiSelector().text("Allow"));
        if (allowPermissions.exists()) {
            allowPermissions.click();
        } else {
            allowPermissions = device.findObject(new UiSelector().text("ALLOW"));
            if (allowPermissions.exists())
                allowPermissions.click();
        }
    }

    private void runSynchronouslyOnUiThread(Runnable action)
            throws ExecutionException, InterruptedException {
        FutureTask<Void> uiTask = new FutureTask<>(action, null);
        mainThreadExecutor.execute(uiTask);
        uiTask.get();
    }

    private static Executor provideMainThreadExecutor(Looper mainLooper) {
        final Handler handler = new Handler(mainLooper);
        return new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                handler.post(runnable);
            }
        };
    }

}
