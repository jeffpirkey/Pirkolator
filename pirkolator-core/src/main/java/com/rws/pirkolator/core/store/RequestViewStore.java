package com.rws.pirkolator.core.store;

import com.rws.pirkolator.view.model.RequestView;

public class RequestViewStore extends AbstractLocalMapStore<RequestView> {

    public RequestViewStore () {

        super (RequestView.class);
    }

    public final static String PROP_REQUESTVIEW_STORE = "requestViewStore";

    @Override
    public String getMapName () {

        return PROP_REQUESTVIEW_STORE;
    }

}
