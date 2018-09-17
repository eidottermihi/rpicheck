RasPi Check [![Build Status](https://travis-ci.org/eidottermihi/rpicheck.svg?branch=master)](https://travis-ci.org/eidottermihi/rpicheck)
========
![RasPi Check Store Graphic](graphics/web_1024_500.jpg)

Android app for checking your Raspberry Pi ® status.

The goal of this Android app is to show the user the current system status of a running Raspberry Pi ®.

RasPi Check uses a SSH connection (using [SSHJ](https://github.com/hierynomus/sshj)) to connect to your Raspberry Pi ® and queries the information using Linux utilities like `ps`, `df` or the [`/proc` virtual filesystem](https://www.tldp.org/LDP/Linux-Filesystem-Hierarchy/html/proc.html). 

Download
------------

<a href="https://f-droid.org/repository/browse/?fdid=de.eidottermihi.raspicheck" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="90"/></a>
<a href="https://play.google.com/store/apps/details?id=de.eidottermihi.raspicheck" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="90"/></a>

Issue Tracker
------------
If you find any bugs and glitches please open an [issue here](https://github.com/eidottermihi/rpicheck/issues).


Contributing
------------
Feel free to fork and open up pull requests!

Just fire up Android Studio and import this project via File -> Import Project.
All dependencies will be integrated automatically by Gradle.

Copyright Information
------------
The app logo is a derivative of "Raspberry.ico" by [Martina Šmejkalová](http://www.sireasgallery.com/), used under [CC BY](http://creativecommons.org/licenses/by/2.0/). The app logo is licensed under [CC BY](http://creativecommons.org/licenses/by/2.0/) by [Michael Prankl](https://github.com/eidottermihi).

'RasPi' is one of the Rasberry Pi ® abriviations. For more information visit [http://www.raspberrypi.org](http://www.raspberrypi.org). Raspberry Pi is a trademark of the Raspberry Pi Foundation.
