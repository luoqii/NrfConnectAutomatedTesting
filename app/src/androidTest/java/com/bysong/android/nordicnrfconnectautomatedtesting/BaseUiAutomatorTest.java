package com.bysong.android.nordicnrfconnectautomatedtesting;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BaseUiAutomatorTest {
    private static final String TAG = BaseUiAutomatorTest.class.getSimpleName();
    private UiDevice device;

    private static final String BASIC_SAMPLE_PACKAGE
            = "no.nordicsemi.android.mcp";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";

    @Before
    public void startMainActivityFromHomeScreen() throws UiObjectNotFoundException {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        device.pressHome();

        // Wait for launcher
        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                LAUNCH_TIMEOUT);

        if (false) {
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
        } else {
            UiObject nrfConnect = device.findObject(new UiSelector()
                                .text("nRF Connect"));
            nrfConnect.click();
            back2AppHome();
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)),
                LAUNCH_TIMEOUT);

        UiObject cancelButton = device.findObject(new UiSelector()
                .packageName(BASIC_SAMPLE_PACKAGE));
        assertTrue(cancelButton.exists());
    }

    @Test
    public void scanBleDevice() throws UiObjectNotFoundException {
        back2AppHome();
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

        device.wait(Until.hasObject(
                By.pkg(BASIC_SAMPLE_PACKAGE)
                        .res("no.nordicsemi.android.mcp:id/address").text(mac)),
                10 * 1000);
        UiObject scanDevice = device.findObject(new UiSelector()
                .resourceId("no.nordicsemi.android.mcp:id/address")
                .text(mac)
                .packageName(BASIC_SAMPLE_PACKAGE));
    }

    @Test
    public void connect_disconnect() throws UiObjectNotFoundException {
        int loop = 10;
        String mac = "18:E7:77:00:0C:40";

        back2AppHome();
        removeAllConnectedDevices();

        switch2ScannerTab();
        scanBleDevice(mac);
        UiObject connect = new UiObject(new UiSelector()
                .text("CONNECT"));
        connect.click();

        connect_disconnect(mac, loop, 5 * 1000, 3 * 1000);
    }

    @Test
    public void connect_disconnect_in_device_tab() throws UiObjectNotFoundException {
        int loop = 1000;
        String mac = "18:E7:77:00:0C:40";

        connect_disconnect(mac, loop, 10 * 1000, 5 * 1000);
    }

    public void connect_disconnect(String mac, int count, long connectTimeoutMs, long disconnectTimeoutMs) throws UiObjectNotFoundException {
        //XXX 无法通过 "更多选项" 获取 ”连接“ 菜单
        UiObject connectMenuButtonP = new UiObject(new UiSelector().childSelector(new UiSelector().description("更多选项")));
        boolean exist = connectMenuButtonP.exists();
        int childCount = connectMenuButtonP.getChildCount();
        Log.d(TAG, "childCount:" + childCount);
        UiObject connectMenuButton = connectMenuButtonP.getChild(new UiSelector().index(0));
        connectMenuButton = connectMenuButtonP.getChild(new UiSelector().index(0));

        connectMenuButton = new UiObject(new UiSelector().text("CONNECT"));
        UiObject disconnectMenuButton = new UiObject(new UiSelector().text("DISCONNECT"));

        UiObject connectState
                = new UiObject(new UiSelector().resourceId("no.nordicsemi.android.mcp:id/connection_state"));
        if ("CONNECTED".equalsIgnoreCase(connectState.getText())) {
            disconnectMenuButton.click();
        }
        for (int i = 0 ; i < count ; i++) {
            String message = " mac:" + mac + " " + i + "/" + count
                    + " connectTimeoutMs:" + connectTimeoutMs
                    + " disconnectTimeoutMs:" + disconnectTimeoutMs;
            Log.d(TAG, "connect_disconnect " + message);

            connectMenuButton.click();
            device.wait(Until.hasObject(
                    By.pkg(BASIC_SAMPLE_PACKAGE)
                            .text("CONNECTED")),
                    connectTimeoutMs);
            assertEquals("connect " + message, "CONNECTED", connectState.getText());

            disconnectMenuButton.click();
            device.wait(Until.hasObject(
                    By.pkg(BASIC_SAMPLE_PACKAGE)
                            .text("DISCONNECTED")),
                    disconnectTimeoutMs);
            assertEquals("disconnect " + message, "DISCONNECTED", connectState.getText());
        }

    }

    void removeAllConnectedDevices() throws UiObjectNotFoundException {
        UiScrollable settingsItem = new UiScrollable(new UiSelector()
                .className("android.widget.HorizontalScrollView"));
//        settingsItem.scrollBackward();
//        settingsItem.scrollIntoView()
    }

    @Test
    public void checkAppVersion() throws UiObjectNotFoundException {
        back2AppHome();
        String version = getAppVersion();

        String expectedVersion = "4.24.3";
        assertEquals(expectedVersion, version);
    }

    void switch2ScannerTab() throws UiObjectNotFoundException {
        UiObject filter = device.findObject(new UiSelector()
                .text("SCANNER"));
        filter.click();
    }

    void back2AppHome(){
        UiObject cancelButton = device.findObject(new UiSelector()
                .descriptionContains("Open")
                .packageName(BASIC_SAMPLE_PACKAGE));
        if (cancelButton.exists()) {
            ;
        } else {
            device.pressBack();
            back2AppHome();
        }
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

        UiScrollable settingsItem = new UiScrollable(new UiSelector()
                .className("android.widget.ScrollView"));
        settingsItem.scrollToEnd(1000);
//        UiObject about = settingsItem.getChildByText(new UiSelector()
//                .className("android.widget.TextView"), "About");
//        about.click();

//        device.pressKeyCode(KeyEvent.KEYCODE_PAGE_DOWN);
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();
//        device.pressDPadDown();

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
