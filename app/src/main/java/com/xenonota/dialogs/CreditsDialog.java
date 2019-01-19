/*
 * Copyright (C) 2018 XenonHD
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
 *
 * Created by Devil7DK for XenonHD
 *
 */

package com.xenonota.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xenonota.R;
import com.xenonota.adapters.CreditsAdapter;
import com.xenonota.xml.CreditsMember;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CreditsDialog extends Dialog {

    private Activity activity;

    public CreditsDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public static final String FILENAME = "credits.xml";

    private static final String KEY_MEMBER = "member";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_LINK = "link";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_about);

        ArrayList<CreditsMember> membersList = new ArrayList<>();

        String xml = "";
        try {
            StringBuilder buf=new StringBuilder();
            InputStream is=activity.getAssets().open(FILENAME);
            BufferedReader in=
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;

            while ((str=in.readLine()) != null) {
                buf.append(str);
            }

            xml = new String(buf);
            in.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!xml.isEmpty()) {
            Document doc = getDomElement(xml); // getting DOM element

            if(doc != null) {
                NodeList nl = doc.getElementsByTagName(KEY_MEMBER);

                for (int i = 0; i < nl.getLength(); i++) {
                    Element e = (Element) nl.item(i);

                    CreditsMember member = new CreditsMember();
                    member.setName(getValue(e, KEY_NAME));
                    member.setDescription(getValue(e, KEY_DESCRIPTION));
                    member.setAvatar(getValue(e, KEY_AVATAR));
                    member.setLink(getValue(e, KEY_LINK));

                    membersList.add(member);
                }
            }
        }

        ListView list = findViewById(R.id.list);

        CreditsAdapter adapter = new CreditsAdapter(activity, membersList);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getWindow() != null) getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private Document getDomElement(String xml){
        Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

        return doc;
    }

    private String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    private String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }
}