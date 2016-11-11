package com.bptracker;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Author: Derek Benda
 */
public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mTestActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using
        // the default Intent with:
        // action = [email protected] Intent#ACTION_MAIN}
        // flags = [email protected] Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
//        mTestActivity = getActivity();
//        mTestEmptyText = (TextView) mTestActivity
//                .findViewById(R.id.empty);
//        mFab = (FloatingActionButton) mTestActivity
//                .findViewById(R.id.fab);
    }

    /**
     * Test if your test fixture has been set up correctly.
     * You should always implement a test that
     * checks the correct setup of your test fixture.
     * If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {
        // Try to add a message to add context to your assertions.
        // These messages will be shown if
        // a tests fails and make it easy to
        // understand why a test failed
//        assertNotNull("mTestActivity is null", mTestActivity);
//        assertNotNull("mTestEmptyText is null", mTestEmptyText);
//        assertNotNull("mFab is null", mFab);
    }

}