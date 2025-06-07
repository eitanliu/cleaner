package jvm.lang.ref.internal;

@FunctionalInterface
public interface Function<T, R> {

    R apply(T t);
}
