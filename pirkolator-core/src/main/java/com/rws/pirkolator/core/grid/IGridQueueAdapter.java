/*******************************************************************************
 * Copyright 2013 Reality Warp Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.rws.pirkolator.core.grid;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;

/**
 * This interface describes an adapter for an underlying data grid implementation
 * of a queue.  The underlying queue is used by the subscriber or publisher instance
 * to receive or deliver messages.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public interface IGridQueueAdapter {

    <T extends Serializable> BlockingQueue<T> getQueue(String name);

    void destroyQueue(String name);

    Lock getExclusiveQueueLock(String key);
}
