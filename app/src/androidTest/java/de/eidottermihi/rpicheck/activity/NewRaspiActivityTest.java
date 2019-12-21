/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
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

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.eidottermihi.raspicheck.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * @author michael
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewRaspiActivityTest {

    private static final String NAME = "a New Pi";
    private static final String HOST = "192.168.0.1";
    private static final String USER = "pi";

    @Rule
    public ActivityTestRule<NewRaspiActivity> mActivityRule = new ActivityTestRule<>(
            NewRaspiActivity.class);

    @Test
    public void fill_all_data() {
        onView(withId(R.id.edit_raspi_name_editText))
                .perform(typeText(NAME), closeSoftKeyboard())
                .check(matches(withText(NAME)));
        onView(withId(R.id.edit_raspi_host_editText))
                .perform(typeText(HOST), closeSoftKeyboard())
                .check(matches(withText(HOST)));
        onView(withId(R.id.edit_raspi_user_editText))
                .perform(typeText(USER), closeSoftKeyboard())
                .check(matches(withText(USER)));
        // continue to next page
        onView(withId(R.id.menu_continue))
                .perform(click());
        onView(withId(R.id.ssh_password_edit_text)).check(matches(isDisplayed()));
        // return to first page
        pressBack();
        pressBack();
        // validate user input is still there
        onView(withId(R.id.edit_raspi_name_editText))
                .check(matches(withText(NAME)));
        onView(withId(R.id.edit_raspi_host_editText))
                .check(matches(withText(HOST)));
        onView(withId(R.id.edit_raspi_user_editText))
                .check(matches(withText(USER)));
    }

    @Test
    public void missing_data() {
        onView(withId(R.id.edit_raspi_name_editText))
                .perform(typeText(NAME), closeSoftKeyboard())
                .check(matches(withText(NAME)));
        onView(withId(R.id.edit_raspi_host_editText))
                .perform(typeText(HOST), closeSoftKeyboard())
                .check(matches(withText(HOST)));
        // try to continue to next page
        onView(withId(R.id.menu_continue))
                .perform(click());
        // assert user field has error text
        onView(withId(R.id.edit_raspi_user_editText))
                .check(matches(hasErrorText("Username cannot be blank.")));
    }

}
