package org.prevayler.contrib.compayler;

import static java.util.Arrays.asList;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.DIRECT;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.QUERY;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.TRANSACTION;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;
import org.prevayler.contrib.compayler.Compayler.ExecutionMode;
import org.prevayler.contrib.compayler.Compayler.ExecutionTime;

//Reflector reflector = new Reflector();
//Generator generator = new Generator(this, reflector.createUnits(this));
//Source source = new Source(getDecoratorName(), generator.generateSource());
public class Reflector {

  protected Unit createUnit(Method method, Map<ExecutionMode, Matcher> matcher) {
    Unit unit = new Unit();
    unit.setName(method.getName());
    unit.setReturns(method.getReturnType().getCanonicalName());
    unit.setDefaults(method.isDefault());
    unit.setVarargs(method.isVarArgs());

    ExecutionMode.MODES.stream().filter(mode -> matcher.get(mode).reset(unit.getName()).matches()).forEachOrdered(unit::setMode);

    Execute execute = method.getAnnotation(Execute.class);
    if (execute != null) {
      unit.setMode(execute.value());
      unit.setSerialVersionUID(execute.serialVersionUID());
    }

    asList(method.getExceptionTypes()).forEach(e -> unit.getThrowns().add(e.getCanonicalName()));

    List<Parameter> parameters = asList(method.getParameters());
    if (!parameters.isEmpty()) {
      Parameter lastParameter = parameters.get(parameters.size() - 1);
      for (Parameter parameter : parameters) {
        Unit.Parameter unitpara = unit.createParameter();
        unitpara.setName(parameter.getName());
        unitpara.setType(parameter.getType().getCanonicalName());
        unitpara.setTime(parameter.isAnnotationPresent(ExecutionTime.class));
        unitpara.setVars(method.isVarArgs() && parameter == lastParameter);
      }
    }

    return unit;
  }

  public List<Unit> createUnits(Compayler compayler) {
    Class<?> interfaceClass;
    try {
      interfaceClass = Class.forName(compayler.getInterfaceName());
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Interface class not loadable?!", e);
    }

    Decorate decorate = interfaceClass.getAnnotation(Decorate.class);
    Map<ExecutionMode, Matcher> matcher = new EnumMap<>(ExecutionMode.class);
    try {
      String transactionRegex = (String) Decorate.class.getMethod("transactionRegex").getDefaultValue();
      String queryRegex = (String) Decorate.class.getMethod("queryRegex").getDefaultValue();
      String directRegex = (String) Decorate.class.getMethod("directRegex").getDefaultValue();
      if (decorate != null) {
        transactionRegex = decorate.transactionRegex();
        queryRegex = decorate.queryRegex();
        directRegex = decorate.directRegex();
      }
      matcher.put(TRANSACTION, Pattern.compile(transactionRegex).matcher(""));
      matcher.put(QUERY, Pattern.compile(queryRegex).matcher(""));
      matcher.put(DIRECT, Pattern.compile(directRegex).matcher(""));
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Could not retrieve default value from " + Decorate.class, e);
    }

    List<Unit> units = new LinkedList<>();
    for (Method method : interfaceClass.getMethods()) {
      Unit unit = createUnit(method, matcher);
      unit.setChainable(interfaceClass.isAssignableFrom(method.getReturnType()));
      units.add(unit);
    }
    Unit.updateAllUniqueProperties(units);
    Unit.sort(units);
    return units;
  }
}
