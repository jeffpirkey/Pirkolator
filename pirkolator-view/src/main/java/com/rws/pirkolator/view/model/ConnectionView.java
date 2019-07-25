package com.rws.pirkolator.view.model;

import com.rws.pirkolator.model.AbstractIdentifiable;

public class ConnectionView extends AbstractIdentifiable {

    private static final long serialVersionUID = 1L;

    public enum ConnectionType {
        TO, FROM, BI, UNDEFINED;
    }

    public ConnectionView (final String id, final String connectedId, final ConnectionType connectionType) {

        super (id);
        this.connectedId = connectedId;
        this.connectionType = connectionType;
    }

    private final String connectedId;
    private final ConnectionType connectionType;


    public String getConnectedId () {

        return connectedId;
    }

    public ConnectionType getConnectionType () {

        return connectionType;
    }
}
