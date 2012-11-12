/*
 * Copyright (c) 2012 Kinvey, Inc. All rights reserved.
 *
 * Licensed to Kinvey, Inc. under one or more contributor
 * license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership.  Kinvey, Inc. licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You
 * may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Author: Tom Giesberg
 */

package com.kinvey.unconboston;

import java.io.File;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.kinvey.KCSClient;
import com.kinvey.KinveyMetadata;
import com.kinvey.KinveyResource;
import com.kinvey.util.KinveyCallback;
import com.kinvey.util.ScalarCallback;

public class EditSessionActivity extends Activity {
    public static final String TAG = EditSessionActivity.class.getSimpleName();

    protected KCSClient mSharedClient;
    private String mId;
    private String mAttendees;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedClient = ((MainApplication) getApplication()).getKinveyService();

        setContentView(R.layout.edit_session);

        Intent myIntent = getIntent();
        mId = myIntent.getStringExtra("id");
        mAttendees = myIntent.getStringExtra("attendees");
        String currentUserId = mSharedClient.getCurrentUser().getId();

        ((EditText) findViewById(R.id.title)).setText(myIntent.getStringExtra("title"));
        ((EditText) findViewById(R.id.leader)).setText(myIntent.getStringExtra("leader"));
        ((EditText) findViewById(R.id.location)).setText(myIntent.getStringExtra("location"));
        ((EditText) findViewById(R.id.description)).setText(myIntent.getStringExtra("description"));

        Spinner spinner = (Spinner) findViewById(R.id.time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.session_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(myIntent.getStringExtra("time")));
    }

    public void cancelSession(View view) {
        finish();
    }
    
    public void postSession(View view) {
        final ProgressDialog progressDialog = ProgressDialog.show(EditSessionActivity.this, "",
                        "Posting. Please wait...", true);
        saveSession(progressDialog);
    }

    public void saveSession(final ProgressDialog progressDialog) {
        SessionEntity sessionEntity = new SessionEntity();

        sessionEntity.setId(mId);
        sessionEntity.setTitle(((EditText) findViewById(R.id.title)).getText().toString());
        sessionEntity.setLeader(((EditText) findViewById(R.id.leader)).getText().toString());
        sessionEntity.setLocation(((EditText) findViewById(R.id.location)).getText().toString());
        sessionEntity.setTime(((Spinner) findViewById(R.id.time)).getSelectedItem().toString());
        sessionEntity.setDescription(((EditText) findViewById(R.id.description)).getText().toString());

        try {
            JSONArray attendees = new JSONArray();
            if (mAttendees != null) {
                attendees = new JSONArray(mAttendees);
            }
            sessionEntity.setAttendees(attendees);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        mSharedClient.mappeddata("Updates").save(sessionEntity, new ScalarCallback<SessionEntity>() {
            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                progressDialog.dismiss();
                finish();
            }

            @Override
            public void onSuccess(SessionEntity updateEntity) {
                android.util.Log.d(TAG, "postSession: SUCCESS _id = " + updateEntity.getId());
                progressDialog.dismiss();
                ((MainApplication) getApplication()).setDirty(true);
                finish();
            }
        });
    }
}
