package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

import com.github.sormuras.compayler.Compayler.Configuration;
import com.github.sormuras.compayler.Compayler.Kind;
import com.github.sormuras.compayler.Compayler.Mode;

public class Description {

  public static class Field {

    private int index;
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

    public boolean isTime() {
      return time;
    }

    public boolean isVariable() {
      return variable;
    }

    public void setIndex(int index) {
      this.index = index;
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
      builder.append("");
      builder.append(type);
      if (variable)
        builder.append("...");
      builder.append(" ");
      builder.append(name);
      builder.append("");
      return builder.toString();
    }

  }

  public class Generator {

    public String generateChecksum() {
      CRC32 crc32 = getConfiguration().getChecksumBuilder();
      crc32.reset();
      for (Field field : getFields()) {
        crc32.update(field.getType().getBytes());
      }
      return Long.toString(crc32.getValue(), Character.MAX_RADIX).toUpperCase();
    }

    public String generateClassName() {
      StringBuilder builder = getConfiguration().getStringBuilder();
      builder.append(getName().toUpperCase().charAt(0));
      builder.append(getName().substring(1));
      if (!isNameUnique())
        builder.append(generateChecksum());
      return builder.toString();
    }

    public String generateExceptions(String separator) {
      StringBuilder builder = getConfiguration().getStringBuilder();
      boolean comma = false;
      for (String throwable : getThrowables()) {
        if (comma) {
          builder.append(separator);
        }
        builder.append(" ");
        builder.append(throwable);
        comma = true;
      }
      return builder.toString().trim();
    }

    public String generateImplements() {
      StringBuilder builder = getConfiguration().getStringBuilder();
      Kind kind = getKind();
      builder.append(kind.getExecutableInterface().getCanonicalName());
      builder.append('<');
      builder.append(getConfiguration().getInterfaceName());
      builder.append(Compayler.merge(Arrays.asList(getConfiguration().getInterfaceTypeVariables())));
      if (kind != Kind.TRANSACTION) {
        assert getReturnType().equals("void");
        builder.append(',').append(' ').append(Compayler.wrap(getReturnType()));
      }
      builder.append('>');
      return builder.toString();
    }

    public Kind generateKind() {
      if (getMode() == Mode.QUERY)
        return Kind.QUERY;
      if (getReturnType().equals("void"))
        return Kind.TRANSACTION;
      if (getThrowables().isEmpty())
        return Kind.TRANSACTION_QUERY;
      // if nothing applies...
      return Kind.TRANSACTION_QUERY_EXCEPTION;
    }

    public String generateMethodDeclaration() {
      StringBuilder builder = new StringBuilder();
      if (!getVariable().getTypeParameters().isEmpty()) {
        builder.append("<");
        for (String var : getVariable().getTypeParameters()) {
          builder.append(var);
        }
        builder.append("> ");
      }
      builder.append(getReturnType());
      builder.append(" ");
      builder.append(getName());
      builder.append(getParameterSignature());
      if (getThrowables().isEmpty())
        return builder.toString();

      builder.append(" throws ");
      builder.append(getExceptions(","));
      return builder.toString();
    }

    public String generateParameterParentheses() {
      int length = getFields().size();
      if (length == 0)
        return "()";
      if (length == 1)
        return "(" + getFields().get(0).getName() + ")";
      StringBuilder builder = new StringBuilder();
      builder.append("(").append(getFields().get(0).getName());
      for (int index = 1; index < length; index++) {
        builder.append(", ").append(getFields().get(index).getName());
      }
      builder.append(")");
      return builder.toString();
    }

    public String generateParameterParenthesesWithExecutionTime() {
      String parantheses = getParameterParentheses();
      for (Field field : getFields()) {
        if (field.isTime())
          return parantheses.replace(field.getName(), "executionTime");
      }
      return parantheses;
    }

