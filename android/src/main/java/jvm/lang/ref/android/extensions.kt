package jvm.lang.ref.android

import java.lang.ref.Reference
import kotlin.reflect.KProperty

operator fun <T> Reference<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = get()