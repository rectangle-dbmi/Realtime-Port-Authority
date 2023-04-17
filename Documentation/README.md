Documentation
=============


[BusTime Developer API Guide 3.X](https://truetime.portauthority.org/bustime/apidoc/docs/DeveloperAPIGuide3_0.pdf) is the API guide to the Real-Time Tracking of Port Authority.

[Realtime Port Authority Bus Tracker Website](http://truetime.portauthority.org/)

Google's [General Transit Reference Specification](https://developers.google.com/transit/gtfs/reference) is also an important read.

The file given by Port Authority is [here](http://www.portauthority.org/GeneralTransitFeed/) but this is only for taking note of stops and whatnot. We do not use this at the moment.

[Google Maps API](https://developers.google.com/maps/)

## For Developers

You need two API keys in order to develop the app.
1. Google Maps API key
    * Follow the steps [here](https://developers.google.com/maps/documentation/android-sdk/get-api-key) to get one.
2. PAT TrueTime API key
    * You must first make an account in http://truetime.portauthority.com, go to My Account, then request for an API Key.

Open `secrets.example.properties`, put in your keys, and move it to a file named `secrets.properties`.

For example... to get all P1 and P3 buses running... (will not work without your API copy pasted into "your_key"):
http://realtime.portauthority.org/bustime/api/v3/getvehicles?key=your_key&rt=P1,P3

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

### Prequisites:
- [Java JDK 17](http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html)

# Opening the Project....
- fork the rectang\[le\] repo into your account (go click on the Fork button at the top right of the github website of this project and then click on your name)
	- git clone your forked repo
	- type this after git cloning:
		- `git remote add upstream https://github.com/rectangle-dbmi/Realtime-Port-Authority.git`
- Click on Open Project (sometimes in the File dropdown menu)
- go all the way up to *../Realtime-Port-Authority/Android/Pittsburgh_Realtime_Tracker*
- click on the android there

After this... should be good... Make sure if you're editing the files in Android Studio, use the program to commit and whatnot. This is because it'll commit and .gitignore the correct files. If you don't do this, it'll mess up the whole project!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

#Contribution Notice

We ask that you read the LICENSE in the root of the repository (especially the bottom) before contributing to the code. Then, please follow the instructions for the hidden package given in the LICENSE. Thank you for contributing to the application!
