package com.rws.pirkolator.core.data.engine;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.core.data.access.IDaoChannel;
import com.rws.pirkolator.core.engine.SystemResourceManager;

public class Archiver<T, ID extends Serializable> {

    static final Logger LOG = notNull (LoggerFactory.getLogger (Archiver.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    final IDaoChannel<T, ID> repo;
    final BlockingQueue<T> queue = new ArrayBlockingQueue<> (1024);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Archiver (final SystemResourceManager resourceManager, final IDaoChannel<T, ID> repo,
            final String daoSourceId) {

        super ();

        this.repo = repo;
        resourceManager.getSingleThreadExecutor ("archiver-" + daoSourceId).execute (new ArchiveTask ());
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void archive (final T object) {

        try {
            queue.put (object);
        } catch (final InterruptedException ex) {
            LOG.warn ("Archiver interrupted");
        }
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class ArchiveTask implements Runnable {

        AtomicBoolean mRunning = new AtomicBoolean ();

        @Override
        public void run () {

            mRunning.set (true);

            while (mRunning.get ()) {

                try {
                    final T object = queue.take ();
                    if (object != null) {
                        repo.save (object);
                    }
                } catch (final InterruptedException ex) {
                    LOG.warn ("Archiver interrupted");
                    mRunning.set (false);
                }

            }
        }
    }
}
