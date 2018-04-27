## Compiling
Install dependencies:
```
npm install
```
See available npm options:
```
npm run
```

| npm run...| description												|
|-----------|-----------------------------------------------------------|
| build		| run production build and start http-server for testing	|
| dist 		| run production build										|
| dev  		| run development server with hot module replacement		|
| watch		| run production build and watch files for changes			|
| analyse	| run development build and display package stats diagram	|
| start		| use [nw.js](https://nwjs.io/) to display the build		|
| test		| run tests													|
| cypress	| open cypress IDE for running tests						|


In order to build binsa to a different prefix (e.g. "/binsa"), set environment variable PREFIX:
```
set PREFIX=/binsa/
npm run dist
```

## Compiling for specific browser
Current compilation targets include:
* Chrome for Android 61+
* Chrome 60+
* UC Browser for Android 11.4+
* iOS Safari 10.3+
* Firefox 56+
* Samsung Internet 4+
* Opera Mini
* IE 11
* Edge 15+
* Safari 11+

The targeted browser can be adjusted .babelrc (presets/targets/browsers) with [browserlist](https://github.com/ai/browserslist) queries.

## Set serverside CORS headers

Browser usually do not allow sending data to other domains from which this app is served. In order to use this app, there are several deployment scenarios possible:

1. allow cors, either by your backend or by configuring a reverse-proxy (nginx, apache mod_proxy)
2. deploy it on the server that should receive the measurements (same-origin)
3. deploy it as a google chrome app (as long as chrome supports apps)
4. deploy it with [nw.js](https://nwjs.io/) to avoid cors checks

For 1.:
add to your nginx location for the /rest endpoint:
```
if ($request_method = 'OPTIONS') {
	add_header 'Access-Control-Allow-Origin' '*';
	add_header 'Access-Control-Allow-Credentials' 'true';
	add_header 'Access-Control-Allow-Methods' 'POST, GET, OPTIONS';
	add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';
	add_header 'Access-Control-Max-Age' 604800;
	add_header 'Content-Type' 'text/plain charset=UTF-8';
	add_header 'Content-Length' 0;
	return 204;
}
if ($request_method = 'POST') {
	add_header 'Access-Control-Allow-Origin' '*';
	add_header 'Access-Control-Allow-Credentials' 'true';
	add_header 'Access-Control-Allow-Methods' 'POST, GET, OPTIONS';
	add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';
	add_header 'Access-Control-Max-Age' 604800;
}
```

add to your apache mod_proxy LocationMatch for the /rest endpoint after adding rewrite and headers module:
```
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Credentials "true"
Header always set Access-Control-Allow-Methods "POST, GET, OPTIONS"
Header always set Access-Control-Max-Age "604800"
Header always set Access-Control-Allow-Headers "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type"

 
RewriteEngine On
RewriteCond %{REQUEST_METHOD} OPTIONS
RewriteRule ^(.*)$ $1 [R=200,L]
```

## Other platforms
### chrome app
* build with PREFIX=/
* open google chrome
* navigate to chrome://extensions
* load 'dist' folder via Load unpacked extension...
* Pack extension...
* Rename dist.crx to binsa.crx

### native Windows / Linux / MacOS executable
```
npm install nw-builder
node conf/build-nw
```
The package with executable will be in dist/binsa/ folder.
For other platforms than win64, adjust the build-nw script and run again

### native (android, iOS) apps
install an android development environment ([android-sdk](https://developer.android.com/studio/index.html#command-tools), [gradle](https://gradle.org/releases/), build-tools, java sdk etc.), set the correct PATHS and
```
npm install -g cordova
cordova create binsa org.eclipse.unide.binsa binsa
cd binsa
cordova platform add android --save
cordova requirements android
```
change the router mode from 'history' to 'hash' in src/router.js:
```javascript
export default new Router({
  mode: 'hash',
  // mode: 'history',
  base: process.env.BASEPATH,
  routes
});
```
replace cordovas default config.xml with the following:
```xml
<?xml version='1.0' encoding='utf-8'?>
<widget id="org.eclipse.unide.binsa" version="2.0.0" xmlns="http://www.w3.org/ns/widgets" xmlns:cdv="http://cordova.apache.org/ns/1.0" xmlns:cdv="http://cordova.apache.org/ns/1.0">
    <name>binsa</name>
    <description>
		Eclipse Unide Sensor Simulator Application
    </description>
    <author email="axel.meinhardt@bosch-si.com" href="http://unide.eclipse.org">
        Axel Meinhardt
    </author>
    <content src="index.html" />
    <access origin="*" />
    <allow-intent href="http://*/*" />
    <allow-intent href="https://*/*" />
    <allow-intent href="tel:*" />
    <allow-intent href="sms:*" />
    <allow-intent href="mailto:*" />
    <allow-intent href="geo:*" />
    <platform name="android">
        <config-file mode="add" parent="/manifest" platform="android" target="AndroidManifest.xml">
            <uses-permission android:name="android.permission.CAMERA" />
        </config-file>
        <icon src="www/icons/unide.png" />
        <icon src="www/icons/favicon-16x16.png" width="16" />
        <icon src="www/icons/firefox_app_128x128.png" width="128" />
    </platform>
    <engine name="android" spec="6.3.0" />
    <plugin name="cordova-plugin-whitelist" spec="^1.3.3" />
</widget>
```
compile and get the apk file to your phone:
```
cordova build android
```
more information at [cordova documentation](https://cordova.apache.org/docs/en/latest/reference/cordova-cli/)
