![PAT track Logotype](https://github.com/Tobaloidee/Realtime-Port-Authority/blob/default/images/logotype-a-v1.png)

Realtime-Port-Authority
=======================

The purpose of this was to create a real-time tracking of port authority
of Pittsburgh, PA, using the realtime API given by [Port Authority](http://truetime.portauthority.org/bustime/home.jsp) on top of
the Google Maps API.

### Features:
- Buses Run at Realtime
- shows ETAs of buses to stops and stops to buses
- shows stop markers above a certain zoom
- RecyclerView saves via sharedpreferences
- Uses RxJava + Retrofit to obtain data from Port Authority's API
- Our (lame) attempt at using MVVM for UI-model interaction


### Releases:
- [Google Play Store](https://play.google.com/store/apps/details?id=rectangledbmi.com.pittsburghrealtimetracker)

API limitations:
- the buses don't update until after 10+ seconds. Our app only updates 
every 10 seconds
- There are only a couple buses offered publicly. We promise to get updates
to the new buses as soon as possible!
- Buses disappear when off-route away from their route lines

#Android Development

Please refer to [this wiki page](https://github.com/rectangle-dbmi/Realtime-Port-Authority/wiki/Contributing-to-to-PAT-Track) for instructions to install and use the project.

This will be done using [Android Studio](https://developer.android.com/sdk/installing/studio.html) 1.0 as of 12-09-2014.


