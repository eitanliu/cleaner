# JDK 9 Cleaner Compatible

### JVM
Dependency `com.github.eitanliu.cleaner:compat:1.0.0`  
User package `java.lang.ref.compat`

### Android

Dependency `com.github.eitanliu.cleaner:android:1.0.0`  
User package `java.lang.ref.android`  

```kotlin
import java.lang.ref.android.Cleaner
import java.lang.ref.android.CleanerFactory

inline fun <T> Cleaner.registerObject(
    resource: T, crossinline builder: (resource: T) -> Runnable
) {
    register(resource, builder(resource))
}

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


binding.btnCreate.setOnClickListener {
    TestCleaner()
}
binding.btnClean.setOnClickListener {
    Runtime.getRuntime().gc()
}
```