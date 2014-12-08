Realtime-Port-Authority
=======================

The purpose of this was to create a real-time tracking of port authority
of Pittsburgh, PA, using the realtime API given by [Port Authority](http://realtime.portauthority.org/bustime/home.jsp) on top of
the Google Maps API.

### Features:
- Buses Run at Realtime
- ListView saves via sharedpreferences
- polylines added via Asynctask
- multiple polylines connected to each other
- uses xml to read and write

### Releases:
- [Google Play Store](https://play.google.com/store/apps/details?id=rectangledbmi.com.pittsburghrealtimetracker)

API limitations:
- the buses don't update until after 10+ seconds. Our app only updates 
every 10 seconds
- There are only a couple buses offered publicly. We promise to get updates
to the new buses as soon as possible!

#Android Development

This will be done using [Android Studio](https://developer.android.com/sdk/installing/studio.html) 1.0 RC4 as of 12-07-2014.


