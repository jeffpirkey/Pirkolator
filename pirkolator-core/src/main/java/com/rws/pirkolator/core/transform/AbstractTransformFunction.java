package com.rws.pirkolator.core.transform;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public abstract class AbstractTransformFunction<F, T> implements ITransformFunction<F, T> {

    @Nullable
    private String beanName;

    private final Class<F> fromType;
    private final Class<T> toType;

    protected AbstractTransformFunction (final Class<F> fromType, final Class<T> toType) {

        super ();

        this.fromType = fromType;
        this.toType = toType;
    }

    @Override
    public void setBeanName (final @Nullable String name) {

        beanName = name;
    }

    @Override
    public Class<T> getToType () {

        return toType;
    }

    @Override
    public Class<F> getFromType () {

        return fromType;
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), beanName, fromType, toType);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof AbstractTransformFunction) {
            if (!super.equals (object))
                return false;
            final AbstractTransformFunction<?, ?> that = (AbstractTransformFunction<?, ?>) object;
            return Objects.equal (this.beanName, that.beanName) && Objects.equal (this.fromType, that.fromType)
                    && Objects.equal (this.toType, that.toType);
        }
        return false;
    }

}
