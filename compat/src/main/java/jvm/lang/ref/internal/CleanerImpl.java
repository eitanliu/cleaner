package jvm.lang.ref.internal;


import jvm.lang.ref.Cleaner;
import jvm.lang.ref.Cleaner.Cleanable;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CleanerImpl manages a set of object references and corresponding cleaning actions.
 * CleanerImpl provides the functionality of {@link java.lang.ref.Cleaner}.
 */
public final class CleanerImpl implements Runnable {

    /**
     * An object to access the CleanerImpl from a Cleaner; set by Cleaner init.
     */
    private static Function<Cleaner, CleanerImpl> cleanerImplAccess = null;

    /**
     * Heads of a CleanableList for each reference type.
     */
    final PhantomCleanable<?> phantomCleanableList;

    // Android-removed: WeakCleanable and SoftCleanable. b/198792576
    // final WeakCleanable<?> weakCleanableList;

    // Android-removed: WeakCleanable and SoftCleanable. b/198792576
    // final SoftCleanable<?> softCleanableList;

    // The ReferenceQueue of pending cleaning actions
    final ReferenceQueue<Object> queue;

    /**
     * Called by Cleaner static initialization to provide the function
     * to map from Cleaner to CleanerImpl.
     * @param access a function to map from Cleaner to CleanerImpl
     */
    public static void setCleanerImplAccess(Function<Cleaner, CleanerImpl> access) {
        if (cleanerImplAccess == null) {
            cleanerImplAccess = access;
        } else {
            throw new InternalError("cleanerImplAccess");
        }
    }

    /**
     * Called to get the CleanerImpl for a Cleaner.
     * @param cleaner the cleaner
     * @return the corresponding CleanerImpl
     */
    static CleanerImpl getCleanerImpl(Cleaner cleaner) {
        return cleanerImplAccess.apply(cleaner);
    }

    /**
     * Constructor for CleanerImpl.
     */
    public CleanerImpl() {
        queue = new ReferenceQueue<>();
        phantomCleanableList = new PhantomCleanableRef();
        // Android-removed: WeakCleanable and SoftCleanable. b/198792576
        // weakCleanableList = new WeakCleanableRef();
        // softCleanableList = new SoftCleanableRef();
    }

    /**
     * @hide
     */
    public CleanerImpl(ReferenceQueue<Object> queue) {
        this.queue = queue;
        this.phantomCleanableList = new PhantomCleanableRef();
    }

    /**
     * Starts the Cleaner implementation.
     * Ensure this is the CleanerImpl for the Cleaner.
     * When started waits for Cleanables to be queued.
     * @param cleaner the cleaner
     * @param threadFactory the thread factory
     */
    public void start(Cleaner cleaner, ThreadFactory threadFactory) {
        if (getCleanerImpl(cleaner) != this) {
            throw new AssertionError("wrong cleaner");
        }
        // schedule a nop cleaning action for the cleaner, so the associated thread
        // will continue to run at least until the cleaner is reclaimable.
        new CleanerCleanable(cleaner);

        if (threadFactory == null) {
            threadFactory = InnocuousThreadFactory.factory();
        }

        // now that there's at least one cleaning action, for the cleaner,
        // we can start the associated thread, which runs until
        // all cleaning actions have been run.
        Thread thread = threadFactory.newThread(this);
        thread.setDaemon(true);
        thread.start();
    }

    // Android-added: start system cleaner which does not need a thread factory.
    /**
     * Starts the Cleaner implementation. Does not need a thread factory as it
     * should be used in the system cleaner only.
     * @param cleaner the cleaner
     * @hide
     */
    public void start(Cleaner cleaner) {
        start(cleaner, null);
    }

    /**
     * Process queued Cleanables as long as the cleanable lists are not empty.
     * A Cleanable is in one of the lists for each Object and for the Cleaner
     * itself.
     * Terminates when the Cleaner is no longer reachable and
     * has been cleaned and there are no more Cleanable instances
     * for which the object is reachable.
     * <p>
     * If the thread is a ManagedLocalsThread, the threadlocals
     * are erased before each cleanup
     */
    @Override
    public void run() {
        Thread t = Thread.currentThread();
        InnocuousThread mlThread = (t instanceof InnocuousThread)
                ? (InnocuousThread) t
                : null;
        while (!phantomCleanableList.isListEmpty()) {
            // Android-removed: WeakCleanable and SoftCleanable. b/198792576
            //     !weakCleanableList.isListEmpty() ||
            //     !softCleanableList.isListEmpty()) {
            if (mlThread != null) {
                // Clear the thread locals
                mlThread.eraseThreadLocals();
            }
            try {
                // Wait for a Ref, with a timeout to avoid getting hung
                // due to a race with clear/clean
                Cleanable ref = (Cleanable) queue.remove(60 * 1000L);
                if (ref != null) {
                    ref.clean();
                }
            } catch (Throwable e) {
                // ignore exceptions from the cleanup action
                // (including interruption of cleanup thread)
            }
        }
    }

