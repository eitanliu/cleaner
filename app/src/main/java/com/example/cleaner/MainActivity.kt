@file:OptIn(ExperimentalStdlibApi::class)

package com.example.cleaner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cleaner.databinding.ActivityMainBinding
import jvm.lang.ref.android.CleanerFactory
import jvm.lang.ref.android.getValue
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.btnCreate.setOnClickListener {
            TestCleaner()
        }
        binding.btnClean.setOnClickListener {
            Runtime.getRuntime().gc()
        }
    }
}


val hexFormat = HexFormat {
    number { removeLeadingZeros = true }
}

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