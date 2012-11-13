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

import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kinvey.KCSClient;
import com.kinvey.MappedAppdata;
import com.kinvey.persistence.EntityDict;
import com.kinvey.persistence.query.SimpleQuery;
import com.kinvey.util.ListCallback;
import com.kinvey.util.ScalarCallback;

public class ViewSessionActivity extends Activity {
    public static final String TAG = ViewSessionActivity.class.getSimpleName();

    protected KCSClient mSharedClient;
    private Calendar mCalendar;
    private String mId;
    private String mAttendees;
    private Session mSession;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedClient = ((MainApplication) getApplication()).getKinveyService();
        mCalendar = ((MainApplication) getApplication()).getAppCalendar();

        setContentView(R.layout.view_session);

        Intent myIntent = getIntent();

        mSession = new Session();
        mSession.setId(myIntent.getStringExtra("id"));
        mSession.setTitle(myIntent.getStringExtra("title"));
        mSession.setLeader(myIntent.getStringExtra("leader"));
        mSession.setLocation(myIntent.getStringExtra("location"));
        mSession.setTime(myIntent.getStringExtra("time"));
        mSession.setDescription(myIntent.getStringExtra("description"));
        try {
            mSession.setAttendees(new JSONArray(myIntent.getStringExtra("attendees")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSession.setAuthor(myIntent.getStringExtra("authorId"));
        updateUI(mSession);
    }

    public void updateUI(Session session) {
        mId = session.getId();
        mAttendees = session.getAttendees().toString();

        ((TextView) findViewById(R.id.toolbarTitle)).setText(session.getTitle());
        ((TextView) findViewById(R.id.title)).setText(session.getTitle());
        ((TextView) findViewById(R.id.leader)).setText(session.getLeader());
        ((TextView) findViewById(R.id.location)).setText(session.getLocation());
        ((TextView) findViewById(R.id.time)).setText(session.getTime());
        ((TextView) findViewById(R.id.attendees)).setText(Integer.toString(session.getAttendees().length()));
        ((TextView) findViewById(R.id.description)).setText(session.getDescription());

        String currentUserId = mSharedClient.getCurrentUser().getId();
        if(mAttendees.indexOf(currentUserId) > -1) {
            ((Button) findViewById(R.id.attendSession)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.unattendSession)).setVisibility(View.VISIBLE);
        } else {
            ((Button) findViewById(R.id.attendSession)).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.unattendSession)).setVisibility(View.GONE);
        }

        if(currentUserId.equals(session.getAuthor())) {
            ((Button) findViewById(R.id.editSession)).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.deleteSession)).setVisibility(View.VISIBLE);
        } else {
            ((Button) findViewById(R.id.editSession)).setVisibility(View.GONE);
            ((Button) findViewById(R.id.deleteSession)).setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (((MainApplication) getApplication()).isDirty()) {
            finish(); // FIXME need a refresh instead of bailing
        }
    }

    public void goBack(View view) {
        finish();
    }

    public void viewAttendees(View view) {
        Intent newIntent = new Intent(ViewSessionActivity.this, UsersActivity.class);
        newIntent.putExtra("ids", mAttendees);
        startActivity(newIntent);
    }
    
    public void attendSession(View view) {
        SimpleQuery q = new SimpleQuery();
        q.addCriteria("_id", "==", mId);

        MappedAppdata mappedAppdata = mSharedClient.mappeddata("Updates");
        mappedAppdata.setQuery(q);
        mappedAppdata.fetch(SessionEntity.class, new ListCallback<SessionEntity>() {
            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(List<SessionEntity> sessionEntities) {
                SessionEntity sessionEntity = sessionEntities.get(0);
                sessionEntity.getAttendees().put(mSharedClient.getCurrentUser().getId());
                
                // FIXME some sort of permissions error is preventing this working on non-owned records
                // FIXME the updateUI code may not work
                mSharedClient.mappeddata("Updates").save(sessionEntity, new ScalarCallback<SessionEntity>() {
                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                    }
        
                    @Override
                    public void onSuccess(SessionEntity updatedSessionEntity) {
                        mSession = new Session(updatedSessionEntity, mCalendar);
                        updateUI(mSession);
                        ((MainApplication) getApplication()).setDirty(true);
                    }
                });
            }
        });
    }

    public void unattendSession(View view) {
        SimpleQuery q = new SimpleQuery();
        q.addCriteria("_id", "==", mId);

        MappedAppdata mappedAppdata = mSharedClient.mappeddata("Updates");
        mappedAppdata.setQuery(q);
        mappedAppdata.fetch(SessionEntity.class, new ListCallback<SessionEntity>() {
            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(List<SessionEntity> sessionEntities) {
                SessionEntity sessionEntity = sessionEntities.get(0);
                String currentUserID = mSharedClient.getCurrentUser().getId();
                try {
                    JSONArray attendees = sessionEntity.getAttendees();
                    for (int i = 0; i < attendees.length(); i++) {
                        if (currentUserID.equals(attendees.getString(i))) {
                            // FIXME this is causing a compile issue
                            // attendees.remove(i);
                            sessionEntity.setAttendees(attendees);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mSharedClient.mappeddata("Updates").save(sessionEntity, new ScalarCallback<SessionEntity>() {
                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                    }
        
                    @Override
                    public void onSuccess(SessionEntity updatedSessionEntity) {
                        mSession = new Session(updatedSessionEntity, mCalendar);
                        updateUI(mSession);
                        ((MainApplication) getApplication()).setDirty(true);
                    }
                });
            }
        });
    }

    public void editSession(View view) {
        Intent newIntent = new Intent(this, EditSessionActivity.class);
        newIntent.putExtra("id", mSession.getId());
        newIntent.putExtra("title", mSession.getTitle());
        newIntent.putExtra("leader", mSession.getLeader());
        newIntent.putExtra("location", mSession.getLocation());
        newIntent.putExtra("time", mSession.getTime());
        newIntent.putExtra("description", mSession.getDescription());
        newIntent.putExtra("attendees", mSession.getAttendees().toString());
        newIntent.putExtra("authorId", mSession.getAuthor());
        startActivity(newIntent);
    }

    public void deleteSession(View view) {

        ((Button) findViewById(R.id.attendSession)).setEnabled(false);
        ((Button) findViewById(R.id.unattendSession)).setEnabled(false);
        ((Button) findViewById(R.id.editSession)).setEnabled(false);
        ((Button) findViewById(R.id.deleteSession)).setEnabled(false);

        EntityDict entityDict = new EntityDict(mSharedClient, "Updates");
        entityDict.delete(mId, new ScalarCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                android.util.Log.w(TAG, "Error deleting data ( " + mId + " ): " + t.getMessage());
            }

            @Override
            public void onSuccess(Void v) {
                android.util.Log.v(TAG, "Sucessfully Deleted: " + mId);
                ((MainApplication) getApplication()).setDirty(true);
                finish();
            }
        });
    }
}