    /**
     * Perform cleaning on an unreachable PhantomReference.
     */
    public static final class PhantomCleanableRef extends PhantomCleanable<Object> {
        private final Runnable action;

        /**
         * Constructor for a phantom cleanable reference.
         * @param obj the object to monitor
         * @param cleaner the cleaner
         * @param action the action Runnable
         */
        public PhantomCleanableRef(Object obj, Cleaner cleaner, Runnable action) {
            super(obj, cleaner);
            this.action = action;
        }

        /**
         * Constructor used only for root of phantom cleanable list.
         */
        PhantomCleanableRef() {
            super();
            this.action = null;
        }

        @Override
        protected void performCleanup() {
            action.run();
        }

        /**
         * Prevent access to referent even when it is still alive.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public Object get() {
            throw new UnsupportedOperationException("get");
        }

        /**
         * Direct clearing of the referent is not supported.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }
    }

    // BEGIN Android-removed: WeakCleanable and SoftCleanable. b/198792576
    /*
     * Perform cleaning on an unreachable WeakReference.
     *
    public static final class WeakCleanableRef extends WeakCleanable<Object> {
        private final Runnable action;

        /**
         * Constructor for a weak cleanable reference.
         * @param obj the object to monitor
         * @param cleaner the cleaner
         * @param action the action Runnable
         *
        WeakCleanableRef(Object obj, Cleaner cleaner, Runnable action) {
            super(obj, cleaner);
            this.action = action;
        }

        /**
         * Constructor used only for root of weak cleanable list.
         *
        WeakCleanableRef() {
            super();
            this.action = null;
        }

        @Override
        protected void performCleanup() {
            action.run();
        }

        /**
         * Prevent access to referent even when it is still alive.
         *
         * @throws UnsupportedOperationException always
         *
        @Override
        public Object get() {
            throw new UnsupportedOperationException("get");
        }

        /**
         * Direct clearing of the referent is not supported.
         *
         * @throws UnsupportedOperationException always
         *
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }
    }

    /**
     * Perform cleaning on an unreachable SoftReference.
     *
    public static final class SoftCleanableRef extends SoftCleanable<Object> {
        private final Runnable action;

        /**
         * Constructor for a soft cleanable reference.
         * @param obj the object to monitor
         * @param cleaner the cleaner
         * @param action the action Runnable
         *
        SoftCleanableRef(Object obj, Cleaner cleaner, Runnable action) {
            super(obj, cleaner);
            this.action = action;
        }

        /**
         * Constructor used only for root of soft cleanable list.
         *
        SoftCleanableRef() {
            super();
            this.action = null;
        }

        @Override
        protected void performCleanup() {
            action.run();
        }

        /**
         * Prevent access to referent even when it is still alive.
         *
         * @throws UnsupportedOperationException always
         *
        @Override
        public Object get() {
            throw new UnsupportedOperationException("get");
        }

        /**
         * Direct clearing of the referent is not supported.
         *
         * @throws UnsupportedOperationException always
         *
        @Override
        public void clear() {
            throw new UnsupportedOperationException("clear");
        }

    }
    */
    // END Android-removed: WeakCleanable and SoftCleanable. b/198792576

    /**
     * A ThreadFactory for InnocuousThreads.
     * The factory is a singleton.
     */
    static final class InnocuousThreadFactory implements ThreadFactory {
        final static ThreadFactory factory = new InnocuousThreadFactory();

        static ThreadFactory factory() {
            return factory;
        }

        final AtomicInteger cleanerThreadNumber = new AtomicInteger();

        public Thread newThread(Runnable r) {
            Thread t = InnocuousThread.newThread(r);
            t.setPriority(Thread.MAX_PRIORITY - 2);
            t.setName("Cleaner-" + cleanerThreadNumber.getAndIncrement());
            return t;
        }
    }

    /**
     * A PhantomCleanable implementation for tracking the Cleaner itself.
     */
    static final class CleanerCleanable extends PhantomCleanable<Cleaner> {
        CleanerCleanable(Cleaner cleaner) {
            super(cleaner, cleaner);
        }

        @Override
        protected void performCleanup() {
            // no action
        }
    }
}
