package java.lang.ref.compat.internal;

@FunctionalInterface
public interface Function<T, R> {

    R apply(T t);
}
