import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;
import java.util.function.Consumer;

/** Implementation of the map phase. */
public class MapPhase<K extends Comparable<K>, V>
        implements Callable<Iterator<Entry<K, ArrayList<V>>>> {

    public MapPhase(String srcDir, Mapper<K, V> mapper) {
        this.srcDir = srcDir;
        this.mapper = mapper;
    }

    /**
     * Return an iterator over the mapped and shuffled keys.
     */
    @Override
    public Iterator<Entry<K, ArrayList<V>>> call() throws Exception {
        // List files in source directory.
        File dir = new File(srcDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("There are no input files to process");
            return new Iterator<Map.Entry<K, ArrayList<V>>>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Entry<K, ArrayList<V>> next() {
                    throw new IllegalStateException("Nothing left");
                }

            };
        }

        // Map.
        ExecutorService executor = Executors.newFixedThreadPool(files.length);
        ArrayList<TreeMap<K, ArrayList<V>>> multimaps = new ArrayList<>(files.length);
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            TreeMap<K, ArrayList<V>> multimap = new TreeMap<>();
            multimaps.add(multimap);
            final MapConsumer mapConsumer = new MapConsumer(multimap);
            executor.submit(() -> {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    System.out.println("Mapping " + file.getPath());
                    String line = br.readLine();
                    while (line != null) {
                        this.mapper.map(line, mapConsumer);
                        line = br.readLine();
                    }
                    System.out.println("Done mapping " + file.getPath());
                } catch (FileNotFoundException e) {
                    System.err.println(file.getPath() + " not found");
                    return;
                } catch (IOException e) {
                    System.err.println("Error reading line from " +
                            file.getPath());
                    return;
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            System.err.println("Error closing " +
                                    file.getPath());
                        }
                    }
                }
            });
        }

        // Wait for all the mapping to be done.
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        return new MappedKeyIterator(multimaps);
    }

    private class MappedKeyIterator implements Iterator<Entry<K, ArrayList<V>>> {

        MappedKeyIterator(ArrayList<TreeMap<K, ArrayList<V>>> multimaps) {
            this.multimaps = multimaps;
        }

        @Override
        public boolean hasNext() {
            this.minKey = nextKey();
            return this.minKey != null;
        }

        @Override
        public Entry<K, ArrayList<V>> next() {
            ArrayList<V> values = new ArrayList<>();
            for (TreeMap<K, ArrayList<V>> multimap : multimaps) {
                if (multimap.isEmpty()) {
                    continue;
                }
                K key = multimap.firstKey();
                if (key.compareTo(minKey) == 0) {
                    values.addAll(multimap.remove(key));
                }
            }
            return Map.entry(minKey, values);
        }

        private K nextKey() {
            K minKey = null;
            for (TreeMap<K, ArrayList<V>> multimap : multimaps) {
                if (multimap.isEmpty()) {
                    continue;
                }
                K key = multimap.firstKey();
                if (minKey == null || key.compareTo(minKey) < 0) {
                    minKey = key;
                }
            }
            return minKey;
        }

        private final ArrayList<TreeMap<K, ArrayList<V>>> multimaps;
        private K minKey;
    }

    private class MapConsumer implements Consumer<Entry<K, V>> {

        MapConsumer(TreeMap<K, ArrayList<V>> multimap) {
            this.multimap = multimap;
        }

        @Override
        public void accept(Entry<K, V> e) {
            K key = e.getKey();
            V value = e.getValue();
            ArrayList<V> list = multimap.get(key);
            if (list == null) {
                list = new ArrayList<>();
                multimap.put(key, list);
            }
            list.add(value);
        }

        private final TreeMap<K, ArrayList<V>> multimap;
    }

    private final String srcDir;
    private final Mapper<K, V> mapper;
}
