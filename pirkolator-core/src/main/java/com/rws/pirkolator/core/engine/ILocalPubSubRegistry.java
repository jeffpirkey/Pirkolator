package com.rws.pirkolator.core.engine;

import java.util.UUID;


public interface ILocalPubSubRegistry {

    void registerSubscriber (UUID pubSubId, ISubscriber subscriber);

    void unregisterSubscriber (ISubscriber subscriber);

    void registerPublisher (UUID pubSubId, IPublisher publisher);

    void unregisterPublisher (IPublisher publisher);
}
