package sandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaType;

import sandbox.Compayler.Configuration;
import sandbox.Compayler.DescriptionFactory;
import sandbox.Compayler.Directive;
import sandbox.Compayler.Mode;
import sandbox.Description.Field;

public class Parser implements DescriptionFactory {

  private final JavaProjectBuilder javaProjectBuilder;
  private final Configuration configuration;

  public Parser(Configuration configuration) {
    this(configuration, new JavaProjectBuilder());
  }

  public Parser(Configuration configuration, JavaProjectBuilder javaProjectBuilder) {
    this.configuration = configuration;
    this.javaProjectBuilder = javaProjectBuilder;
  }

  @Override
  public List<Description> createDescriptions() {
    JavaClass javaClass = javaProjectBuilder.getClassByName(configuration.getInterfaceName());
    if (javaClass == null)
      throw new IllegalStateException("Couldn't retrieve interface for name: " + configuration.getInterfaceName());

    List<Description> descriptions = new ArrayList<>();
    CRC32 crc32 = new CRC32();

    for (JavaMethod method : javaClass.getMethods(true)) {

      String name = method.getName();
      String returnType = method.getReturnType(true).getGenericFullyQualifiedName();
      List<String> throwables = new ArrayList<>();
      for (JavaType exception : method.getExceptionTypes()) {
        throwables.add(exception.getGenericFullyQualifiedName());
      }
      // parse parameters to fields
      crc32.reset();
      List<Field> fields = new ArrayList<>();
      int index = 0;
      for (JavaParameter parameter : method.getParameters()) {
        Field field = new Field();
        field.setIndex(index++);
        field.setName(parameter.getName());
        field.setTime(!parameter.getAnnotations().isEmpty());
        field.setType(parameter.getType().getGenericFullyQualifiedName() + (parameter.isVarArgs() ? "[]" : ""));
        field.setVariable(parameter.isVarArgs());
        fields.add(field);
        // update checksum
        crc32.update(field.getType().getBytes());
      }

      // create description
      Description description = new Description(crc32.getValue(), name, returnType, fields, throwables);
      // update mode, if possible
      for (JavaAnnotation annotation : method.getAnnotations()) {
        if (!Directive.class.getName().equals(annotation.getType().getFullyQualifiedName()))
          continue;
        Object object = annotation.getNamedParameter("value");
        if (object != null)
          description.setMode(Mode.valueOf(object.toString().substring(object.toString().lastIndexOf('.') + 1)));
      }
      // done
      descriptions.add(description);
    }

    return descriptions;
  }

  public JavaProjectBuilder getJavaProjectBuilder() {
    return javaProjectBuilder;
  }

}
