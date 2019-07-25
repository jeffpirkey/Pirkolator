package com.rws.pirkolator.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.ILabeledIdentifiable;

public abstract class AbstractLabeledIdentifiable extends AbstractIdentifiable implements ILabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String label;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractLabeledIdentifiable (final String id, final String label) {

        super (id);

        this.label = label;
    }

    public AbstractLabeledIdentifiable (final String id, final Metadata metadata, final String label) {

        super (id, metadata);

        this.label = label;
    }
    
    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public String getLabel () {

        return label;
    }

    @Override
    public void setLabel (final String label) {

        this.label = label;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), label);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof AbstractLabeledIdentifiable) {
            if (!super.equals (object))
                return false;
            final AbstractLabeledIdentifiable that = (AbstractLabeledIdentifiable) object;
            return Objects.equal (label, that.label);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("label", label).toString ();
    }

}
