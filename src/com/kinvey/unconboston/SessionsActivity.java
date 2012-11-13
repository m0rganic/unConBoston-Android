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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.MappedAppdata;
import com.kinvey.persistence.query.SimpleQuery;
import com.kinvey.persistence.query.SortQueryModifier;
import com.kinvey.util.ListCallback;

public class SessionsActivity extends Activity {
    public static final String TAG = SessionsActivity.class.getSimpleName();

    private KCSClient mSharedClient;
    private Calendar mCalendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sessions);
        mSharedClient = ((MainApplication) getApplication()).getKinveyService();
        mCalendar = ((MainApplication) getApplication()).getAppCalendar();
        findUsers();
        buildSessionList();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (((MainApplication) getApplication()).isDirty()) {
            buildSessionList();
        }
    }

    public void buildSessionList() {
        final SeparatedListAdapter adapter = new SeparatedListAdapter(this);
        final ListView lv = (ListView) this.findViewById(R.id.sessionList);

        ((MainApplication) getApplication()).setDirty(false);
        // make synchronous calls so we build the list in the correct order
        buildSessionList(adapter, lv, 0);
    }

    public void buildSessionList(final SeparatedListAdapter adapter, final ListView lv, final int index) {
        final String[] sessionTimes = getResources().getStringArray(R.array.session_times);
        final String sessionTime = sessionTimes[index];
    
        final ArrayList<Session> sessions = new ArrayList<Session>();
        SimpleQuery q = new SimpleQuery();
        q.addCriteria("time", "==", sessionTime);
        q.setSortQueryModifier(new SortQueryModifier("title", false));
        MappedAppdata mappedAppdata = mSharedClient.mappeddata("Updates");
        mappedAppdata.setQuery(q);
        mappedAppdata.fetch(SessionEntity.class, new ListCallback<SessionEntity>() {
            @Override
            public void onFailure(Throwable t) {
                android.util.Log.w(TAG, "Error fetching sessions data: " + t.getMessage());
            }

            @Override
            public void onSuccess(List<SessionEntity> sessionEntities) {
                for (SessionEntity sessionEntity : sessionEntities) {
                    Session session = new Session(sessionEntity, mCalendar);
                    sessions.add(session);
                }

                SessionAdapter listadapter = new SessionAdapter(SessionsActivity.this, sessions);
                adapter.addSection(sessionTime, listadapter);
                lv.setAdapter(adapter);

                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent newIntent = new Intent(SessionsActivity.this, ViewSessionActivity.class);
                        Session session = (Session) adapter.getItem(position);
                        newIntent.putExtra("id", session.getId());
                        newIntent.putExtra("title", session.getTitle());
                        newIntent.putExtra("leader", session.getLeader());
                        newIntent.putExtra("location", session.getLocation());
                        newIntent.putExtra("time", session.getTime());
                        newIntent.putExtra("description", session.getDescription());
                        newIntent.putExtra("attendees", session.getAttendees().toString());
                        newIntent.putExtra("authorId", session.getAuthor());
                        SessionsActivity.this.startActivity(newIntent);
                    }
                });
                
                if (index < sessionTimes.length-1) {
                    buildSessionList(adapter, lv, index + 1);
                }

            }
        });
    }

    public void findUsers() {
        mSharedClient.userCollection().allUsers(new ListCallback<KinveyUser>() {
            @Override
            public void onFailure(Throwable t) {
               android.util.Log.w(TAG, "Error fetching user data: " + t.getMessage());
            }

            @Override
            public void onSuccess(List<KinveyUser> kinveyUsers) {
                //android.util.Log.d(TAG, "Count of users found: " + kinveyUsers.size());
                Map<String, User> users = new TreeMap<String, User>();
                for (KinveyUser kinveyUser : kinveyUsers) {
                    User user = new User();
                    user.setId((String) (kinveyUser.getAttribute("_id")));

                    try {
                        JSONObject socialIdentity = (JSONObject) (kinveyUser.getAttribute("_socialIdentity"));
                        if (socialIdentity != null) {
                            if (socialIdentity.has("facebook") ==  true) {
                                JSONObject facebookIdentity = socialIdentity.getJSONObject("facebook");
                                if (facebookIdentity != null) {
                                    user.setName(facebookIdentity.getString("name"));
                                }
                            } else if (socialIdentity.has("google") ==  true) {
                                JSONObject twitterIdentity = socialIdentity.getJSONObject("google");                            
                                if (twitterIdentity != null) {
                                    user.setName(twitterIdentity.getString("name"));
                                }
                            }
                        }
                        if (user.getName() == null) {
                            user.setName(kinveyUser.getUsername());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                   android.util.Log.d(TAG, user.getId() + ": " + user.getName() + " " + kinveyUser.getAttribute("_socialIdentity"));
                   users.put(user.getId(), user);
                }
                ((MainApplication) getApplication()).setUsers(users);
            }
        });
    }

    public void tryToLogout(View view) {
        mSharedClient.getCurrentUser().logout();
        
        // deletes the saved user context
        SharedPreferences userdetails = getSharedPreferences(LoginSocialActivity.USER_DETAILS, MODE_PRIVATE);
        SharedPreferences.Editor editor = userdetails.edit();
        editor.clear();
        editor.commit();
        
        Intent intent = new Intent(this, LoginSocialActivity.class);
        startActivity(intent);
        finish();
    }

    public void tryToEditSession(View view) {
        startActivity(new Intent(this, EditSessionActivity.class));
    }

}
