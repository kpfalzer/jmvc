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

    public Tag(String tagName, Object... items) {
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
                invariant((item instanceof Tag) || (item instanceof Object));
                add(item);
            }
        }
    }

    public Tag(String tagName, String val) {
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

    public static Tag div(String... items) {
        return new Tag("div", (Object[]) items);
    }

    public static Tag div(Object... items) {
        return new Tag("div", items);
    }

    public static Tag th(String... items) {
        return new Tag("th", (Object[]) items);
    }

    public static Tag th(Object... items) {
        return new Tag("th", items);
    }

    public static Tag tr(String... items) {
        return new Tag("tr", (Object[]) items);
    }

    public static Tag tr(Object... items) {
        return new Tag("tr", items);
    }

    public static Tag td(String... items) {
        return new Tag("td", (Object[]) items);
    }

    public static Tag td(Object... items) {
        return new Tag("td", items);
    }

    public static Tag tbody(String... items) {
        return new Tag("tbody", (Object[]) items);
    }

    public static Tag tbody(Object... items) {
        return new Tag("tbody", items);
    }

    public static Tag span(String... items) {
        return new Tag("span", (Object[]) items);
    }

    public static Tag span(Object... items) {
        return new Tag("span", items);
    }

    public static class ATag extends Tag {

        private ATag(String href, Object... items) {
            super("a", items);
            _href = href;
        }

        private ATag(String href, String val) {
            super("a", val);
            _href = href;
        }

        public String toString() {
            String href = String.format("<a href=\"%s\"", _href);
            return href + super.toString().substring(2);
        }

        private final String _href;
    }

    public static class Link extends Tag {
        public Link(String rel, String href) {
            super("link");
            _rel = rel;
            _href = href;
        }

        public String toString() {
            return String.format("<link rel=\"%s\" href=\"%s\">", _rel, _href);
        }

        private final String _rel, _href;
    }

    public static Tag a(String href, String... items) {
        return new ATag(href, (Object[]) items);
    }

    public static Tag a(String href, Object... items) {
        return new ATag(href, items);
    }

    public static Tag link(String rel, String href) {
        return new Link(rel, href);
    }

    public static final String NL = "\n";
}
