package qdox;

import java.io.File;

import org.junit.Test;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

public class QDoxTest {

  @Test
  public void testType() {
    JavaProjectBuilder builder = new JavaProjectBuilder();
    builder.addSourceFolder(new File("src/test"));
    JavaClass deeply = builder.getClassByName("de.sormuras.compayler.Apis$Nested$Deeply");
    System.out.println(deeply.getSource().getURL());
    System.out.println(deeply);
    System.out.println(deeply.getFullyQualifiedName());
    System.out.println(deeply.getPackageName());
    System.out.println(deeply.getCodeBlock());
    for (JavaMethod m : deeply.getMethods()) {
      System.out.println("");
      System.out.println(m);
      for (JavaParameter p : m.getParameters()) {
        System.out.println(p.getType().getGenericFullyQualifiedName() + (p.isVarArgs() ? "..." : "") + " " + p.getJavaClass().isArray());
      }
    }
  }

}
