package de.eidottermihi.rpicheck.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

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
