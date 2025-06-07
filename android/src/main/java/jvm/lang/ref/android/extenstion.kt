package jvm.lang.ref.android

inline fun <T> Cleaner.registerObject(
    resource: T, crossinline builder: (resource: T) -> Runnable
) {
    register(resource, builder(resource))
}