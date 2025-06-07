package jvm.lang.ref.internal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread that has no permissions, is not a member of any user-defined
 * ThreadGroup and supports the ability to erase ThreadLocals.
 */
public final class InnocuousThread extends Thread {
    private static final ThreadGroup INNOCUOUSTHREADGROUP;

    private static final AtomicInteger threadNumber = new AtomicInteger(1);
    private static String newName() {
        return "InnocuousThread-" + threadNumber.getAndIncrement();
    }

    /**
     * Returns a new InnocuousThread with an auto-generated thread name
     * and its context class loader is set to the system class loader.
     */
    public static Thread newThread(Runnable target) {
        return newThread(newName(), target);
    }

    /**
     * Returns a new InnocuousThread with its context class loader
     * set to the system class loader.
     */
    public static Thread newThread(String name, Runnable target) {
        return new InnocuousThread(INNOCUOUSTHREADGROUP,
                target,
                name,
                ClassLoader.getSystemClassLoader());
    }

    /**
     * Returns a new InnocuousThread with an auto-generated thread name.
     * Its context class loader is set to null.
     */
    public static Thread newSystemThread(Runnable target) {
        return newSystemThread(newName(), target);
    }

    /**
     * Returns a new InnocuousThread with null context class loader.
     */
    public static Thread newSystemThread(String name, Runnable target) {
        return new InnocuousThread(INNOCUOUSTHREADGROUP,
                target, name, null);
    }

    private InnocuousThread(ThreadGroup group, Runnable target, String name, ClassLoader tccl) {
        super(group, target, name, 0L);
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler x) {
        // silently fail
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
        // Allow clearing of the TCCL to remove the reference to the system classloader.
        if (cl == null)
            super.setContextClassLoader(null);
        else
            throw new SecurityException("setContextClassLoader");
    }

    /**
     * Drops all thread locals (and inherited thread locals).
     */
    public final void eraseThreadLocals() {
    }

    // ensure run method is run only once
    private volatile boolean hasRun;

    @Override
    public void run() {
        if (Thread.currentThread() == this && !hasRun) {
            hasRun = true;
            super.run();
        }
    }

    // Use Unsafe to access Thread group and ThreadGroup parent fields
    static {
        try {
            INNOCUOUSTHREADGROUP = new ThreadGroup("InnocuousThreadGroup");;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
