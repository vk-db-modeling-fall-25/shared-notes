package mapreduce.examples.grep;

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
        Mapper<String, String> mapper = (String line, Consumer<Entry<String, String>> consumer) -> {
            for (String target : new String[] { "rose", "cauldron", "wherefor" }) {
                if (line.contains(target)) {
                    consumer.accept(Map.entry(target, line));
                }
            }
        };
        Reducer<String, String> reducer = (String key, Collection<String> values, Consumer<String> consumer) -> {
            for (String v : values) {
                consumer.accept(key + ": " + v);
            }
        };
        String srcDir = "src/mapreduce/examples/data";
        String dstDir = "src/mapreduce/examples/grep/output";
        int numOutputShards = 3;
        Spec<String, String> spec = new Spec<>(srcDir, mapper, reducer, dstDir, numOutputShards);
        Runner<String, String> runner = new Runner<>(spec);
        runner.run();
    }
}
