package com.rws.pirkolator.core.request;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.rws.pirkolator.core.engine.SystemResourceManager;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.core.store.RequestViewStore;
import com.rws.pirkolator.core.transform.RequestEventToViewFunction;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.request.Request;
import com.rws.pirkolator.model.request.RequestEvent;
import com.rws.pirkolator.model.request.RequestState;
import com.rws.utility.common.UUIDs;

public class RequestCoordinator extends AbstractSystemIdentifiable implements IShutdownListener {

    final static Logger LOG = notNull (LoggerFactory.getLogger (RequestCoordinator.class));

    public final static String PROP_REQUEST_TOPIC = "requestTopic";
    public final static String PROP_REQUESTSTART_TOPIC = "requestStartTopic";
    public final static String PROP_REQUESTCOORDINATOR_LIST = "requestCoordinatorList";
    public final static String PROP_REQUESTPROCESS_MAP = "requestProcessMap";
    public final static String PROP_REQUESTPROGRESS_QUEUE = "requestProgressQueue";
    public final static String PROP_REQUESTNOTCOMPLETE_QUEUE = "requestNotCompleteQueue";

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    public final String PROP_REQUESTACK_QUEUE;
    public final String PROP_REQUESTCOMPLETED_QUEUE;

    @Resource
    @Nullable
    HazelcastGrid grid;

    @Resource
    @Nullable
    RequestEventToViewFunction transform;

    @Resource
    @Nullable
    RequestViewStore viewStore;

    @Resource
    @Nullable
    SystemResourceManager resourceManager;

    EventBus eventBus = new EventBus ();
    WaitForAck waitForAck = new WaitForAck ();
    WaitForComplete waitForComplete = new WaitForComplete ();
    ProgressRunner progressRunner = new ProgressRunner ();
    RequestListener requestListener = new RequestListener ();
    StartListener startListener = new StartListener ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestCoordinator () {

        super (UUIDs.generateUUID (), "Request Coordinator");

        PROP_REQUESTACK_QUEUE = "requestack-" + getId ().toString ();
        PROP_REQUESTCOMPLETED_QUEUE = "requestcomplete-" + getId ().toString ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    HazelcastGrid getHazelcastGrid () {

        return notNull (grid, "The Hazelcast Grid for the Request Coordinator has not been defined."
                + " Check the Spring configuration to ensure the Request Coordinator has been defined.");
    }

    RequestEventToViewFunction getRequestEventToViewFunction () {

        return notNull (transform, "The RequestEvent To View transform function for the Request Coordinator"
                + " has not been defined. Check the Spring configuration to ensure"
                + " the Request Coordinator has been defined.");
    }

    RequestViewStore getRequestViewStore () {

        return notNull (viewStore, "The RequestViewStore for the Request Coordinator has not been defined."
                + " check the Spring configuration to ensure the bean has been setup properly.");
    }

    SystemResourceManager getSystemResourceManager () {

        return notNull (resourceManager, "The System Resource Manager instance is undefined."
                + " This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void register (final Object requestProcessor) {

        eventBus.register (requestProcessor);
    }

    public <T extends Request> void submitRequest (final T request) {

        if (getRequestProcessMap ().containsKey (request.getId ())) {
            throw new RuntimeException ("Request already submitted");
        }

        final long submitTime = System.currentTimeMillis ();

        final RequestProcess rp = new RequestProcess ();
        rp.setCoordinatorId (UUIDs.uuidAsStringOrDefault (getId ()));
        rp.setRequest (request);
        rp.setState (RequestState.SUBMITTED);
        rp.getStats ().setTimeSubmitted (submitTime);

        // These are the coordinators that we expect to send an ack
        final List<String> coordinatorList = new ArrayList<> ();
        for (final String coordinatorId : getRequestCoordinatorList ()) {
            if (!getId ().toString ().equals (coordinatorId)) {
                coordinatorList.add (coordinatorId);
            }
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Wait for ACKs from {}", coordinatorList);
        }

        getRequestProcessMap ().put (request.getId (), rp);
        getRequestViewStore ().put (
                getRequestEventToViewFunction ().apply (
                        new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ())));

        waitForAck.addWait (request.getId (), coordinatorList);
        getRequestTopic ().publish (request.getId ());
    }

    void ackRequest (final String requestId) {

        final RequestProcess rp = getRequestProcessMap ().get (requestId);
        if (rp == null) {
            throw new UnsupportedRequestException ("Unable to ACK request, request not submitted");
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Sending ACK from {}", getId ().toString ());
        }

        getRequestAckQueue (rp.getCoordinatorId ()).add (
                new RequestAck (requestId, UUIDs.uuidAsStringOrDefault (getId ())));
    }

    void startRequest (final String requestId, final List<String> coordinatorList) {

        final RequestProcess rp = getRequestProcessMap ().get (requestId);
        if (rp == null) {
            throw new UnsupportedRequestException ("Unable to start request, request not submitted");
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Starting {}", rp);
        }

        rp.setState (RequestState.INPROGRESS);

        // Update
        getRequestProcessMap ().put (requestId, rp);
        final RequestEvent requestEvent = new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ());
        getRequestViewStore ().put (getRequestEventToViewFunction ().apply (requestEvent));

        waitForComplete.addWait (requestId, coordinatorList);
        getRequestStartTopic ().publish (requestId);

        eventBus.post (requestEvent);
    }

