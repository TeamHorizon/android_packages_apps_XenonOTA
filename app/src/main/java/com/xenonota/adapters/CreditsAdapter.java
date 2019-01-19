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

package com.xenonota.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xenonota.R;
import com.xenonota.utils.ImageLoader;
import com.xenonota.utils.OTAUtils;
import com.xenonota.utils.RoundedImageView;
import com.xenonota.xml.CreditsMember;

public class CreditsAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<CreditsMember> data;
    private ImageLoader imageLoader;

    public CreditsAdapter(Activity a, ArrayList<CreditsMember> d) {
        activity = a;
        data=d;
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = View.inflate(activity, R.layout.about_row, null);

        TextView title = vi.findViewById(R.id.title);
        TextView description = vi.findViewById(R.id.description);
        RoundedImageView avatar_image=vi.findViewById(R.id.avatar);
        ImageView link_image=vi.findViewById(R.id.link);

        final CreditsMember member = data.get(position);

        title.setText(member.getName());
        description.setText(member.getDescription());
        if (!member.getAvatar().isEmpty()){
            try {
                imageLoader.DisplayImage(member.getAvatar(), avatar_image);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        link_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTAUtils.launchURL(v.getContext(), member.getLink());
            }
        });
        return vi;
    }
}