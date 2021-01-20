package com.bysong.android.nordicnrfconnectautomatedtesting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiCollection;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.prefs.BackingStoreException;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BaseUiAutomatorTest {
    private UiDevice device;

    private static final String BASIC_SAMPLE_PACKAGE
            = "no.nordicsemi.android.mcp";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        device.pressHome();

        // Wait for launcher
        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                LAUNCH_TIMEOUT);

        // Launch the app
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        // Clear out any previous instances

        ComponentName cName = new ComponentName(BASIC_SAMPLE_PACKAGE, "no.nordicsemi.android.mcp.DeviceListActivity");
        intent = Intent.makeMainActivity(cName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);

        UiObject cancelButton = device.findObject(new UiSelector()
                .packageName(BASIC_SAMPLE_PACKAGE));
        assertTrue(cancelButton.exists());
    }

    @Test
    public void scanBleDevice() throws UiObjectNotFoundException {
        scanBleDevice("18:E7:77:00:0C:40");
    }

    void scanBleDevice(String mac) throws UiObjectNotFoundException {
        UiObject filter = device.findObject(new UiSelector()
                .resourceId("no.nordicsemi.android.mcp:id/filter_header")
                .packageName(BASIC_SAMPLE_PACKAGE));
        filter.click();

        UiObject filterByName = device.findObject(new UiSelector()
                .resourceId("no.nordicsemi.android.mcp:id/filter")
                .packageName(BASIC_SAMPLE_PACKAGE));
        filterByName.setText(mac);
        filter.click();
        UiObject scanButton = device.findObject(new UiSelector()
                .resourceId("no.nordicsemi.android.mcp:id/action_scan_start")
                .packageName(BASIC_SAMPLE_PACKAGE));
        scanButton.click();
    }

    @Test
    public void checkAppVersion() throws UiObjectNotFoundException {
        String version = getAppVersion();

        String expectedVersion = "4.24.3";
        assertEquals(expectedVersion, version);
    }

    /**
     *
     * @return a.b.c
     * @throws UiObjectNotFoundException
     */
    String getAppVersion() throws UiObjectNotFoundException {
        String version = "";
        UiObject cancelButton = device.findObject(new UiSelector()
                .descriptionContains("Open")
                .packageName(BASIC_SAMPLE_PACKAGE));
        cancelButton.click();
        UiObject settingButton = device.findObject(new UiSelector()
                .text("Settings")
                .packageName(BASIC_SAMPLE_PACKAGE));
        settingButton.click();

//        UiScrollable settingsItem = new UiScrollable(new UiSelector()
//                .className("android.widget.ScrollView"));
//        UiObject about = settingsItem.getChildByText(new UiSelector()
//                .className("android.widget.TextView"), "About");
//        about.click();

//        device.pressKeyCode(KeyEvent.KEYCODE_PAGE_DOWN);
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();
        device.pressDPadDown();

        UiObject about = device.findObject(new UiSelector()
                .className("android.widget.TextView")
//                .text("About")
                .resourceId("no.nordicsemi.android.mcp:id/category_about")
        );
        about.click();


        UiObject aboutAppButton = device.findObject(new UiSelector()
                .text("About application")
                .packageName(BASIC_SAMPLE_PACKAGE));
        aboutAppButton.click();

        UiObject versionTextView = device.findObject(new UiSelector().textMatches("Version.*"));
        String actualVersion = versionTextView.getText();
        String[] vs = actualVersion.split(" ");
        version = vs[1];
        return version;
    }
}
