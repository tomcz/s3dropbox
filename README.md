A Java-based GUI front-end to Amazon S3
=======================================

This is a Java Swing application into which you can drag and drop files, which are then uploaded to your
Amazon S3 account. You can create public links to these files, and paste these links into emails, so that
the files can be downloaded later (using any web browser) by clicking on the link, or you can download the
files yourself at a later stage using the application.

Usage
-----

Run using: `java -jar S3DropBox.jar [credentials file]`

The credentials file is optional. If not present, the application will look for a file called `.s3dropbox`
in your home directory. If that is not present you will be asked for some AWS credentials when the
application starts and then a `.s3dropbox` file will be created for you in your home directory.

Here are the values that the `.s3dropbox` file will contain:

AMAZON\_ACCESS\_KEY\_ID

* Required entry.
* This is the public key provided to you by Amazon when you sign up for an S3 account.

AMAZON\_SECRET\_ACCESS\_KEY

* Required entry.
* This is the secret key provided to you by Amazon when you sign up for an S3 account.

AWS\_REGION

* Required entry.
* Set to "us-east-1" by default.
* You'll need to set this to an appropriate value for non-AWS endpoints.

AWS\_ENDPOINT

* Optional entry.
* Set this to use non-AWS endpoints, like [DigitalOcean's Spaces](https://www.digitalocean.com/products/spaces/).

USE\_SSL

* Optional entry.
* Versions 1.6+ of the S3DropBox use HTTPS by default. Older versions used HTTP.
* If you want to use HTTPS then simply set this option to either 'true' or 'yes'.
* If you want to use HTTP then simply set this option to either 'false' or 'no'.

PROXY\_HOST and PROXY\_PORT

* Optional entries.
* If you need to connect to the internet via a HTTP proxy then you should provide
  the hostname or IP address of the proxy and its port using these options.

PROXY\_USERNAME and PROXY\_PASSWORD

* Optional entries.
* If your HTTP proxy requires username and password authentication then you should
  provide them via these options. The S3DropBox is capable of using BASIC, DIGEST
  and NTLM authentication mechanisms.
