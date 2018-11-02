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

package com.xenonota.configs;

import java.util.Properties;

public class MagiskConfig extends Properties {

    private String url;
    private String filename;
    private String version;

    public MagiskConfig(String url, String filename, String version) {
        this.url = url;
        this.filename = filename;
        this.version = version;
    }

    public String getUrl() {return url;}

    public String getVersion() {return version;}

    public String getFilename() {return filename;}
}
