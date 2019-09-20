package me.tr.survival.main.util.callback;

@FunctionalInterface
public interface TypedCallback<T> {

    public void execute(T type);

}
