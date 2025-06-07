package jvm.lang.ref;

public class TestCleaner {
    String identity = Integer.toHexString(System.identityHashCode(this)) + ", " + this;

    {
        System.out.println("create obj " + identity);
        CleanerFactory.cleaner().register(this, new CleanRunnable(this));
    }

    public static class CleanRunnable implements Runnable {
        final String clean;

        public CleanRunnable(String clean) {
            this.clean = clean;
        }

        public CleanRunnable(TestCleaner cleaner) {
            clean = Integer.toHexString(System.identityHashCode(cleaner)) + ", " + cleaner;
        }

        @Override
        public void run() {
            System.out.println("clean  obj " + clean);
        }
    }

}