    void processRequest (final String requestId) {

        final RequestProcess rp = getRequestProcessMap ().get (requestId);
        if (rp == null) {
            throw new UnsupportedRequestException ("Unable to process request, request not submitted");
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Processing {}", rp);
        }

        eventBus.post (rp.getRequest ());
    }

    public void completeRequest (final String requestId) {

        final RequestProcess rp = getRequestProcessMap ().get (requestId);
        if (rp == null) {
            throw new UnsupportedRequestException ("Unable to process request, request not submitted");
        }

        getRequestCompletedQueue (rp.getCoordinatorId ()).add (
                new RequestCompleted (requestId, UUIDs.uuidAsStringOrDefault (getId ())));
    }

    void finalizeRequest (final String requestId) {

        final RequestProcess rp = getRequestProcessMap ().get (requestId);
        if (rp == null) {
            throw new UnsupportedRequestException ("Unable to complete request, request not submitted");
        }

        // Done
        rp.getStats ().setTimeCompleted (System.currentTimeMillis ());
        rp.setState (RequestState.COMPLETED);

        // First update distributed coordinators with the state
        getRequestProcessMap ().put (requestId, rp);

        final RequestEvent requestEvent = new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ());
        getRequestViewStore ().put (getRequestEventToViewFunction ().apply (requestEvent));

        // Now move to a 'completed' processing queue
        getRequestProcessMap ().remove (requestId);

