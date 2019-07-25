package com.rws.pirkolator.core.engine;

import java.util.Set;

public interface IQueueGroupManager {

    String getQueueName();
    
    void addQueue (MessageQueue queue);
    
    Set<MessageQueue> getAllQueueSet ();
    
    Set<MessageQueue> nextQueueSet ();

}