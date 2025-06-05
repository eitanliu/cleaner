@file:OptIn(ExperimentalStdlibApi::class)

package com.example.cleaner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.cleaner.databinding.ActivityMainBinding
import java.lang.ref.android.Cleaner
import java.lang.ref.android.CleanerFactory

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

inline fun <T> cleanerRegister(
    resource: T, crossinline builder: (resource: T) -> Runnable
) {
    CleanerFactory.cleaner().registerObject(resource, builder)
}

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