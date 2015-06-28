Documentation
=============


[BusTime Developer API Guide 2.4.pdf](https://github.com/rectangle-dbmi/Realtime-Port-Authority/blob/master/Documentation/BusTime%20Developer%20API%20Guide%202.4.pdf) is the API guide to the Real-Time Tracking of Port Authority.

[Realtime Port Authority Bus Tracker Website](http://truetime.portauthority.org/)

Google's [General Transit Reference Specification](https://developers.google.com/transit/gtfs/reference) is also an important read.

The file given by Port Authority is [here](http://www.portauthority.org/GeneralTransitFeed/) but this is only for taking note of stops and whatnot. We do not use this at the moment.

[Google Maps API](https://developers.google.com/maps/)

##For Developers

Make sure you get your API key... You must first make an account in http://truetime.portauthority.com,
go to My Account, then request for an API Key.

Looks like in order for this to go commercially, we would have to request
for a commercial API key. But we will have to prove a real working version
of the app.

Ritwik figured out how the API works... It's a simple hyperlink provided in the API

For example... to get all P1 and P3 buses running... (will not work without your API copy pasted into "your_key"):
http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=your_key&rt=P1,P3

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

```
map.setMyLocationEnabled(true);
```

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
- ~~print locations of the buses that we poll.~~ done by Ritwik

#Installing Android Studio on Ubuntu (and Linux in general)

It seems hard but it really isn't... not really... But there are some quirks

######Prequisites:
- [Oracle Java 8](http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html)
- [32-Bit Libraries](http://askubuntu.com/questions/454253/how-to-run-android-sdk-in-ubuntu-64-bits)

######Instructions:
1. Install Oracle Java 7 (might as well install 8 also if you want)
	- `sudo add-apt-repository ppa:webupd8team/java`
	- `sudo apt-get update`
	- `sudo apt-get install oracle-java7-installer`
	- `sudo apt-get install oracle-java8-installer`
		- make sure java oracle 8 is the main java jdk
2. Install 32-bit libraries (assuming 12.04 or 14.04 ubuntu)
	- `sudo apt-get install libc6:i386`
	- `sudo apt-get install libncurses5:i386`
	- `sudo apt-get install libstdc++6:i386`
	- `sudo apt-get install lib32z1`
3. Install Android Studio..
	- `sudo add-apt-repository ppa:paolorotolo/android-studio`
	- `sudo apt-get update`
	- `sudo apt-get install android-studio`
4. Install the update that pops up (would be 1.0.2)
5. Install the SDK Manager and install
	- API 16 to the most recent (21 atm)
	- Google Play Services
	- Google Repositories
	- ... pretty much everything kinda a little
	
Do not open the project until you do all of these steps....
	
#Opening the Project....
- fork the rectang\[le\] repo into your account (go click on the Fork button at the top right of the github website of this project and then click on your name)
	- git clone your forked repo
	- type this after git cloning:
		- `git remote add upstream https://github.com/rectangle-dbmi/Realtime-Port-Authority.git`
- Click on Open Project (sometimes in the File dropdown menu)
- go all the way up to *../Realtime-Port-Authority/Android/Pittsburgh_Realtime_Tracker*
- click on the android there
- **You need to change the Java JVM too...**
	- right-click the project folder and click on **Open Module Settings** near the bottom of the dropdown box
	- change the JDK to Java 8 Oracle (look for yours or look below..)
		- mine was `/usr/lib/jvm/java-8-oracle` which will be the same if you install the real version.

After this... should be good... Make sure if you're editing the files in Android Studio, use the program to commit and whatnot. This is because it'll commit and .gitignore the correct files. If you don't do this, it'll mess up the whole project!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

#Contribution Notice

We ask that you read the LICENSE in the root of the repository (especially the bottom) before contributing to the code. Then, please follow the instructions for the hidden package given in the LICENSE. Thank you for contributing to the application!
