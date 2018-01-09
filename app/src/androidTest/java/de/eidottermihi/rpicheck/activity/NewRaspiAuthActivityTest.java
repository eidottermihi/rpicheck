/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.eidottermihi.rpicheck.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.eidottermihi.raspicheck.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * @author michael
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewRaspiAuthActivityTest {

    private static final String NAME = "a New Pi";
    private static final String HOST = "192.168.0.1";
    private static final String USER = "pi";

    @Rule
    public ActivityTestRule<NewRaspiAuthActivity> mActivityRule = new ActivityTestRule<>(
            NewRaspiAuthActivity.class, true, false);

    @Test
    public void check_auth_method_switching() throws InterruptedException {
        setupAndLaunchActivity();
        onView(withId(R.id.spinnerAuthMethod))
                .perform(click());
        onData(allOf(instanceOf(String.class), is("password"))).perform(click());
        // check: password textfield present, no file chooser button,
        // no private key password textfield, no checkbox to save pk password
        checkState(true, false, false, false);
        // switch to private key
        onView(withId(R.id.spinnerAuthMethod))
                .perform(click());
        onData(allOf(instanceOf(String.class), is("private key"))).perform(click());
        // check
        checkState(false, true, false, false);
        // switch to private key with password
        onView(withId(R.id.spinnerAuthMethod))
                .perform(click());
        onData(allOf(instanceOf(String.class), is("private key w. password"))).perform(click());
        // check
        checkState(false, true, false, true);
    }

    private void setupAndLaunchActivity() {
        // setup activity
        Context targetContext = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        Intent intent = new Intent(targetContext, NewRaspiAuthActivity.class);
        Bundle b = new Bundle();
        b.putString(NewRaspiActivity.PI_HOST, HOST);
        b.putString(NewRaspiActivity.PI_USER, USER);
        b.putString(NewRaspiActivity.PI_NAME, NAME);
        b.putString(NewRaspiActivity.PI_DESC, "");
        intent.putExtra(NewRaspiActivity.PI_BUNDLE, b);
        mActivityRule.launchActivity(intent);
    }

    private void checkState(boolean sshPassDisplayed, boolean keyfileButtonDisplayed, boolean keyfilePasswordDisplayed, boolean checkboxAskDisplayed) {
        final ViewInteraction sshPassword = onView(withId(R.id.ssh_password_edit_text));
        if (sshPassDisplayed) {
            sshPassword.check(matches(isDisplayed()));
        } else {
            sshPassword.check(matches(not(isDisplayed())));
        }
        final ViewInteraction keyfileButton = onView(withId(R.id.buttonKeyfile));
        if (keyfileButtonDisplayed) {
            keyfileButton.check(matches(isDisplayed()));
        } else {
            keyfileButton.check(matches(not(isDisplayed())));
        }
        final ViewInteraction keyfilePassword = onView(withId(R.id.key_password_edit_text));
        if (keyfilePasswordDisplayed) {
            keyfilePassword.check(matches(isDisplayed()));
        } else {
            keyfilePassword.check(matches(not(isDisplayed())));
        }
        final ViewInteraction checkboxAsk = onView(withId(R.id.checkboxAsk));
        if (checkboxAskDisplayed) {
            checkboxAsk.check(matches(isDisplayed()));
        } else {
            checkboxAsk.check(matches(not(isDisplayed())));
        }
    }


}
