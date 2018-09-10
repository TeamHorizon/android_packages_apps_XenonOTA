/*
 * Copyright (C) 2018 Chandra Poerwanto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xenonota.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

public class OTAParser {

    private static final String ns = null;
    private static final String FILENAME_TAG = "Filename";
    private static final String ROMURL_TAG = "RomUrl";
    private static final String MD5URL_TAG = "MD5Url";
    private static final String CHANGELOGURL_TAG = "ChangelogUrl";
    private static OTAParser mInstance;
    private String mDeviceName = null;
    private String mReleaseType = null;
    private OTADevice mDevice = null;

    private OTAParser() {
    }

    public static OTAParser getInstance() {
        if (mInstance == null) {
            mInstance = new OTAParser();
        }
        return mInstance;
    }

    public OTADevice parse(InputStream in, String deviceName, String releaseType) throws XmlPullParserException, IOException {
        this.mDeviceName = deviceName;
        this.mReleaseType = releaseType;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readBuildType(parser);
            return mDevice;
        } finally {
            in.close();
        }
    }

    private void readBuildType(XmlPullParser parser) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase(mReleaseType)) {
                readOreo(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void readOreo(XmlPullParser parser) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equalsIgnoreCase(mDeviceName)) {
                readDevice(parser);
            } else {
                skip(parser);
            }
        }
    }

    private void readDevice(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, mDeviceName);
        mDevice = new OTADevice();
        String ROMURL = "";
        String CHECKSUMURL = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            if (tagName.equalsIgnoreCase(FILENAME_TAG)) {
                String tagValue = readTag(parser, tagName);
                mDevice.setLatestVersion(tagValue);
            } else if (tagName.equalsIgnoreCase(ROMURL_TAG)) {
                String tagValue = readTag(parser, tagName);
                ROMURL = tagValue;
            } else if (tagName.equalsIgnoreCase(MD5URL_TAG)) {
                String tagValue = readTag(parser, tagName);
                CHECKSUMURL = tagValue;
            } else if (tagName.equalsIgnoreCase(CHANGELOGURL_TAG)) {
                String tagValue = readTag(parser, tagName);
                mDevice.setChangelogURL(tagValue);
            } else {
                skip(parser);
            }
        }
        mDevice.setROMURL(ROMURL);
        mDevice.setChecksumURL(CHECKSUMURL);
    }

    private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return text;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
