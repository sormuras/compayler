package de.sormuras.compayler;

import java.util.ArrayList;
import java.util.List;

import de.sormuras.compayler.Compayler.Mode;

/**
 * Executable class descriptor bean.
 * 
 * @author Christian Stein
 */
public class Descriptor {

  public class Field {

    private boolean first;

    private int index;

    private boolean last;

    private String name = "";

    private boolean time = false;

    private String type = "";

    private boolean variable;

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public boolean isFirst() {
      return first;
    }

    public boolean isLast() {
      return last;
    }

    public boolean isTime() {
      return time;
    }

    public boolean isVariable() {
      return variable;
    }

    public void setFirst(boolean first) {
      this.first = first;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public void setLast(boolean last) {
      this.last = last;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setTime(boolean time) {
      this.time = time;
    }

    public void setType(String type) {
      this.type = type;
    }

    public void setVariable(boolean variable) {
      this.variable = variable;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Field [");
      builder.append("name=").append(name);
      builder.append(", type=").append(type);
      builder.append(", variable=").append(variable);
      builder.append(", time=").append(time);
      builder.append("]");
      return builder.toString();
    }

  }

  private String className = "";

  private List<Field> fields = new ArrayList<>();

  private Mode mode = Mode.TRANSACTION;

  private final String name;

  private String packageName = "";

  private String returnType = "void";

  private long serialVersionUID;
  
  private List<String> throwables = new ArrayList<>();
  
  private boolean unique = true;

  public Descriptor(String name) {
    this.name = name;
  }

  public Field addField(String name) {
    Field field = new Field();
    field.setName(name);
    getFields().add(field);
    return field;
  }

  public String getClassName() {
    return className;
  }

  public List<Field> getFields() {
    return fields;
  }

  public Mode getMode() {
    return mode;
  }

  public String getName() {
    return name;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getReturnType() {
    return returnType;
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }

  public List<String> getThrowables() {
    return throwables;
  }

  public boolean hasFields() {
    return !fields.isEmpty();
  }

  public boolean isUnique() {
    return unique;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

  public void setThrowables(List<String> throwables) {
    this.throwables = throwables;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Descriptor [name=");
    builder.append(name);
    builder.append(", className=");
    builder.append(className);
    builder.append(", mode=");
    builder.append(mode);
    builder.append(", packageName=");
    builder.append(packageName);
    builder.append(", returnType=");
    builder.append(returnType);
    builder.append(", unique=");
    builder.append(unique);
    builder.append(", fields=");
    builder.append(fields);
    builder.append(", throwables=");
    builder.append(throwables);
    builder.append("]");
    return builder.toString();
  }

}
