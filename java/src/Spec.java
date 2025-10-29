/** The specification of a mapreduce program. */
public class Spec<K extends Comparable<K>, V> {

    public Spec(String srcDir, Mapper<K, V> mapper, Reducer<K, V> reducer,
            String dstDir) {
        this.srcDir = srcDir;
        this.mapper = mapper;
        this.reducer = reducer;
        this.dstDir = dstDir;
    }

    public String getSrcDir() {
        return srcDir;
    }

    public Mapper<K, V> getMapper() {
        return mapper;
    }

    public Reducer<K, V> getReducer() {
        return reducer;
    }

    public String getDstDir() {
        return dstDir;
    }

    private final String srcDir;
    private final Mapper<K, V> mapper;
    private final Reducer<K, V> reducer;
    private final String dstDir;
}
