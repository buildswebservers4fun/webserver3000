package server;

/**
 * Created by CJ on 2/8/2017.
 */
public class CachedItem<T> {
    private long createdTime;
    private T item;

    public CachedItem(long time, T item) {
        this.createdTime = time;
        this.item = item;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public T getItem() {
        return item;
    }
}
