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

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kinvey.KinveyMetadata;
import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

/**
 * Stores information for syncing with Kinvey backend.
 *
 */
public class SessionEntity implements MappedEntity {

    public static final String TAG = SessionEntity.class.getSimpleName();

/*
@synthesize userDate = _userDate;*/

    protected String id;
    protected String title;
    protected String leader;
    protected String location;
    protected String time;
    protected String description;
    protected JSONArray attendees;
    protected KinveyMetadata meta;
    protected JSONObject attachment;

    public SessionEntity() {
        id = null;
        title = null;
        leader = null;
        location = null;
        time = null;
        description = null;
        attendees = null;
        meta = null;
        attachment = null;
    }

    public SessionEntity(SessionEntity s) {
        id = s.id;
        title = s.title;
        leader = s.leader;
        location = s.location;
        time = s.time;
        description = s.description;
        attendees = s.attendees;
        meta = s.meta;
        attachment = s.attachment;
    }

    @Override
    public List<MappedField> getMapping() {
        return Arrays.asList(new MappedField[] {
            new MappedField("id", "_id")
            , new MappedField("title", "title")
            , new MappedField("leader", "leader")
            , new MappedField("location", "location")
            , new MappedField("time", "time")
            , new MappedField("description", "description")
            , new MappedField("attendees", "attendees")
            , new MappedField("meta", KinveyMetadata.FIELD_NAME)
            , new MappedField("attachment", "attachment")
            });
    }

    // Getters and setters for all fields are required
    public String getId() { return id; }
    public void setId(String i) { id = i; }

    public String getTitle() { return title; }
    public void setTitle(String t) { title = t; 
        android.util.Log.w(TAG, "title: " + title);
    }

    public String getLeader() { return leader; }
    public void setLeader(String l) { leader = l; }

    public String getLocation() { return location; }
    public void setLocation(String l) { location = l; }

    public String getTime() { return time; }
    public void setTime(String t) { time = t; }

    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }

    public JSONArray getAttendees() { return attendees; }
    public void setAttendees(JSONArray a) { attendees = a;
        android.util.Log.w(TAG, "attendees: " + attendees);
    }

    public KinveyMetadata getMeta() { return meta; }
    public void setMeta(KinveyMetadata m) { meta = m; }

    public JSONObject getAttachment() { return attachment; }
    public void setAttachment(JSONObject a) {
        attachment = a;
    }
}
