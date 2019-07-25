package com.rws.pirkolator.model;

import com.rws.pirkolator.schema.IUserSession;
import com.rws.utility.common.UUIDs;

public class UserSession implements IUserSession {

    private final String userId;
    private final String sessionId;

    public UserSession () {
        
        super ();
        
        userId = UUIDs.defaultUUIDAsString ();
        sessionId = UUIDs.defaultUUIDAsString ();
    }
    
    public UserSession (final String userId, final String sessionId) {

        super ();

        this.userId = userId;
        this.sessionId = sessionId;
    }

    @Override
    public String getUserId () {

        return userId;
    }

    @Override
    public String getSessionId () {

        return sessionId;
    }

}
