package mapreduce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import mapreduce.impl.MapPhase;
import mapreduce.impl.ReducePhase;

public class Runner<K extends Comparable<K>, V> implements Runnable {

    public Runner(Spec<K, V> spec) {
        this.spec = spec;
    }

    @Override
    public void run() {
        MapPhase<K, V> mapPhase = new MapPhase<>(
                spec.getSrcDir(), spec.getMapper());
        Iterator<Entry<K, ArrayList<V>>> it;
        try {
            it = mapPhase.call();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ReducePhase<K, V> reducePhase = new ReducePhase<>(
                it, spec.getReducer(), spec.getDstDir(), spec.getNumOutputShards());
        reducePhase.run();
    }

    private final Spec<K, V> spec;
}
