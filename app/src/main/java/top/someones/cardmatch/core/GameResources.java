package top.someones.cardmatch.core;

public interface GameResources<T> {
    T getFrontResource();
    T getBackResources(int index);
    String getGameName();
    int size();
}
