# JDK 9 Cleaner Compatible

### JVM
Dependency `com.github.eitanliu.cleaner:compat:1.0.0`  
User package `jvm.lang.ref`

```java
import jvm.lang.ref.CleanerFactory;

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

```

### Android

Dependency `com.github.eitanliu.cleaner:android:1.0.0`  
User package `jvm.lang.ref.android`  

```kotlin
import jvm.lang.ref.android.CleanerFactory
import jvm.lang.ref.android.registerObject

class TestCleaner {

    init {

        val create = "${System.identityHashCode(this).toHexString()}, $this"
        Log.e("Cleaner", "obj create $create")

        CleanerFactory.cleaner().registerObject(this) {
            object : Runnable {

                val clean = "${System.identityHashCode(it).toHexString()}, $it"

                override fun run() {
                    Log.e("Cleaner", "obj clean  $clean")
                }
            }
        }
    }
}

```