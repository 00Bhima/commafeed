CommaFeed [![Build Status](https://buildhive.cloudbees.com/job/Athou/job/commafeed/badge/icon)](https://buildhive.cloudbees.com/job/Athou/job/commafeed/)
=========
Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on Dropwizard and AngularJS.

Related open-source projects
----------------------------

Android apps: [News+ extension](https://github.com/Athou/commafeed-newsplus) - [Android app](https://github.com/doomrobo/CommaFeed-Android-Reader)

Browser extensions: [Chrome](https://github.com/Athou/commafeed-chrome) - [Firefox](https://github.com/Athou/commafeed-firefox) - [Opera](https://github.com/Athou/commafeed-opera) - [Safari](https://github.com/Athou/commafeed-safari)

Deployment on your own server
-----------------------------

For storage, you can either use an embedded H2 database or an external MySQL, PostgreSQL or SQLServer database.
You also need Maven 3.x (and a Java 1.7+ JDK) installed in order to build the application.

To install maven and openjdk on Ubuntu, issue the following commands

    sudo add-apt-repository ppa:natecarlson/maven3
    sudo apt-get update
    sudo apt-get install openjdk-7-jdk maven3
    
    # Not required but if you don't, use 'mvn3' instead of 'mvn' for the rest of the instructions.
    sudo ln -s /usr/bin/mvn3 /usr/bin/mvn
    
On Windows and other operating systems, just download maven 3.x from the [official site](http://maven.apache.org/), extract it somewhere and add the `bin` directory to your `PATH` environment variable.
    
Clone this repository. If you don't have git you can download the sources as a zip file from [here](https://github.com/Athou/commafeed/archive/master.zip)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    
Now build the application

    mvn clean package
    
Copy `config.yml.example` to `config.yml` then edit the file to your liking.
Issue the following command to run the app, the server will listen by default on ``http://localhost:8082`. The default user is `admin` and the default password is `admin`.

	java -jar target/commafeed-2.0.0.jar server config.yml

You can use nginx or apache as a proxy http server. Note that when using apache, the `ProxyPreserveHost on` option should be set in your config file.

Local development
-----------------

To start the dropwizard backend, use your IDE to run CommaFeedApplication as your main class, and pass `server config.dev.yml` as arguments to the program.
To start the client-side webserver with watches on assets, run ``gulp dev`. The server is now running on port 8082 and is proxying REST requests to dropwizard on port 8083.


Translate CommaFeed into your language
--------------------------------------

Files for internationalization are located [here](https://github.com/Athou/commafeed/tree/master/src/main/resources/i18n).

To add a new language, create a new file in that directory.
The name of the file should be the two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
The language has to be referenced in the `languages.properties` file to be picked up.

When adding new translations, add them in en.properties then run `mvn -e groovy:execute -Pi18n`. It will parse the english file and add placeholders in the other translation files. 

Themes
---------------------

To create a theme, create a new file  `src/main/webapp/sass/themes/_<theme>.scss`. Your styles should be wrapped in a `#theme-<theme>` element and use the [SCSS format](http://sass-lang.com/) which is a superset of CSS.

Don't forget to reference your theme in `src/main/webapp/sass/app.scss` and in `src/main/webapp/js/controllers.js` (look for `$scope.themes`).

See [_test.scss](https://github.com/Athou/commafeed/blob/master/src/main/webapp/sass/themes/_test.scss) for an example.


Copyright and license
---------------------

Copyright 2013-2014 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.