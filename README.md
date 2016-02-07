# teamcity-oauth
teamcity oauth2 authentication plugin

![Login Screen](./docs/login-screen.png)

# Installation
 
Grab latest version of plugin from [ ![Download](https://api.bintray.com/packages/pwielgolaski/generic/teamcity-oauth/images/download.svg) ](https://bintray.com/pwielgolaski/generic/teamcity-oauth/_latestVersion)
and install it to Teamcity following https://confluence.jetbrains.com/display/TCD9/Installing+Additional+Plugins

# Test drive

You can test this plugin using GitHub oAuth & docker.

Just create github application https://github.com/settings/applications/new

Use file like this 
```
teamcity:
  image: pwielgolaski/teamcity-oauth
  environment:
    CLIENT_ID: YOUR_CLIENT
    CLIENT_SECRET: YOUR_SECRET
  ports:
   - "8111:8111"
   
```

Login at your docker host on port 8111