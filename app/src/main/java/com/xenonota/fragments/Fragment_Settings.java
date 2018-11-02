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

package com.xenonota.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.xenonota.R;
import com.xenonota.configs.AppConfig;
import com.xenonota.configs.GappsConfig;
import com.xenonota.utils.OTAUtils;

public class Fragment_Settings extends Fragment {

    RadioGroup GappsVariant;
    RadioGroup AutoUpdateInterval;
    RadioGroup PreferredType;

    static Fragment_Settings fragment;

    public static Fragment_Settings newInstance() {
        fragment = new Fragment_Settings();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        GappsVariant = view.findViewById(R.id.rg_gapps);
        AutoUpdateInterval = view.findViewById(R.id.rg_autocheck_interval);
        PreferredType = view.findViewById(R.id.rg_preferred_type);
        LoadPreferences();
        AssignEvents();
        return view;
    }

    private void LoadPreferences(){
        String gappsVariant = GappsConfig.getVariant(getContext().getApplicationContext());
        switch (gappsVariant){
            case "aroma":{
                GappsVariant.check(R.id.rb_aroma);
                break;
            }
            case "super":{
                GappsVariant.check(R.id.rb_super);
                break;
            }
            case "stock":{
                GappsVariant.check(R.id.rb_stock);
                break;
            }
            case "full":{
                GappsVariant.check(R.id.rb_full);
                break;
            }
            case "mini":{
                GappsVariant.check(R.id.rb_mini);
                break;
            }
            case "micro":{
                GappsVariant.check(R.id.rb_micro);
                break;
            }
            case "nano":{
                GappsVariant.check(R.id.rb_nano);
                break;
            }
            case "pico":{
                GappsVariant.check(R.id.rb_pico);
                break;
            }
            case "tvstock":{
                GappsVariant.check(R.id.rb_tvstock);
                break;
            }
        }

        String preferredType = AppConfig.getPreferredType(getContext().getApplicationContext());
        switch (preferredType){
            case "Official":{
                PreferredType.check(R.id.rb_official);
                break;
            }
            case "Experimental":{
                PreferredType.check(R.id.rb_experimental);
                break;
            }
        }

        int updateInterval = AppConfig.getUpdateIntervalIndex(getContext().getApplicationContext());
        switch (updateInterval){
            case 0:{
                AutoUpdateInterval.check(R.id.rb_disabled);
                break;
            }
            case 1:{
                AutoUpdateInterval.check(R.id.rb_onehour);
                break;
            }
            case 2:{
                AutoUpdateInterval.check(R.id.rb_halfday);
                break;
            }
            case 3:{
                AutoUpdateInterval.check(R.id.rb_day);
                break;
            }
            case 4:{
                AutoUpdateInterval.check(R.id.rb_onboot);
                break;
            }
        }
    }

    private void AssignEvents(){
        GappsVariant.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                OTAUtils.logInfo("store var ");
                RadioButton selected = group.findViewById(checkedId);
                OTAUtils.logInfo("store var " + selected.getText().toString());
                GappsConfig.setVariant(selected.getText().toString(),getContext().getApplicationContext());
            }
        });
        AutoUpdateInterval.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selected = group.findViewById(checkedId);
                switch(selected.getId()){
                    case R.id.rb_disabled:{
                        AppConfig.persistUpdateIntervalIndex(0,getContext().getApplicationContext());
                        break;
                    }
                    case R.id.rb_onehour:{
                        AppConfig.persistUpdateIntervalIndex(1,getContext().getApplicationContext());
                        break;
                    }
                    case R.id.rb_halfday:{
                        AppConfig.persistUpdateIntervalIndex(2,getContext().getApplicationContext());
                        break;
                    }
                    case R.id.rb_day:{
                        AppConfig.persistUpdateIntervalIndex(3,getContext().getApplicationContext());
                        break;
                    }
                    case R.id.rb_onboot:{
                        AppConfig.persistUpdateIntervalIndex(4,getContext().getApplicationContext());
                        break;
                    }
                }
            }
        });
        PreferredType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selected = group.findViewById(checkedId);
                switch (selected.getId()){
                    case R.id.rb_official:{
                        AppConfig.persistPreferredVersion("Official",getContext().getApplicationContext());
                        break;
                    }
                    case R.id.rb_experimental:{
                        AppConfig.persistPreferredVersion("Experimental",getContext().getApplicationContext());
                        break;
                    }
                }
            }
        });
    }
}
