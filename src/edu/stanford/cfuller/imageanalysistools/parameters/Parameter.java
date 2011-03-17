package edu.stanford.cfuller.imageanalysistools.parameters;

/**
 *
 * @author cfuller
 */
public class Parameter {

    public final static int TYPE_BOOLEAN = 0;
    public final static int TYPE_INTEGER = 1;
    public final static int TYPE_FLOATING = 2;
    public final static int TYPE_STRING = 3;

    String name;
    String displayName;
    int type;
    Object defaultValue;
    Object value;
    String description;

    private Parameter(){}

    public Parameter(Parameter p) {
        this.name = p.name;
        this.displayName = p.displayName;
        this.type = p.type;
        this.defaultValue = p.defaultValue;
        this.value = p.value;
        this.description = p.description;
    }

    public Parameter(String name, String displayName, int type, Object defaultValue, Object value, String description) {
        this.name = name;
        this.displayName = displayName;
        this.type= type;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
    }

    public void setValue(Object value) {
        this.value = convertValue(value);
    }

    public void setName(String name) {this.name = name;}

    public void setDisplayName(String displayName) {this.displayName = displayName;}

    public void setType(int type) {this.type = type;}

    public String getName() {return this.name;}

    public String getDisplayName() {return this.displayName;}

    public Object getValue() {return this.value;}

    public Object getDefaultValue() {return this.defaultValue;}

    public String getDescription() {return this.description;}

    public int getType() {return this.type;}

    public String toString() {return this.getDisplayName();}

    private Object convertValue(Object originalValue) {
        if (this.type == TYPE_STRING) return originalValue.toString();
        if (this.type == TYPE_BOOLEAN) return Boolean.valueOf(originalValue.toString());
        if (this.type == TYPE_FLOATING) return Double.valueOf(originalValue.toString());
        if (this.type == TYPE_INTEGER) return Integer.valueOf(originalValue.toString());
        return null;
    }

}
