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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.exception.KinveyException;
import com.kinvey.util.ListCallback;

public class UsersActivity extends Activity {
    public static final String TAG = UsersActivity.class.getSimpleName();

    private KCSClient mSharedClient;
    private Calendar mCalendar;
    private List<User> mUsers;
    private JSONArray mUserIDs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users);
        mSharedClient = ((MainApplication) getApplication()).getKinveyService();
        try {
            mUserIDs = new JSONArray(getIntent().getStringExtra("ids"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        buildUserList();
    }

    public void buildUserList() {
        final ListView lv = (ListView) findViewById(R.id.userList);
        if (lv.getAdapter() != null) {
            ((UserAdapter) lv.getAdapter()).clear();
        }

        mUsers = new ArrayList<User>();
        Map<String, User> allUsers = ((MainApplication) getApplication()).getUsers();

        try {
            for (int i = 0; i < mUserIDs.length(); i++) {
                String userID = mUserIDs.getString(i);
                User user = allUsers.get(userID);
                mUsers.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        lv.setAdapter(new UserAdapter(UsersActivity.this, mUsers));
    }

    public void goBack(View view) {
        finish();
    }

}