    public String generateParameterSignature() {
      StringBuilder builder = getConfiguration().getStringBuilder();
      builder.append('(');
      for (Field field : getFields()) {
        if (field.getIndex() > 0)
          builder.append(", ");
        if (field.isVariable())
          builder.append(Compayler.replaceLast(field.getType(), "[]", "..."));
        else
          builder.append(field.getType());
        builder.append(' ').append(field.getName());
      }
      builder.append(')');
      return builder.toString();
    }

    public String generateClassNameWithTypeVariables() {
      StringBuilder typeVarBuilder = new StringBuilder();
      typeVarBuilder.append(generateClassName());
      if (!configuration.getInterfaceTypeVariables().isEmpty() || !getVariable().getTypeParameters().isEmpty()) {
        typeVarBuilder.append(Compayler.merge(Arrays.asList(configuration.getInterfaceTypeVariables(), getVariable().getTypeParameters())));
      }
      return typeVarBuilder.toString();
    }

  }

  public static class Signature {

    private final List<Field> fields;
    private final String name;
    private final String returnType;
    private final List<String> throwables;
    private final boolean unique;

    public Signature(String name, String returnType, List<Field> fields, List<String> throwables, boolean unique) {
      if (name == null || returnType == null)
        throw new IllegalArgumentException("name and returnType must not be null");
      this.name = name;
      this.returnType = returnType;
      this.fields = Compayler.unmodifiableList(fields);
      this.throwables = Compayler.unmodifiableList(throwables);
      this.unique = unique;
    }

  }

  public static class Variable {

    private Mode mode = Mode.TRANSACTION;
    private long serialVersionUID = 0L;
    private List<String> typeParameters = new ArrayList<>();

    public Mode getMode() {
      return mode;
    }

    public long getSerialVersionUID() {
      return serialVersionUID;
    }

    public List<String> getTypeParameters() {
      return typeParameters;
    }

    public void setMode(Mode mode) {
      if (mode == null)
        throw new IllegalArgumentException("mode must not be null");
      this.mode = mode;
    }

    public void setSerialVersionUID(long serialVersionUID) {
      this.serialVersionUID = serialVersionUID;
    }

  }

  private final Configuration configuration;
  private final Generator generator;
  private final Signature signature;
  private final Variable variable;

  public Description(Configuration configuration, Signature signature) {
    this.configuration = configuration;
    this.signature = signature;
    this.variable = new Variable();
    this.generator = new Generator();
  }

  public String getClassName() {
    return generator.generateClassName();
  }

  public String getClassNameWithTypeVariables() {
    return generator.generateClassNameWithTypeVariables();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public String getExceptions(String separator) {
    return generator.generateExceptions(separator);
  }

  public List<Field> getFields() {
    return signature.fields;
  }

  public String getImplements() {
    return generator.generateImplements();
  }

  public Kind getKind() {
    return generator.generateKind();
  }

  public String getMethodDeclaration() {
    return generator.generateMethodDeclaration();
  }

  public Mode getMode() {
    return variable.mode;
  }

  public String getName() {
    return signature.name;
  }

  public String getParameterParentheses() {
    return generator.generateParameterParentheses();
  }

  public String getParameterParenthesesWithExecutionTime() {
    return generator.generateParameterParenthesesWithExecutionTime();
  }

  public String getParameterSignature() {
    return generator.generateParameterSignature();
  }

  public String getReturnType() {
    return signature.returnType;
  }

  public long getSerialVersionUID() {
    return variable.serialVersionUID;
  }

  public List<String> getThrowables() {
    return signature.throwables;
  }

  public Variable getVariable() {
    return variable;
  }

  public boolean isNameUnique() {
    return signature.unique;
  }

  @Override
  public String toString() {
    StringBuilder builder = configuration.getStringBuilder();
    builder.append("Description [");
    builder.append("name=").append(getName());
    builder.append(", className=").append(getClassName());
    builder.append(", returnType=").append(getReturnType());
    builder.append(", mode=").append(getMode());
    builder.append(", unique=").append(isNameUnique());
    builder.append(", fields=").append(getFields());
    builder.append(", throwables=").append(getThrowables());
    builder.append("]");
    return builder.toString();
  }

}
