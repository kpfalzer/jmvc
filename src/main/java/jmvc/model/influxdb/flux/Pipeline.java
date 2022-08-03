package jmvc.model.influxdb.flux;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class Pipeline extends LinkedList<Operator> {
    public String toString() {
        return stream().map(op -> op.toString()).collect(Collectors.joining("\n|> "));
    }
}
