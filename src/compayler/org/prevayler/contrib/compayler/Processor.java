package org.prevayler.contrib.compayler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;
import org.prevayler.contrib.compayler.Compayler.ExecutionMode;
import org.prevayler.contrib.compayler.Compayler.ExecutionTime;
import org.prevayler.contrib.compayler.Unit.Parameter;

@SupportedAnnotationTypes({ "org.prevayler.contrib.compayler.Compayler.Decorate", "org.prevayler.contrib.compayler.Compayler.Execute" })
@SupportedOptions("org.prevayler.contrib.compayler.Processor.debug")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class Processor extends AbstractProcessor {

  private boolean debug;
  private Elements elements;
  private StringBuilder message;
  private Types types;

  protected boolean hides(ExecutableElement hider, ExecutableElement hidden, TypeElement type) {
    return elements.hides(hider, hidden) || elements.overrides(hider, hidden, type);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    debug = Boolean.parseBoolean(processingEnv.getOptions().get("org.prevayler.contrib.compayler.Processor.debug"));
    elements = processingEnv.getElementUtils();
    types = processingEnv.getTypeUtils();
    message = new StringBuilder();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver())
      return true;

    if (annotations.isEmpty())
      return false;

    if (debug) {
      message.append("Requested procession for ");
      annotations.forEach(annotation -> message.append('@').append(annotation.getSimpleName()).append(' '));
      processingEnv.getMessager().printMessage(Kind.NOTE, message.toString());
    }

    for (Element decorated : roundEnv.getElementsAnnotatedWith(Decorate.class)) {
      if (!decorated.getKind().isInterface())
        throw new IllegalStateException(Decorate.class + " expects an interface as target!");

      Decorate decorate = decorated.getAnnotation(Decorate.class);
      if (debug) {
        message.setLength(0);
        message.append("Processing interface ").append(decorated).append(" using ").append(decorate).append('\n');
      }
      processInterface((TypeElement) decorated, decorate);
      if (debug) {
        // message.append('\n');
        processingEnv.getMessager().printMessage(Kind.NOTE, message.toString());
      }
    }

    return true;
  }

  protected void processInterface(TypeElement type, Decorate decorate) {
    // find all declared and inherited methods and remove all hidden/overridden ones
    List<ExecutableElement> methods = scan(type);
    methods.removeAll(methods.stream().flatMap(m1 -> methods.stream().filter(m2 -> hides(m1, m2, type))).collect(toList()));

    // methods.forEach(System.out::println);

    String packageName = elements.getPackageOf(type).getQualifiedName().toString();
    Compayler compayler = new Compayler(decorate, packageName, type.getQualifiedName().toString(), type.getSimpleName().toString());
    if (debug) {
      message.append("Using Compayler ").append(compayler.getVersion()).append("\n");
    }

    List<Unit> units = methods.stream().map(method -> processMethod(type, method, compayler)).collect(toList());

    Unit.updateAllUniqueProperties(units);
    Unit.sort(units);
    if (debug) {
      units.forEach(unit -> message.append(unit).append("\n"));
    }

    try {
      writeDecorator(type, compayler, units);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected Unit processMethod(TypeElement type, ExecutableElement method, Compayler compayler) {
    Unit unit = new Unit();
    unit.setName(method.getSimpleName().toString());
    unit.setReturns(method.getReturnType().toString());
    unit.setDefaults(method.isDefault());
    unit.setVarargs(method.isVarArgs());
    unit.setChainable(types.isAssignable(type.asType(), method.getReturnType()));

    unit.setMode(ExecutionMode.forName(unit.getName(), compayler.getExecutionModeMatchers()));

    Execute execute = method.getAnnotation(Execute.class);
    if (execute != null) {
      unit.setMode(execute.value());
      unit.setSerialVersionUID(execute.serialVersionUID());
    }

    method.getThrownTypes().forEach(thrown -> unit.getThrowns().add(thrown.toString()));

    List<? extends VariableElement> parameters = method.getParameters();
    if (!parameters.isEmpty()) {
      VariableElement lastParameter = parameters.get(parameters.size() - 1);
      for (VariableElement variable : parameters) {
        Parameter parameter = unit.createParameter();
        parameter.setLast(variable == lastParameter);
        parameter.setName(variable.getSimpleName().toString());
        parameter.setType(variable.asType().toString());
        parameter.setTime(variable.getAnnotation(ExecutionTime.class) != null);
        parameter.setVars(method.isVarArgs() && variable == lastParameter);
      }
    }

    return unit;
  }

  protected List<ExecutableElement> scan(TypeElement type) {
    return scan(type, new ArrayList<>());
  }

  protected List<ExecutableElement> scan(TypeElement type, List<ExecutableElement> methods) {
    Predicate<Element> method = e -> e.getKind().equals(METHOD);
    Consumer<Element> add = e -> methods.add((ExecutableElement) e);
    Consumer<TypeMirror> scan = i -> scan((TypeElement) types.asElement(i), methods);

    type.getEnclosedElements().stream().filter(method).forEach(add);
    type.getInterfaces().forEach(scan);
    return methods;
  }

  protected void writeDecorator(TypeElement type, Compayler compayler, List<Unit> units) throws Exception {
    Generator generator = new Generator(compayler, units);

    String name = compayler.getDecoratorName();
    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(name);

    List<String> lines = generator.generateSource();
    try (BufferedWriter writer = new BufferedWriter(jfo.openWriter())) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    }
  }

}
