package jmvc.model.influxdb.flux;

import com.influxdb.exceptions.NotImplementedException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Encapsulate flux operation such that Operation can be chained.
 */
public class Operator {
    protected Operator(String format, Object... args) {
        __op = String.format(format, args);
    }

    public Operator range(String start) {
        return factory("range(start:%s)", start);
    }

    public Operator range(String start, String stop) {
        return factory("range(start:%s,stop:%s)", start, stop);
    }

    public Operator filter(String predicate) {
        return factory("filter(fn:(r)=>%s)", predicate);
    }

    public Operator unique(String column) {
        return factory("unique(column:\"%s\")", column);
    }

    public Operator keep(String... columns) {
        final String cols = Arrays.stream(columns)
                .map(c -> String.format("\"%s\"", c))
                .collect(Collectors.joining(","));
        return factory("keep(columns:[%s])", cols);
    }

    protected Operator factory(String format, Object... args) {
        final Operator op = new Operator(format, args);
        return chain(op);
    }

    public String toString() {
        return __op;
    }

    protected Operator chain(Operator op) {
        return this;
    }

    private final String __op;
}
