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

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserAdapter extends ArrayAdapter<User> {
    public static final String TAG = UserAdapter.class.getSimpleName();

    private final Activity activity;
    private final List<User> users;

    public UserAdapter(Activity activity, List<User> objects) {
        super(activity, R.layout.user_list_item , objects);
        //android.util.Log.d(TAG, "UserAdapter::constructor");
        this.activity = activity;
        this.users = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        UserView userView = null;

        if (rowView == null) {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.user_list_item, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            userView = new UserView();
            userView.image = (ImageView) rowView.findViewById(R.id.image);
            userView.name = (TextView) rowView.findViewById(R.id.name);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(userView);
        } else {
            userView = (UserView) rowView.getTag();
        }

        // Transfer the test data from the data object
        // to the view objects
        User currentUser = (User) users.get(position);
        if (currentUser != null) {
            userView.image.setImageBitmap(currentUser.getAvatar());
            userView.name.setText(currentUser.getName());
        } else {
            userView.name.setText("Unkown: " + position); // FIXME: Give better info
        }
        return rowView;
    }

    protected static class UserView {
        protected ImageView image;
        protected TextView name;
    }

}
