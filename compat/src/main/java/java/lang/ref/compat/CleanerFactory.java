package java.lang.ref.compat;

/**
 * CleanerFactory provides a Cleaner for use within system modules.
 * The cleaner is created on the first reference to the CleanerFactory.
 */
public final class CleanerFactory {

    /* The common Cleaner. */
    // Android-changed: objects registered in the system cleaner are cleaned
    // by the finalizer daemon thread, not in a InnocuousThread.
    /*
    private final static Cleaner commonCleaner = Cleaner.create(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return AccessController.doPrivileged(new PrivilegedAction<>() {
                @Override
                public Thread run() {
                    Thread t = InnocuousThread.newSystemThread("Common-Cleaner", r);
                    t.setPriority(Thread.MAX_PRIORITY - 2);
                    return t;
                }
            });
        }
    });
    */
    private static final Cleaner commonCleaner = Cleaner.createSystemCleaner();

    /**
     * Cleaner for use within system modules.
     *
     * This Cleaner will run on a thread whose context class loader
     * is {@code null}. The system cleaning action to perform in
     * this Cleaner should handle a {@code null} context class loader.
     *
     * @return a Cleaner for use within system modules
     */
    public static Cleaner cleaner() {
        return commonCleaner;
    }
}
