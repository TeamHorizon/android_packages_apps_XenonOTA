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
 * Taken from: http://www.technotalkative.com/android-load-images-from-web-and-caching
 *
 */

package com.xenonota.utils;

import java.io.File;
import android.content.Context;
import android.os.Environment;

class FileCache {

    private File cacheDir;

    FileCache(Context context){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"Avatars");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            if (!cacheDir.mkdirs()) OTAUtils.logError("Unable to create cache dir for avatars");
    }

    File getFile(String url){
        return new File(cacheDir, String.valueOf(url.hashCode()));
    }

    void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            if (!f.delete()) OTAUtils.logError("Unable to clear cache file " + f.getName());
    }

}
