Documentation
=============


BusTime Developer API Guide 2.4.pdf is the API guide to the Real-Time Tracking of Port Authority.

Google's [General Transit Reference Specification](https://developers.google.com/transit/gtfs/reference) is also an important read.

The file given by Port Authority is [here](http://www.portauthority.org/GeneralTransitFeed/) but this is only for taking note of stops and whatnot.

[Google Maps API](https://developers.google.com/maps/)

##For Developers

Make sure you get your API key... You must first make an account in http://realtime.portauthority.com,
go to My Account, then request for an API Key.

Looks like in order for this to go commercially, we would have to request
for a commercial API key. But we will have to prove a real working version
of the app.

Ritwik figured out how the API works... It's a simple hyperlink provided in the API

For example... to get all P3 buses running... (will not work without your API copy pasted into "your_key"):
http://realtime.portauthority.org/bustime/api/v1/getvehicles?key=your_key&rt=P3

This will return an XML of the real-time data.... with some data changed

```
<bustime-response>
	<vehicle>
		<vid>3247</vid>
		<tmstmp>20140729 16:06</tmstmp>
		<lat>0</lat>
		<lon>9001</lon>
		<hdg>159</hdg>
		<pid>801</pid>
		<rt>P3</rt>
		<des>blablah</des>
		<pdist>35442</pdist>
		<spd>27</spd>
		<tablockid>P3 -335</tablockid>
		<tatripid>53191</tatripid>
		<zone/>
	</vehicle>
	<vehicle>
	.
	.
	.
	etc...
```
This data changes every 10 seconds!

Apparently printing your current location in Google maps is done through
```map.setMyLocationEnabled(true);```

Apparently a hunky dunky way of [displaying points](http://stackoverflow.com/questions/14822567/display-many-points-with-google-maps-android-api-v2) on the screen is through
...Through android
```
mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
   @Override
   public void onCameraChange(CameraPosition position) {
      final LatLngBounds screenBounds = mMapView.getProjection().getVisibleRegion().latLngBounds;
      for (YourPoint point : mPoints) {
         if (screenBounds.contains(point.getLatLng()) {
            mMapView.addMarker(point.getLatLng()); //over here....
         }
      } 
   }
}
```
This means atm, we can just get a map and input coordinates to print the location of each bus that we can pull up.

So then... TODOS as of 07.29.2014:
- Know Android programming (java, xml, manifest files...)
- Figure out Google Maps API
- print locations of the buses that we poll.
