# JDK 9 Cleaner Compatible

[![](https://jitpack.io/v/eitanliu/cleaner.svg)](https://jitpack.io/#eitanliu/cleaner)

### How to

To get a Git project into your build:  
*Step 1.* Add the JitPack repository to your build file  
Add it in your root `settings.gradle.kts` at the end of repositories:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

*Step 2.* Add the dependency

```kotlin
dependencies {
    implementation("com.github.eitanliu.cleaner:android:1.0.2")
    implementation("com.github.eitanliu.cleaner:compat:1.0.2")
}
```

### JVM
Dependency `com.github.eitanliu.cleaner:compat:1.0.2`  
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

Dependency `com.github.eitanliu.cleaner:android:1.0.2`  
User package `jvm.lang.ref.android`  

```kotlin
import jvm.lang.ref.android.CleanerFactory
import jvm.lang.ref.getValue
import java.lang.ref.WeakReference

class TestCleaner {

    init {

        val create = "${System.identityHashCode(this).toHexString()}, $this"
        Log.e("Cleaner", "obj create $create")

        CleanerFactory.cleaner().register(this, object : Runnable {

            val obj by WeakReference(this@TestCleaner)
            val clean = "${System.identityHashCode(obj).toHexString()}, $obj"

            override fun run() {
                Log.e("Cleaner", "obj clean  $clean")
            }
        })
    }
}

```