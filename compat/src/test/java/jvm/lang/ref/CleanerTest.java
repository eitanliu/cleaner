package jvm.lang.ref;

import org.junit.Test;

public class CleanerTest {

    @Test
    public void cleaner() {
        new TestCleaner();
        Runtime.getRuntime().gc();
    }
}