        eventBus.post (requestEvent);
    }

    public void updateProgress (final Request request, final int progressCount, final int totalProgress) {

        final RequestProcess rp = getRequestProcessMap ().get (request.getId ());
        if (rp == null) {
            throw new UnsupportedRequestException ("Request not submitted");
        }

        rp.setRequest (request);
        rp.getStats ().setCurrentProgress (progressCount);
        rp.getStats ().setTotalProgress (totalProgress);
        rp.setState (RequestState.INPROGRESS);

        getRequestProcessMap ().put (request.getId (), rp);

        final RequestEvent requestEvent = new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ());
        getRequestViewStore ().put (getRequestEventToViewFunction ().apply (requestEvent));

        getRequestProgressQueue ().add (request.getId ());
    }

    public void updateProgress (final String id) {

        final RequestProcess rp = getRequestProcessMap ().get (id);
        if (rp == null) {
            throw new UnsupportedRequestException ("Request not submitted");
        }

        final RequestEvent requestEvent = new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ());
        eventBus.post (requestEvent);
    }

    public void terminateRequest (final Request request) {

        final RequestProcess rp = getRequestProcessMap ().get (request.getId ());
        if (rp == null) {
            throw new UnsupportedRequestException ("Request not submitted");
        }

        rp.setRequest (request);
        rp.setState (RequestState.NOTCOMPLETED);

        // Update distributed coordinators with new state
        getRequestProcessMap ().put (request.getId (), rp);
        final RequestEvent requestEvent = new RequestEvent (rp.getRequest (), rp.getStats (), rp.getState ());
        getRequestViewStore ().put (getRequestEventToViewFunction ().apply (requestEvent));

        // Now move the request to the not complete queue for further processing
        getRequestProcessMap ().remove (request.getId ());
        getRequestNotCompleteQueue ().add (request);

        eventBus.post (requestEvent);
    }

    // *************************************************************************
    // ** Hazelcast Grid Maps 
    // *************************************************************************

    ITopic<String> getRequestTopic () {

        return getHazelcastGrid ().getTopic (PROP_REQUEST_TOPIC);
    }

    ITopic<String> getRequestStartTopic () {

        return getHazelcastGrid ().getTopic (PROP_REQUESTSTART_TOPIC);
    }

    IQueue<String> getRequestProgressQueue () {

        return getHazelcastGrid ().getQueue (PROP_REQUESTPROGRESS_QUEUE);
    }

    IQueue<RequestAck> getRequestAckQueue () {

        return getHazelcastGrid ().getQueue (PROP_REQUESTACK_QUEUE);
    }

    IQueue<RequestAck> getRequestAckQueue (final String coordinatorId) {

        return getHazelcastGrid ().getQueue ("requestack-" + coordinatorId);
    }

    IQueue<RequestCompleted> getRequestCompletedQueue () {

        return getHazelcastGrid ().getQueue (PROP_REQUESTCOMPLETED_QUEUE);
    }

    IQueue<RequestCompleted> getRequestCompletedQueue (final String coordinatorId) {

        return getHazelcastGrid ().getQueue ("requestcomplete-" + coordinatorId);
    }

    List<String> getRequestCoordinatorList () {

        return getHazelcastGrid ().getList (PROP_REQUESTCOORDINATOR_LIST);
    }

    IMap<String, RequestProcess> getRequestProcessMap () {

        return getHazelcastGrid ().getMap (PROP_REQUESTPROCESS_MAP);
    }

    IQueue<Request> getRequestNotCompleteQueue () {

        return getHazelcastGrid ().getQueue (PROP_REQUESTNOTCOMPLETE_QUEUE);
    }

    // *************************************************************************
    // ** Life-cycle 
    // *************************************************************************

    @Override
    public void shutdown () {

        try {
            getRequestCoordinatorList ().remove (getId ().toString ());
        } catch (final HazelcastInstanceNotActiveException ex) {
            LOG.warn ("Not removing request coordinates. Hazelcast instance has already shutdown.");
        }

        getSystemResourceManager ().getSingleThreadExecutor ("request-ack").shutdown ();
        getSystemResourceManager ().getSingleThreadExecutor ("request-complete").shutdown ();
        getSystemResourceManager ().getSingleThreadExecutor ("request-progress").shutdown ();
    }

    @PostConstruct
    private void postConstruct () {

        getHazelcastGrid ().registerShutdown (this);

        final ExecutorService ackService = getSystemResourceManager ().getSingleThreadExecutor ("request-ack");
        ackService.execute (waitForAck);

        final ExecutorService completedService =
                getSystemResourceManager ().getSingleThreadExecutor ("request-complete");
        completedService.execute (waitForComplete);

        final ExecutorService service = getSystemResourceManager ().getSingleThreadExecutor ("request-progress");
        service.execute (progressRunner);

        getRequestTopic ().addMessageListener (requestListener);
        getRequestStartTopic ().addMessageListener (startListener);
        getRequestCoordinatorList ().add (getId ().toString ());
    }

    @PreDestroy
    private void preDestroy () {

        try {
            getRequestCoordinatorList ().remove (getId ().toString ());
        } catch (final HazelcastInstanceNotActiveException ex) {
            LOG.warn ("Not removing request coordinates. Hazelcast instance has already shutdown.");
        }

        getSystemResourceManager ().getSingleThreadExecutor ("request-ack").shutdownNow ();
        getSystemResourceManager ().getSingleThreadExecutor ("request-complete").shutdownNow ();
        getSystemResourceManager ().getSingleThreadExecutor ("request-progress").shutdownNow ();
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class RequestListener implements MessageListener<String> {

        @Override
        public void onMessage (final @Nullable Message<String> message) {

            checkNotNull (message);

            ackRequest (notNull (message.getMessageObject ()));
        }
    }

    class WaitForAck implements Runnable {

        final Map<String, List<String>> coordinatorMap = Maps.newConcurrentMap ();
        final Map<String, List<String>> waitingMap = Maps.newConcurrentMap ();

        public WaitForAck () {

            super ();
        }

        public void addWait (final String requestId, final List<String> list) {

            coordinatorMap.put (requestId, list);
            waitingMap.put (requestId, list);
        }

        @Override
        public void run () {

            final AtomicBoolean running = new AtomicBoolean (true);
            while (running.get ()) {

                try {
                    final IQueue<RequestAck> queue = getRequestAckQueue ();

                    final RequestAck ra = queue.take ();

                    if (ra != null) {
                        if (LOG.isTraceEnabled ()) {
                            LOG.trace ("Received {}", ra);
                        }
                        final String requestId = ra.getRequestId ();
                        if (waitingMap.containsKey (requestId)) {
                            final List<String> ackList = waitingMap.get (requestId);
                            ackList.remove (ra.getCoordinatorId ());

                            if (ackList.isEmpty ()) {
                                waitingMap.remove (requestId);
                                final List<String> coordinatorList = coordinatorMap.get (requestId);

                                coordinatorMap.remove (requestId);
                                startRequest (requestId, notNull (coordinatorList));
                            }
                        } else {
                            LOG.warn ("Received {}, but no waiting Request", ra);
                        }
                    }
                } catch (final HazelcastInstanceNotActiveException ex) {
                    LOG.warn ("Stopping WaitForAck. Hazelcast instance has been shutdown.");
                    running.set (false);
                } catch (final InterruptedException ex) {
                    LOG.warn ("WaitForAck interrupted");
                    running.set (false);
                } catch (final IllegalStateException ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                } catch (final Exception ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                }
            }
        }
    }

    class StartListener implements MessageListener<String> {

        public StartListener () {

            super ();
        }

        @Override
        public void onMessage (final @Nullable Message<String> message) {

            checkNotNull (message);

            processRequest (notNull (message.getMessageObject ()));
        }
    }

    class WaitForComplete implements Runnable {

        final Map<String, List<String>> mWaitMap = new ConcurrentHashMap<> ();

        public WaitForComplete () {

            super ();
        }

        public void addWait (final String requestId, final List<String> coordinatorList) {

            mWaitMap.put (requestId, coordinatorList);
        }

        @Override
        public void run () {

            final AtomicBoolean running = new AtomicBoolean (true);
            while (running.get ()) {
                final IQueue<RequestCompleted> queue = getRequestCompletedQueue ();
                try {
                    final RequestCompleted requestCompleted = queue.take ();

                    if (requestCompleted != null) {
                        if (LOG.isTraceEnabled ()) {
                            LOG.trace ("Received {}", requestCompleted);
                        }

                        if (mWaitMap.containsKey (requestCompleted.getRequestId ())) {
                            final List<String> ackList = mWaitMap.get (requestCompleted.getRequestId ());

                            if (ackList.isEmpty ()) {
                                mWaitMap.remove (requestCompleted.getRequestId ());
                                finalizeRequest (requestCompleted.getRequestId ());
                            }
                        }
                    }

                } catch (final HazelcastInstanceNotActiveException ex) {
                    LOG.warn ("Stopping WaitForComplete. Hazelcast instance has been shutdown.");
                    running.set (false);
                } catch (final InterruptedException ex) {
                    LOG.warn ("WaitForComplete interrupted");
                    running.set (false);
                } catch (final IllegalStateException ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                } catch (final Exception ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                }
            }
        }
    }

    class ProgressRunner implements Runnable {

        public ProgressRunner () {

            super ();
        }

        @Override
        public void run () {

            final AtomicBoolean running = new AtomicBoolean (true);
            while (running.get ()) {
                final IQueue<String> queue = getRequestProgressQueue ();
                try {
                    final String requestId = queue.take ();

                    if (requestId != null) {
                        if (LOG.isTraceEnabled ()) {
                            LOG.trace ("Received progress update on {}", requestId);
                        }

                        updateProgress (requestId);
                    }

                } catch (final HazelcastInstanceNotActiveException ex) {
                    LOG.warn ("Stopping ProgressRunner. Hazelcast instance has been shutdown.");
                    running.set (false);
                } catch (final InterruptedException ex) {
                    LOG.warn ("ProgressRunner interrupted");
                    running.set (false);
                } catch (final IllegalStateException ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                } catch (final Exception ex) {
                    LOG.warn (ex.getLocalizedMessage ());
                    running.set (false);
                }
            }
        }
    }

}
