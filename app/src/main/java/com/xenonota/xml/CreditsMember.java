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

package com.xenonota.xml;

public class CreditsMember {
    private String mName;
    private String mDescription;
    private String mAvatar;
    private String mLink;

    public String getName() {return mName;}
    public void setName(String name) {mName = name;}

    public String getDescription() {return mDescription;}
    public void setDescription(String description) {mDescription = description;}

    public String getAvatar() {return mAvatar;}
    public void setAvatar(String avatar) {mAvatar = avatar;}

    public String getLink() {return mLink;}
    public void setLink(String link) {mLink = link;}
}
