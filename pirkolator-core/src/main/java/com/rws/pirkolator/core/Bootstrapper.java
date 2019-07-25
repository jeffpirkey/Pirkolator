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
package com.rws.pirkolator.core;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class provides lifecycle support to keeping the SystemInfo running, even if
 * nothing is being processed.  It manages loading and starting the required Spring
 * context in the "resources/config/system-context.xml" XML definition.
 * 
 * @author pirk
 * @since 1.0.0
 */
public class Bootstrapper {

    static final Logger LOG = notNull (LoggerFactory.getLogger (Bootstrapper.class));

    static ClassPathXmlApplicationContext sApplicationContext = new ClassPathXmlApplicationContext (
            "resources/config/system-context.xml");

    static AtomicBoolean sStarted = new AtomicBoolean (false);
    static final Object sRunLock = new Object ();

    // *************************************************************************
    // ** SystemInfo Command-line Execution
    // *************************************************************************

    public static void main (final String[] args) {

        try {
            start ();
        } catch (final Throwable ex) {
            LOG.error ("Unable to start the Pirkolator --> " + ex.getLocalizedMessage (), ex);
            System.exit (-1);
        }
    }

    public static void start () {

        if (sStarted.get ()) {
            LOG.warn ("The Pirkolator has already been started.");
            return;
        }

        final Thread shutdownThread = new Thread (new Runnable () {

            @Override
            public void run () {

                LOG.info ("Shutting down the Pirkolator...");
                synchronized (sRunLock) {
                    if (!sStarted.get ()) {
                        LOG.warn ("Pirkolator has not been started or has already been stopped.");
                        return;
                    }

                    sStarted.set (false);
                    sRunLock.notify ();
                    //sApplicationContext.close ();
                }
                LOG.info ("The Pirkolator has been shutdown.");
            }
        });

        Runtime.getRuntime ().addShutdownHook (shutdownThread);
        sApplicationContext.registerShutdownHook ();
        run ();
    }

    /**
     * A blocking method to keep the main thread alive.
     */
    private static void run () {

        synchronized (sRunLock) {

            sApplicationContext.start ();
            sStarted.set (true);

            while (sStarted.get ()) {
                try {
                    sRunLock.wait ();
                } catch (final InterruptedException ex) {
                    if (sStarted.get ()) {
                        LOG.error ("Execution thread interrupted.", ex);
                    }
                    sApplicationContext.stop ();
                    sStarted.set (false);
                }
            }
        }
    }
}
