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
        Spec<String, String> spec = new Spec<>("java/src/examples/data",
                mapper, reducer, "java/src/examples/grep");
        Runner<String, String> runner = new Runner<>(spec);
        runner.run();
    }
}
