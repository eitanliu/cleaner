@file:OptIn(ExperimentalStdlibApi::class)

package com.example

import android.util.Log
import jvm.lang.ref.android.CleanerFactory
import jvm.lang.ref.android.getValue
import org.junit.Test
import java.lang.ref.WeakReference

class KtExampleTest {
    @Test
    fun cleaner() {
        KtTestCleaner()
        Runtime.getRuntime().gc()
    }
}

class KtTestCleaner {

    init {

        val create = "${System.identityHashCode(this).toHexString()}, $this"
        Log.e("Cleaner", "obj create $create")

        CleanerFactory.cleaner().register(this, object : Runnable {

            val obj by WeakReference(this@KtTestCleaner)
            val clean = "${System.identityHashCode(obj).toHexString()}, $obj"

            override fun run() {
                Log.e("Cleaner", "obj clean  $clean")
            }
        })
    }
}