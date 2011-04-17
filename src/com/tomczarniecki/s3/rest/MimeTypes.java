/*
 * Copyright (c) 2011, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tomczarniecki.s3.rest;

import com.tomczarniecki.s3.Generics;
import org.apache.commons.io.FilenameUtils;

import java.util.Map;

public class MimeTypes {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private final Map<String, String> types = Generics.newHashMap();

    public MimeTypes() {
        types.put("323", "text/h323");
        types.put("acx", "application/internet-property-stream");
        types.put("ai", "application/postscript");
        types.put("aif", "audio/x-aiff");
        types.put("aifc", "audio/x-aiff");
        types.put("aiff", "audio/x-aiff");
        types.put("asf", "video/x-ms-asf");
        types.put("asr", "video/x-ms-asf");
        types.put("asx", "video/x-ms-asf");
        types.put("au", "audio/basic");
        types.put("avi", "video/x-msvideo");
        types.put("axs", "application/olescript");
        types.put("bas", "text/plain");
        types.put("bcpio", "application/x-bcpio");
        types.put("bin", "application/octet-stream");
        types.put("bmp", "image/bmp");
        types.put("c", "text/plain");
        types.put("cat", "application/vnd.ms-pkiseccat");
        types.put("cdf", "application/x-cdf");
        types.put("cer", "application/x-x509-ca-cert");
        types.put("class", "application/octet-stream");
        types.put("clp", "application/x-msclip");
        types.put("cmx", "image/x-cmx");
        types.put("cod", "image/cis-cod");
        types.put("cpio", "application/x-cpio");
        types.put("crd", "application/x-mscardfile");
        types.put("crl", "application/pkix-crl");
        types.put("crt", "application/x-x509-ca-cert");
        types.put("csh", "application/x-csh");
        types.put("css", "text/css");
        types.put("dcr", "application/x-director");
        types.put("der", "application/x-x509-ca-cert");
        types.put("dir", "application/x-director");
        types.put("dll", "application/x-msdownload");
        types.put("dms", "application/octet-stream");
        types.put("doc", "application/msword");
        types.put("dot", "application/msword");
        types.put("dvi", "application/x-dvi");
        types.put("dxr", "application/x-director");
        types.put("eps", "application/postscript");
        types.put("etx", "text/x-setext");
        types.put("evy", "application/envoy");
        types.put("exe", "application/octet-stream");
        types.put("fif", "application/fractals");
        types.put("flr", "x-world/x-vrml");
        types.put("gif", "image/gif");
        types.put("gtar", "application/x-gtar");
        types.put("gz", "application/x-gzip");
        types.put("h", "text/plain");
        types.put("hdf", "application/x-hdf");
        types.put("hlp", "application/winhlp");
        types.put("hqx", "application/mac-binhex40");
        types.put("hta", "application/hta");
        types.put("htc", "text/x-component");
        types.put("htm", "text/html");
        types.put("html", "text/html");
        types.put("htt", "text/webviewhtml");
        types.put("ico", "image/x-icon");
        types.put("ief", "image/ief");
        types.put("iii", "application/x-iphone");
        types.put("ins", "application/x-internet-signup");
        types.put("isp", "application/x-internet-signup");
        types.put("jfif", "image/pipeg");
        types.put("jpe", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("jpg", "image/jpeg");
        types.put("js", "application/x-javascript");
        types.put("latex", "application/x-latex");
        types.put("lha", "application/octet-stream");
        types.put("lsf", "video/x-la-asf");
        types.put("lsx", "video/x-la-asf");
        types.put("lzh", "application/octet-stream");
        types.put("m13", "application/x-msmediaview");
        types.put("m14", "application/x-msmediaview");
        types.put("m3u", "audio/x-mpegurl");
        types.put("man", "application/x-troff-man");
        types.put("mdb", "application/x-msaccess");
        types.put("me", "application/x-troff-me");
        types.put("mht", "message/rfc822");
        types.put("mhtml", "message/rfc822");
        types.put("mid", "audio/mid");
        types.put("mny", "application/x-msmoney");
        types.put("mov", "video/quicktime");
        types.put("movie", "video/x-sgi-movie");
        types.put("mp2", "video/mpeg");
        types.put("mp3", "audio/mpeg");
        types.put("mpa", "video/mpeg");
        types.put("mpe", "video/mpeg");
        types.put("mpeg", "video/mpeg");
        types.put("mpg", "video/mpeg");
        types.put("mpp", "application/vnd.ms-project");
        types.put("mpv2", "video/mpeg");
        types.put("ms", "application/x-troff-ms");
        types.put("mvb", "application/x-msmediaview");
        types.put("nws", "message/rfc822");
        types.put("oda", "application/oda");
        types.put("p10", "application/pkcs10");
        types.put("p12", "application/x-pkcs12");
        types.put("p7b", "application/x-pkcs7-certificates");
        types.put("p7c", "application/x-pkcs7-mime");
        types.put("p7m", "application/x-pkcs7-mime");
        types.put("p7r", "application/x-pkcs7-certreqresp");
        types.put("p7s", "application/x-pkcs7-signature");
        types.put("pbm", "image/x-portable-bitmap");
        types.put("pdf", "application/pdf");
        types.put("pfx", "application/x-pkcs12");
        types.put("pgm", "image/x-portable-graymap");
        types.put("pko", "application/ynd.ms-pkipko");
        types.put("pma", "application/x-perfmon");
        types.put("pmc", "application/x-perfmon");
        types.put("pml", "application/x-perfmon");
        types.put("pmr", "application/x-perfmon");
        types.put("pmw", "application/x-perfmon");
        types.put("pnm", "image/x-portable-anymap");
        types.put("pot,", "application/vnd.ms-powerpoint");
        types.put("ppm", "image/x-portable-pixmap");
        types.put("pps", "application/vnd.ms-powerpoint");
        types.put("ppt", "application/vnd.ms-powerpoint");
        types.put("prf", "application/pics-rules");
        types.put("ps", "application/postscript");
        types.put("pub", "application/x-mspublisher");
        types.put("qt", "video/quicktime");
        types.put("ra", "audio/x-pn-realaudio");
        types.put("ram", "audio/x-pn-realaudio");
        types.put("ras", "image/x-cmu-raster");
        types.put("rgb", "image/x-rgb");
        types.put("rmi", "audio/mid");
        types.put("roff", "application/x-troff");
        types.put("rtf", "application/rtf");
        types.put("rtx", "text/richtext");
        types.put("scd", "application/x-msschedule");
        types.put("sct", "text/scriptlet");
        types.put("setpay", "application/set-payment-initiation");
        types.put("setreg", "application/set-registration-initiation");
        types.put("sh", "application/x-sh");
        types.put("shar", "application/x-shar");
        types.put("sit", "application/x-stuffit");
        types.put("snd", "audio/basic");
        types.put("spc", "application/x-pkcs7-certificates");
        types.put("spl", "application/futuresplash");
        types.put("src", "application/x-wais-source");
        types.put("sst", "application/vnd.ms-pkicertstore");
        types.put("stl", "application/vnd.ms-pkistl");
        types.put("stm", "text/html");
        types.put("svg", "image/svg+xml");
        types.put("sv4cpio", "application/x-sv4cpio");
        types.put("sv4crc", "application/x-sv4crc");
        types.put("swf", "application/x-shockwave-flash");
        types.put("t", "application/x-troff");
        types.put("tar", "application/x-tar");
        types.put("tcl", "application/x-tcl");
        types.put("tex", "application/x-tex");
        types.put("texi", "application/x-texinfo");
        types.put("texinfo", "application/x-texinfo");
        types.put("tgz", "application/x-compressed");
        types.put("tif", "image/tiff");
        types.put("tiff", "image/tiff");
        types.put("tr", "application/x-troff");
        types.put("trm", "application/x-msterminal");
        types.put("tsv", "text/tab-separated-values");
        types.put("txt", "text/plain");
        types.put("uls", "text/iuls");
        types.put("ustar", "application/x-ustar");
        types.put("vcf", "text/x-vcard");
        types.put("vrml", "x-world/x-vrml");
        types.put("wav", "audio/x-wav");
        types.put("wcm", "application/vnd.ms-works");
        types.put("wdb", "application/vnd.ms-works");
        types.put("wks", "application/vnd.ms-works");
        types.put("wmf", "application/x-msmetafile");
        types.put("wps", "application/vnd.ms-works");
        types.put("wri", "application/x-mswrite");
        types.put("wrl", "x-world/x-vrml");
        types.put("wrz", "x-world/x-vrml");
        types.put("xaf", "x-world/x-vrml");
        types.put("xbm", "image/x-xbitmap");
        types.put("xla", "application/vnd.ms-excel");
        types.put("xlc", "application/vnd.ms-excel");
        types.put("xlm", "application/vnd.ms-excel");
        types.put("xls", "application/vnd.ms-excel");
        types.put("xlt", "application/vnd.ms-excel");
        types.put("xlw", "application/vnd.ms-excel");
        types.put("xof", "x-world/x-vrml");
        types.put("xpm", "image/x-xpixmap");
        types.put("xwd", "image/x-xwindowdump");
        types.put("z", "application/x-compress");
        types.put("zip", "application/zip");
    }

    public String get(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        String mimeType = types.get(extension);
        return (mimeType != null) ? mimeType : DEFAULT_MIME_TYPE;
    }
}
