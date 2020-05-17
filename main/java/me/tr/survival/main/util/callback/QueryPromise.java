package me.tr.survival.main.util.callback;

public interface QueryPromise<R,C> {

    void join(R resultSet, C connection);

}
