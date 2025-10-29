import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import mapreduce.Reducer;

/** Implementation of the reduce phase. */
public class ReducePhase<K extends Comparable<K>, V> implements Runnable {

    public ReducePhase(Iterator<Entry<K, ArrayList<V>>> it,
            Reducer<K, V> reducer, String dstDir) {
        this.it = it;
        this.reducer = reducer;
        this.dstDir = dstDir;
    }

    @Override
    public void run() {
        System.out.println("Starting reduce phase");
        final int numOutputShards = 3;
        ExecutorService executor = Executors.newFixedThreadPool(
                numOutputShards);
        for (int i = 0; i < numOutputShards; i++) {
            final int index = i;
            executor.submit(() -> {
                File f = new File(this.dstDir, "out" + index + ".txt");
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(f);
                    ReduceConsumer consumer = new ReduceConsumer(writer);
                    while (true) {
                        Entry<K, ArrayList<V>> entry = null;
                        synchronized (this) {
                            if (this.it.hasNext()) {
                                entry = this.it.next();
                            }
                        }
                        if (entry == null) {
                            System.out.println("Reduce done!");
                            break;
                        }
                        reducer.reduce(entry.getKey(), entry.getValue(), consumer);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ReduceConsumer implements Consumer<String> {

        ReduceConsumer(PrintWriter writer) {
            this.writer = writer;
        }

        @Override
        public void accept(String out) {
            writer.println(out);
        }

        private final PrintWriter writer;
    }

    private final Iterator<Entry<K, ArrayList<V>>> it;
    private final Reducer<K, V> reducer;
    private final String dstDir;
}
