package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Util.index;
import static org.prevayler.contrib.compayler.Util.name;
import static org.prevayler.contrib.compayler.Util.wrap;

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

/**
 * Method information & description bean.
 * 
 * @author Christian Stein
 */
public class Tag extends MethodDescriptor {

  public static final String TYPE_ARGS = "TYPE_ARGS";

  private List<String> customDecoratorMethodImplementation;
  private boolean direct;
  private final int indexOfPrevalentDate;
  private final Method method;
  private long serialVersionUID;
  private PrevalentType type;
  private final boolean unique;

  public Tag(Method method, boolean unique) {
    super(method, new ParameterDescriptor[method.getParameterTypes().length]);
    this.method = method;
    this.unique = unique;
    this.indexOfPrevalentDate = index(method, PrevalentDate.class);
    setDirect(false);
    setType(PrevalentType.TRANSACTION);
    setSerialVersionUID(1L);
    setValue(TYPE_ARGS, "");
    for (int i = 0; i < getParameterDescriptors().length; i++) {
      getParameterDescriptors()[i] = new ParameterDescriptor();
      getParameterDescriptors()[i].setName("p" + i);
      getParameterDescriptors()[i].setValue(TYPE_ARGS, "");
    }
  }

  /**
   * Simple class name based on the method name and parameter types.
   * 
   * Examples:
   * <ul>
   * <li> {@code Run} for {@code java.lang.Runnable.run()}
   * <li> {@code Append$1TF86} for {@code java.lang.Appendable.append(char)}
   * <li> {@code Append$HKFHPX} for {@code java.lang.Appendable.append(java.lang.CharSequence)}
   * </ul>
   * 
   * @return simple class name based on the method name and parameter types
   */
  public String getClassName() {
    String name = method.getName().toUpperCase().charAt(0) + method.getName().substring(1);
    name = name + type.name().charAt(0) + type.name().substring(1).toLowerCase(); // Query | Transaction
    if (isUnique()) {
      return name;
    }
    // append hash for parameter types
    long hash = 0L;
    for (Class<?> type : method.getParameterTypes()) {
      hash = 37 * hash + type.getCanonicalName().hashCode();
    }
    hash = Math.abs(hash);
    return name + "_" + Long.toString(hash, Character.MAX_RADIX).toUpperCase();
  }

  public List<String> getCustomDecoratorMethodImplementation() {
    return customDecoratorMethodImplementation;
  }

  public Class<?> getExecutableClass() {
    if (type == PrevalentType.QUERY)
      return Query.class;
    if (method.getReturnType() == Void.TYPE)
      return Transaction.class;
    if (method.getExceptionTypes().length == 0)
      return SureTransactionWithQuery.class;
    return TransactionWithQuery.class;
  }

  public String getExecutableDeclaration(Configuration<?, ?> configuration) {
    String interfaceName = name(configuration.getPrevalentInterface()) + configuration.getPrevalentInterfaceTypeArguments();
    String executableName = getExecutableName();
    // Transaction only has 1 generic type parameter
    if (executableName == Transaction.class.getCanonicalName())
      return executableName + "<" + interfaceName + ">";
    // Query, TransactionWithQuery and SureTransactionWithQuery all have 2 generic type parameters
    return executableName + "<" + interfaceName + ", " + getReturnTypeWrapName() + ">";
  }

  public String getExecutableName() {
    return getExecutableClass().getCanonicalName();
  }

  public int getIndexOfPrevalentDate() {
    return indexOfPrevalentDate;
  }

  public Method getMethod() {
    return method;
  }

  public String getMethodDeclaration() {
    StringBuilder builder = new StringBuilder();
    builder.append(name(method.getReturnType()) + getValue(TYPE_ARGS));
    builder.append(" ");
    builder.append(method.getName());
    builder.append(getParameterSignature());
    if (method.getExceptionTypes().length == 0)
      return builder.toString();

    builder.append(" throws");
    builder.append(getMethodExceptionTypes(","));
    return builder.toString();
  }

  public String getMethodExceptionTypes(String separator) {
    StringBuilder builder = new StringBuilder();
    boolean comma = false;
    for (Class<?> etype : method.getExceptionTypes()) {
      if (comma) {
        builder.append(separator);
      }
      builder.append(" ");
      builder.append(name(etype));
      comma = true;
    }
    return builder.toString();
  }

  public String getParameterName(int index) {
    return getParameterDescriptors()[index].getName();
  }

  /**
   * <pre>
   * 0 = "()"
   * 1 = "(p0)"
   * 2 = "(p0, p1)"
   * n = "(p0, ..., pn-1)"
   * </pre>
   * 
   * @return {@code "(p0, p1, ... pn-1)"}
   */
  public String getParameterParentheses() {
    int length = method.getParameterTypes().length;
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + getParameterName(0) + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(getParameterName(0));
    for (int i = 1; i < length; i++) {
      builder.append(", ").append(getParameterName(i));
    }
    builder.append(")");
    return builder.toString();
  }

  /**
   * @return {@code "(p0, executionTime, ... pn-1)"}
   */
  public String getParameterParenthesesWithExecutionTime() {
    return getParameterParentheses().replace("p" + indexOfPrevalentDate, "executionTime");
  }

  /**
   * @return {@code "(Type p0, String p1, ... Object pn-1)"}
   */
  public String getParameterSignature() {
    int length = method.getParameterTypes().length;
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    int index = 0;
    for (Class<?> param : method.getParameterTypes()) {
      if (index > 0)
        builder.append(", ");
      String ptype = name(param) + getParameterDescriptors()[index].getValue(TYPE_ARGS);
      if (method.isVarArgs() && index == length - 1)
        ptype = name(param.getComponentType()) + getParameterDescriptors()[index].getValue(TYPE_ARGS) + "...";
      builder.append(ptype);
      builder.append(" ").append(getParameterName(index++));
    }
    builder.append(")");
    return builder.toString();
  }

  public String getReturnTypeName() {
    return name(method.getReturnType()) + getValue(TYPE_ARGS);
  }

  public String getReturnTypeWrapName() {
    return name(wrap(method.getReturnType())) + getValue(TYPE_ARGS);
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }

  public PrevalentType getType() {
    return type;
  }

  public boolean isDirect() {
    return direct;
  }

  public boolean isUnique() {
    return unique;
  }

  public Tag setCustomDecoratorMethodImplementation(List<String> customDecoratorMethodImplementation) {
    this.customDecoratorMethodImplementation = customDecoratorMethodImplementation;
    return this;
  }

  public Tag setDirect(boolean direct) {
    this.direct = direct;
    return this;
  }

  public Tag setParameterNames(String... names) {
    for (int i = 0; i < names.length; i++) {
      getParameterDescriptors()[i].setName(names[i]);
    }
    return this;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

  public Tag setType(PrevalentType type) {
    this.type = type;
    return this;
  }

}