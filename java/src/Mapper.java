import java.util.Map.Entry;
import java.util.function.Consumer;

/** An interface that defines the Map function. */
public interface Mapper<K extends Comparable<K>, V> {
    /**
     * Maps a 'record' from the input data into zero or more key-value pairs.
     * Invokes 'consumer' for every emitted pair.
     * The MR framework invokes this method for every record in the input data.
     */
    void map(String record, Consumer<Entry<K, V>> consumer);
}