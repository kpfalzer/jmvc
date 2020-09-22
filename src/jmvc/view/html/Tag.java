package jmvc.view.html;

import gblibx.Util;

import java.util.LinkedList;
import java.util.List;

import static gblibx.Util.downcast;
import static gblibx.Util.invariant;
import static gblibx.Util.isNonNull;
import static java.util.Objects.isNull;

public class Tag {
    public static enum EAttr {
        eClass, eId
    }

    ;

    public static class Attribute extends Util.Pair<EAttr, String> {
        public Attribute(EAttr attr, String val) {
            super(attr, val);
        }

        public EAttr type() {
            return v1;
        }

        public String val() {
            return v2;
        }
    }

    public static Attribute mkClass(String val) {
        return new Attribute(EAttr.eClass, val);
    }

    public static Attribute mkId(String val) {
        return new Attribute(EAttr.eId, val);
    }

    protected Tag(String tagName, Object... items) {
        name = tagName;
        for (Object item : items) {
            if (item instanceof Attribute) {
                final Attribute attr = downcast(item);
                if (EAttr.eClass == attr.type()) {
                    appendCls(attr.val());
                } else {
                    setId(attr.val());
                }
            } else {
                invariant((item instanceof String) || (item instanceof Tag));
                add(item);
            }
        }
    }

    protected Tag(String tagName, String val) {
        this(tagName, (Object) val);
    }

    public Tag setCls(String cls) {
        _cls = cls;
        return this;
    }

    public Tag setId(String id) {
        _id = id;
        return this;
    }

    public Tag appendCls(String cls) {
        if (isNull(_cls)) {
            return setCls(cls);
        }
        _cls += " " + cls;
        return this;
    }


    public String getCls() {
        return _cls;
    }

    public String getId() {
        return _id;
    }

    public Tag add(Object item) {
        if (isNull(_items)) {
            _items = new LinkedList<>();
        }
        _items.add(item);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("<%s", name));
        if (isNonNull(_cls)) {
            sb.append(" class=\"").append(_cls).append('"');
        }
        if (isNonNull(_id)) {
            sb.append(" id=\"").append(_id).append('"');
        }
        sb.append('>');
        if (isNonNull(_items)) {
            for (Object item : _items) {
                sb.append(item.toString());
            }
        }
        sb.append("</").append(name).append('>');
        return sb.toString();
    }

    private String _cls = null;
    private String _id = null;
    protected List<Object> _items = null;
    public final String name;

    public static Tag th(String... items) {
        return new Tag("th", (Object[])items);
    }

    public static Tag th(Object... items) {
        return new Tag("th", items);
    }

    public static final String NL = "\n";
}
