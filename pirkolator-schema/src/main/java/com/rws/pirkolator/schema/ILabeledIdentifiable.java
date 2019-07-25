package com.rws.pirkolator.schema;

public interface ILabeledIdentifiable extends IIdentifiable {

    /**
     * A mutable name for this instance.
     * 
     * @return {@link String} instance with the given name
     */
    String getLabel ();

    void setLabel (String label);
}
