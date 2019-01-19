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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;

class MemoryCache {
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

    Bitmap get(String id){
        if(!cache.containsKey(id))
            return null;
        SoftReference<Bitmap> ref=cache.get(id);
        if (ref != null)
            return ref.get();
        else
            return null;
    }

    void put(String id, Bitmap bitmap){
        cache.put(id, new SoftReference<>(bitmap));
    }
}
