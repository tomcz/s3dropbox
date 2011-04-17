package com.tomczarniecki.s3.rest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class WebRequestBuilderTests {

    @Test
    public void shouldCreateCorrectInsecureUrlToObject() throws Exception {
        Configuration credentials = new Configuration("0PN5J17HBGZHT7JJ3X82", "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o",
                "", "", "", "", "", "", "false");

        WebRequestBuilder builder = new WebRequestBuilder(credentials);
        String url = builder.createURL(Parameters.forObject(Method.GET, "johnsmith", "/cute/puppy.jpg"));

        assertThat(url, is("http://s3.amazonaws.com/johnsmith/cute/puppy.jpg"));
    }

    @Test
    public void shouldCreateCorrectSecureUrlToObject() throws Exception {
        Configuration credentials = new Configuration("0PN5J17HBGZHT7JJ3X82", "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o",
                "", "", "", "", "", "", "true");

        WebRequestBuilder builder = new WebRequestBuilder(credentials);
        String url = builder.createURL(Parameters.forObject(Method.GET, "johnsmith", "/cute/puppy.jpg"));

        assertThat(url, is("https://s3.amazonaws.com/johnsmith/cute/puppy.jpg"));
    }

    @Test
    public void shouldCreateExpectedPublicUrlToObject() throws Exception {
        Configuration credentials = new Configuration("0PN5J17HBGZHT7JJ3X82", "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o",
                "", "", "", "", "", "", "false");

        Parameters parameters = Parameters.forObject(Method.GET, "johnsmith", "/cute/puppy.jpg", 1234L);

        QueryString query = new QueryString();
        query.add("AWSAccessKeyId", credentials.getAccessKeyId());
        query.add("Expires", "1234");
        query.add("Signature", credentials.sign(parameters));

        WebRequestBuilder builder = new WebRequestBuilder(credentials);
        String url = builder.createURL(parameters, query);

        String expected = "http://s3.amazonaws.com/johnsmith/cute/puppy.jpg" +
                "?AWSAccessKeyId=0PN5J17HBGZHT7JJ3X82" +
                "&Expires=1234" +
                "&Signature=HWHkXHVSQazVDcxkZaCkVlGz7vg%3D";

        assertThat(url, is(expected));
    }

    @Test
    public void shouldSetMethodHeadersCorrectly() throws Exception {
        Configuration credentials = new Configuration("0PN5J17HBGZHT7JJ3X82", "uV3F3YluFJax1cknvbcGwgjvx4QpvB+leU8dUj2o");

        Headers headers = new Headers();
        headers.add("Content-Length", "1234");
        headers.add("Content-Type", "text/plain");
        headers.add("Content-MD5", "zasqweqwssa");
        headers.add("Date", "Tue, 27 Mar 2007 19:36:42 +0000");

        WebRequestBuilder builder = new WebRequestBuilder(credentials);
        WebRequest request = builder.build(Parameters.forObject(Method.PUT, "johnsmith", "puppy.jpg", headers));

        Headers req = request.getHeaders();

        assertThat(req.values("Content-Length"), hasItem("1234"));
        assertThat(req.values("Content-Type"), hasItem("text/plain"));
        assertThat(req.values("Content-MD5"), hasItem("zasqweqwssa"));
        assertThat(req.values("Date"), hasItem("Tue, 27 Mar 2007 19:36:42 +0000"));
        assertThat(req.values("Authorization"), hasItem("AWS 0PN5J17HBGZHT7JJ3X82:U1mGO/4WVFbZgp2Q+3x1BkNKDPs="));
    }
}
