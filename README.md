RasPi Check ![Get it on Google Play.](https://developer.android.com/images/brand/en_generic_rgb_wo_45.png)
========

Android app for checking your Raspberry Pi status.
[Raspi Check is available on Google Play](https://play.google.com/store/apps/details?id=de.eidottermihi.rpicheck)

The goal of this Android app is to show the user the current system status of a running Raspberry Pi.

To gather the information needed, RasPi Check uses a SSH connection (using a fork of [shikhar/sshj](https://github.com/shikhar/sshj)).
RasPi Check does not need additional software installed on your Raspberry Pi!


Issue Tracking
------------
Please report bugs here: https://github.com/eidottermihi/rpicheck/issues


Contributing
------------
Feel free to contribute!

Eclipse Setup
------------

RasPi Check uses the following Android libraries: [ActionBarSherlock](http://actionbarsherlock.com/), [Android-PullToRefresh](https://github.com/chrisbanes/Android-PullToRefresh) and [Android File Dialog](https://code.google.com/p/android-file-dialog/).
They are included under the libs/ directory, so import them as Android Projects into your workspace.

