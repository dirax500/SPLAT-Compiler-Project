package splat.parser.elements;

public class Type {
    public static final Type Int = new Type("Integer");
    public static final Type Bool = new Type("Boolean");
    public static final Type Str = new Type("String");
    public static final Type Void = new Type("void");

    private String type;

    public Type(String type) {this.type = type;}

    public String getType() {
        return type;
    }
}
