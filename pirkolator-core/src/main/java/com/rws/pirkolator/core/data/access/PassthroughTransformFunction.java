package com.rws.pirkolator.core.data.access;

import com.rws.pirkolator.core.transform.AbstractTransformFunction;


public class PassthroughTransformFunction extends AbstractTransformFunction <Object, Object> {

    protected PassthroughTransformFunction () {

        super (Object.class, Object.class);
    }

    @Override
    public Object apply (final Object input) {

        final Object tmp = getToType().cast (input);
        if (tmp != null) {
            return tmp;
        }
        
        throw new NullPointerException ();
    }

}
