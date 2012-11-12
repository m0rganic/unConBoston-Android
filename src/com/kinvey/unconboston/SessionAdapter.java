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

public class SessionAdapter extends ArrayAdapter<Session> {
    public static final String TAG = SessionAdapter.class.getSimpleName();

    private final Activity activity;
    private final List<Session> sessions;

    public SessionAdapter(Activity activity, List<Session> objects) {
        super(activity, R.layout.session_list_item , objects);

        this.activity = activity;
        this.sessions = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        SessionView sessionView = null;

        if (rowView == null) {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.session_list_item, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            sessionView = new SessionView();
            sessionView.title = (TextView) rowView.findViewById(R.id.title);
            sessionView.leader = (TextView) rowView.findViewById(R.id.leader);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(sessionView);
        } else {
            sessionView = (SessionView) rowView.getTag();
        }

        // Transfer the data from the data object
        // to the view objects
        Session currentSession = (Session) sessions.get(position);
        sessionView.title.setText(currentSession.getTitle());
        sessionView.leader.setText("By: " + currentSession.getLeader());

        return rowView;
    }

    protected static class SessionView {
        protected TextView title;
        protected TextView leader;
    }

}
