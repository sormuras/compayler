package org.prevayler.contrib.compayler;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;
import org.prevayler.contrib.compayler.Compayler.ExecutionMode;
import org.prevayler.contrib.compayler.Compayler.ExecutionTime;
import org.prevayler.contrib.compayler.Unit.Parameter;

public class Processor extends AbstractProcessor {

  class E {

    ExecutableElement hider;
    ExecutableElement hidden;

    E(ExecutableElement hider, ExecutableElement hidden) {
      this.hider = hider;
      this.hidden = hidden;
    }

  }

  private Elements elements;
  private StringBuilder message;
  private Types types;

  protected String binary(TypeElement element) {
    return elements.getBinaryName(element).toString();
  }

  protected String binary(TypeMirror mirror) {
    try {
      TypeElement element = (TypeElement) types.asElement(mirror);
      if (element != null)
        return binary(element);
    } catch (ClassCastException e) {
      // ignore
    }
    return mirror.toString();
  }

  protected String canonical(TypeElement element) {
    return element.getQualifiedName().toString();
  }

  protected String canonical(TypeMirror mirror) {
    try {
      TypeElement element = (TypeElement) types.asElement(mirror);
      if (element != null)
        return canonical(element);
    } catch (ClassCastException e) {
      // ignore
    }
    return mirror.toString();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(Decorate.class.getCanonicalName(), Execute.class.getCanonicalName()));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elements = processingEnv.getElementUtils();
    types = processingEnv.getTypeUtils();
    message = new StringBuilder();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver())
      return false;

    message.append("Requested procession for ");
    annotations.forEach(a -> message.append('@').append(a.getSimpleName()).append(' '));
    message.append('\n');

    for (Element decorated : roundEnv.getElementsAnnotatedWith(Decorate.class)) {
      if (!decorated.getKind().isInterface())
        throw new IllegalStateException(Decorate.class + " expects an interface as target!");

      Decorate decorate = decorated.getAnnotation(Decorate.class);
      message.append("Processing interface ").append(decorated).append(" using ").append(decorate).append('\n');
      processInterface((TypeElement) decorated, decorate);

      message.append('\n');
      processingEnv.getMessager().printMessage(Kind.NOTE, message.toString());
    }

    return true;

  }

  protected boolean hides(ExecutableElement hider, ExecutableElement hidden, TypeElement type) {
    return elements.hides(hider, hidden) || elements.overrides(hider, hidden, type);
  }

  protected void processInterface(TypeElement type, Decorate decorate) {
    // find all declared and inherited methods and remove all hidden/overridden ones
    List<ExecutableElement> methods = scan(type);
    methods.removeAll(methods.stream().flatMap(m1 -> methods.stream().filter(m2 -> hides(m1, m2, type))).collect(toList()));

    // methods.forEach(System.out::println);

    Map<ExecutionMode, Matcher> matcher = new EnumMap<>(ExecutionMode.class);
    matcher.put(ExecutionMode.TRANSACTION, Pattern.compile(decorate.transactionRegex()).matcher(""));
    matcher.put(ExecutionMode.QUERY, Pattern.compile(decorate.queryRegex()).matcher(""));
    matcher.put(ExecutionMode.DIRECT, Pattern.compile(decorate.directRegex()).matcher(""));

    List<Unit> units = methods.stream().map(method -> processMethod(type, method, matcher)).collect(toList());

    units.forEach(unit -> message.append(unit).append("\n"));

    // Compayler compayler = new Compayler(new Type(binary(type)));
    // compayler.setUnits(units);
    // try {
    // writeDecorator(compayler);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
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

  private Unit processMethod(TypeElement type, ExecutableElement method, Map<ExecutionMode, Matcher> matcher) {

    Unit unit = new Unit();
    unit.setName(method.getSimpleName().toString());
    unit.setReturns(method.getReturnType().toString());
    unit.setDefaults(method.isDefault());
    unit.setVarargs(method.isVarArgs());
    unit.setChainable(types.isAssignable(type.asType(), method.getReturnType()));
   
    for (ExecutionMode mode : Arrays.asList(ExecutionMode.TRANSACTION, ExecutionMode.QUERY, ExecutionMode.DIRECT))
      if (matcher.get(mode).reset(unit.getName()).matches())
        unit.setMode(mode);

    Execute execute = method.getAnnotation(Execute.class);
    if (execute != null) {
      unit.setMode(execute.value());
      unit.setSerialVersionUID(execute.serialVersionUID());
    }

    method.getThrownTypes().forEach(thrown -> unit.getThrowns().add(thrown.toString()));

    for (VariableElement variable : method.getParameters()) {
      Parameter parameter = unit.createParameter();
      parameter.setName(variable.getSimpleName().toString());
      parameter.setType(variable.asType().toString());
      parameter.setTime(variable.getAnnotation(ExecutionTime.class) != null);
    }

    return unit;
  }

  // private void writeDecorator(Compayler compayler) throws IOException {
  // Generator generator = new Generator(compayler);
  // Source source = generator.generateSource();
  //
  // JavaFileObject jfo = processingEnv.getFiler().createSourceFile(compayler.getDecoratorType().getCanonicalName());
  //
  // try (BufferedWriter bw = new BufferedWriter(jfo.openWriter())) {
  // for (String line : source.getCode()) {
  // bw.write(line);
  // bw.newLine();
  // }
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // }

}
