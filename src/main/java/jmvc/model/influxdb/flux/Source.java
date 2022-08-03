package jmvc.model.influxdb.flux;

import static gblibx.Util.downcast;

public class Source extends Operator {
    private Source(String format, Object... args) {
        super(format, args);
    }

    public static Source from(String bucket) {
        return new Source("from(bucket:%s)", bucket);
    }

    protected Operator chain(Operator op) {
        __chain.add(op);
        return this;
    }

    private final Pipeline __chain = new Pipeline();
}
