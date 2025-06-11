@file:OptIn(ExperimentalStdlibApi::class)

package com.example

import jvm.lang.ref.android.CleanerFactory
import jvm.lang.ref.getValue
import org.junit.Test
import java.lang.ref.WeakReference

class ExampleTest {
    @Test
    fun cleaner() {
        TestCleaner()
        Runtime.getRuntime().gc()
    }
}


class TestCleaner {

    init {

        val create = "${System.identityHashCode(this).toHexString()}, $this"
        println("obj create $create")

        CleanerFactory.cleaner().register(this, object : Runnable {

            val obj by WeakReference(this@TestCleaner)
            val clean = "${System.identityHashCode(obj).toHexString()}, $obj"

            override fun run() {
                println("obj clean  $clean")
            }
        })
    }
}