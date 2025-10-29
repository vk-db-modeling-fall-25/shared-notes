import java.util.Collection;
import java.util.function.Consumer;

/** An interface that defines the Reduce function. */
public interface Reducer<K, V> {
    /**
     * Reduces all the mapped values for a specific key into zero or more
     * strings.
     * Invokes 'consumer' for every reduced string.
     * The MR framework invokes this method for every mapped key after
     * collecting all the mapped values for each key.
     */
    void reduce(K key, Collection<V> values, Consumer<String> consumer);
}
