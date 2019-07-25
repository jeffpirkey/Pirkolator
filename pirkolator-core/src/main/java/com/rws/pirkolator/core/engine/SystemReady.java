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
package com.rws.pirkolator.core.engine;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Sets;
import com.rws.pirkolator.core.engine.listener.IStartListener;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.utility.common.Globals;

public class SystemReady implements ApplicationContextAware {

    final static Logger LOG = notNull (LoggerFactory.getLogger (SystemReady.class));

    @Nullable
    private ApplicationContext appCtx;

    final CountDownLatch internalLatch;
    final CountDownLatch startupLatch;
    private final Set<UUID> idSet = Sets.newConcurrentHashSet ();
    final SystemInfo systemInfo;
    volatile boolean systemReady;
    private volatile boolean dalReady;
    private volatile boolean hubReady;

    final List<IReadyListener> mReadyList = new ArrayList<> ();

    public SystemReady (final SystemInfo si) {

        super ();

        systemInfo = si;

        startupLatch = new CountDownLatch (1);
        internalLatch = new CountDownLatch (2);
    }

    public SystemReady (final SystemInfo si, final Set<UUID> idSet) {

        super ();

        systemInfo = si;
        this.idSet.addAll (idSet);

        startupLatch = new CountDownLatch (1);
        // We add two to support the special DAL and HUB starts
        internalLatch = new CountDownLatch (idSet.size () + 2);
    }

    ApplicationContext getApplicationContext () {

        return notNull (appCtx, "Spring Application Context is undefined. "
                + "This indicates that Spring has not been initialized correctly.");
    }

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext ctx) throws BeansException {

        checkNotNull (ctx);
        appCtx = ctx;
    }

    public void addListener (final IReadyListener listener) {

        mReadyList.add (listener);

        if (systemReady) {
            listener.ready ();
        }
    }

    public void readyDal () {

        if (dalReady) {
            LOG.warn ("DAL has already been readied");
        } else {
            dalReady = true;
            internalLatch.countDown ();
        }
    }

    public void readyHub () {

        if (hubReady) {
            LOG.warn ("HUB has already been readied");
        } else {
            hubReady = true;
            internalLatch.countDown ();
        }
    }

    public void ready (final UUID processId) {

        if (idSet.remove (processId)) {
            internalLatch.countDown ();
        }
    }

    public boolean awaitReady () {

        try {
            startupLatch.await ();
        } catch (final InterruptedException ex) {
            LOG.warn ("Await System Ready interrupted");
            return false;
        }

        return true;
    }

    public boolean awaitReady (final long timeToWaitInMillis) {

        try {
            startupLatch.await (timeToWaitInMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            LOG.warn ("Await System Ready interrupted");
            return false;
        }

        return true;
    }

    @PostConstruct
    void postConstruct () {

        LOG.info ("{} SYSTEM STARTING - {}{}", Globals.DASH_LINE, systemInfo.getName (), Globals.DASH_LINE);

        Executors.newSingleThreadExecutor ().execute (new Loader ());
    }

    class Loader implements Runnable {

        @Override
        public void run () {

            try {
                internalLatch.await ();

                for (final IStartListener starter : getApplicationContext ().getBeansOfType (IStartListener.class)
                        .values ()) {
                    starter.started ();
                }

                LOG.info ("{} SYSTEM STARTED - {}{}", Globals.DASH_LINE, systemInfo.getName (),
                        Globals.DASH_LINE);

                for (final IReadyListener listener : mReadyList) {
                    listener.ready ();
                }

                systemReady = true;
                startupLatch.countDown ();
            } catch (final InterruptedException ex) {
                ex.printStackTrace ();
            }
        }
    }

    public interface IReadyListener {

        void ready ();
    }
}
