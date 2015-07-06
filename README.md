oc-scenario-tool
================

A playground project using Play Framework and ElasticSearch

Installation
------------

Install ElasticSearch, Typesafe Activator and Play Framework

Setup
-----

Assuming $APP_HOME is this projects root directory...

### ElasticSearch

If your ElasticSearch setup derives from standard values please edit the config for the app in `conf/application.conf`.

### OpenID connect

The app uses various OAuth providers for sign in functionality. In order to use them you need to provide clientId/clientSecret configurations in separate config files under $APP_HOME/conf/play-authenticate. Please don't upload them into version control! $APP_HOME/conf/play-authenticate/mine.conf will include these files. You can find information on how to set up this app for Google, Facebook and others to get client IDs and secrets.

Running
-------

Run one or two (or more) Elastic Search instances:

```
cd $APP_HOME; elasticsearch --config=elasticsearch/node0.yml
cd $APP_HOME; elasticsearch --config=elasticsearch/node1.yml
```

You should be able to check the ElasticSearch cluster state at http://localhost:9200/_plugin/gui/.

then fire up the Play Framework, either from your most beloved IDE or using

```
activator run
```

Then browse to http://localhost:9000

Authentication
--------------

The current implementation uses http://joscha.github.io/play-authenticate/ for user authentication with twitter, github,
Facebook, Google and LinkedIn. If a user logs in for the first time a new user is created in the database. Any "user
meta data" that the auth provider delivers (e.g., picture, gender, ...) is saved additionally as a linked account.

If the same user logs in using a different auth provider he will be identified as an existing user by matching his email
address. The "user meta data" delivered by the second (third, ...) auth provider will also be stored to another linked
account.

App Accounts ca be set up here:

Twitter  - 
Google   - https://console.developers.google.com
Facebook - https://developers.facebook.com/apps/
Github   - https://github.com/settings/developers
LinkedIn - https://www.linkedin.com/developer/apps

TODOs:
- Implement sign up and login with username / password credentials 

Authorization
-------------

Authorization is done via https://github.com/schaloner/deadbolt-2.
