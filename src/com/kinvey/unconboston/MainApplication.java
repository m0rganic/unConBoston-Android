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
import java.util.Map;
import java.util.TimeZone;

import android.app.Application;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;

/**
 * Store global state. In this case, the single instance of KCS.
 *
 */
public class MainApplication extends Application {

    public static final String TAG = MainApplication.class.getSimpleName();

    private KCSClient mService;
    private Calendar mAppCalendar;
    private Map<String, User> mUsers;
    private boolean mDirty;
    
    // Enter your Kinvey app credentials
//    private static final String APP_KEY = "<your_app_key>";
//    private static final String APP_SECRET = "<your_app_secret>";
/*
    NSDictionary *options = @{ KCS_PUSH_IS_ENABLED_KEY : @"YES",
    KCS_PUSH_KEY_KEY : @"2l-5BkPkSe6fkf2IHeUbyg",
    KCS_PUSH_SECRET_KEY : @"4MV50jGcQl-ik0B0TWjd7Q",
    KCS_PUSH_MODE_KEY : KCS_PUSH_DEBUG,
    KCS_TWITTER_CLIENT_KEY : @"QTyGt9aQpeZLKJoTauRjnw",
    KCS_TWITTER_CLIENT_SECRET : @"yUd8JGhUXQNUlHfZOFNaKpmYJcwAC3vJtbJHNarINY"};
*/    

//    private static final String APP_KEY = "kid_PPnCuemfeJ";
//    private static final String APP_SECRET = "9f99a4ff97764f8eb18e2d3eb3a1457f";

    private static final String APP_KEY = "kid_TTmQdBb2Pf"; // kid_TTLKLIe2N";
    private static final String APP_SECRET = "6b47b78bee014f489c5ba3e593dfd8b5"; // 1c5df31e91554bdabf9cba77db4417a9";

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        mService = KCSClient.getInstance(this.getApplicationContext(), new KinveySettings(APP_KEY, APP_SECRET));

        TimeZone reference = TimeZone.getTimeZone("GMT");
        mAppCalendar = Calendar.getInstance(reference);
        TimeZone.setDefault(reference);
        setDirty(true);
    }

    public KCSClient getKinveyService() {
        return mService;
    }

    public Calendar getAppCalendar() {
        return mAppCalendar;
    }

    public Map<String, User> getUsers() {
        return mUsers;
    }

    public void setUsers (Map<String, User> users) {
        mUsers = users;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty (boolean dirty) {
        mDirty = dirty;
    }
}
