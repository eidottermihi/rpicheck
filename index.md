# RasPi Check [![Build Status](https://travis-ci.org/eidottermihi/rpicheck.svg?branch=master)](https://travis-ci.org/eidottermihi/rpicheck)

Android app for checking your Raspberry Pi ® status.

The goal of this Android app is to show the user the current system status of a running Raspberry Pi ®.

To gather the information needed, RasPi Check uses a SSH connection (using a android-specific fork of [SSHJ](https://github.com/hierynomus/sshj)). This app also works on other SBCs via [fake_vcgencmd](https://github.com/clach04/fake_vcgencmd), e.g. when running [Armbian](https://www.armbian.com).

<img src="raspicheck.png" width="128px">

## Download

RasPi Check is available for free at [F-Droid](https://f-droid.org/repository/browse/?fdid=de.eidottermihi.raspicheck) and at [Google Play](https://play.google.com/store/apps/details?id=de.eidottermihi.raspicheck).

## Issue Tracker

If you find any bugs and glitches please open an [issue here](https://github.com/eidottermihi/rpicheck/issues).


|   |   |   |
|---|---|---|
![01-rpicheck.png](01-rpicheck.png) | ![02-rpicheck.png](02-rpicheck.png) | ![03-rpicheck.png](03-rpicheck.png)

## Contributing

Feel free to fork and open up pull requests!

Just fire up Android Studio and import this project via File -> Import Project.
All dependencies will be integrated automatically by Gradle.

![featured graphic](web_1024_500.jpg)

## Copyright Information

The app logo is a derivative of "Raspberry.ico" by [Martina Šmejkalová](http://www.sireasgallery.com/), used under [CC BY](http://creativecommons.org/licenses/by/2.0/). The app logo is licensed under [CC BY](http://creativecommons.org/licenses/by/2.0/) by [Michael Prankl](https://github.com/eidottermihi).

'RasPi' is one of the Rasberry Pi ® abriviations. For more information visit [http://www.raspberrypi.org](http://www.raspberrypi.org). Raspberry Pi is a trademark of the Raspberry Pi Foundation.
