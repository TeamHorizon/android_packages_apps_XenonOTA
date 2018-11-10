/*
 * Copyright (C) 2017 Team Horizon
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

package com.xenonota.dialogs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

public class WaitDialogHandler extends Handler {

    public static final int MSG_SHOW_DIALOG = 0;
    public static final int MSG_CLOSE_DIALOG = 1;

    private static final String DIALOG_TAG = WaitDialogFragment.class.getName();

    private Context mContext;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SHOW_DIALOG:
                mContext = (Context) msg.obj;
                if (mContext instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) mContext;

                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    Fragment prev = getOTADialogFragment();
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    WaitDialogFragment dialog = WaitDialogFragment.newInstance(msg.arg1);
                    dialog.show(ft, DIALOG_TAG);
                }
                break;
            case MSG_CLOSE_DIALOG:
                WaitDialogFragment dialog = getOTADialogFragment();
                if (dialog != null) {
                    dialog.dismissAllowingStateLoss();
                }
                break;
            default:
                break;
        }
    }

    private WaitDialogFragment getOTADialogFragment() {
        if (mContext instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) mContext;
            Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(DIALOG_TAG);
            return (WaitDialogFragment) fragment;
        }
        return null;
    }
}
