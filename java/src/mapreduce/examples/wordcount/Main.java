package mapreduce.examples.wordcount;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import mapreduce.Mapper;
import mapreduce.Reducer;
import mapreduce.Runner;
import mapreduce.Spec;

public class Main {

    public static void main(String[] args) {
        Mapper<String, Integer> mapper = (String line, Consumer<Entry<String, Integer>> consumer) -> {
            for (String word : line.split(" ")) {
                word = word.toLowerCase();
                consumer.accept(Map.entry(word, 1));
            }
        };
        Reducer<String, Integer> reducer = (String key, Collection<Integer> values, Consumer<String> consumer) -> {
            int sum = 0;
            for (int v : values) {
                sum += v;
            }
            consumer.accept(key + ": " + sum);
        };
        String srcDir = "src/mapreduce/examples/data";
        String dstDir = "src/mapreduce/examples/wordcount/output";
        int numOutputShards = 3;
        Spec<String, Integer> spec = new Spec<>(srcDir, mapper, reducer, dstDir, numOutputShards);
        Runner<String, Integer> runner = new Runner<>(spec);
        runner.run();
    }
}
