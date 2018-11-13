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

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xenonota.R;

public class WaitDialogFragment extends DialogFragment {

    private int msgId;

    public static WaitDialogFragment newInstance(int msgId) {
        WaitDialogFragment frag = new WaitDialogFragment();
        frag.msgId = msgId;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(true);

        View progressDialog_layout = View.inflate(getContext(), R.layout.dialog_progress, null);
        TextView title = progressDialog_layout.findViewById(R.id.titleTextView);
        if (getContext() != null) {
            if (msgId > 0) title.setText(getContext().getText(msgId));
        }

        AlertDialog.Builder progressDialog_builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
        progressDialog_builder.setView(progressDialog_layout);
        progressDialog_builder.setCancelable(false);
        AlertDialog progressDialog = progressDialog_builder.create();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }

    @Override
    public void onDestroyView() {
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getOTADialogListener() != null) {
            getOTADialogListener().onProgressCancelled();
        }
    }

    private OTADialogListener getOTADialogListener() {
        if (getActivity() instanceof OTADialogListener) {
            return (OTADialogListener) getActivity();
        }
        return null;
    }

    public interface OTADialogListener {
        void onProgressCancelled();
    }
}
