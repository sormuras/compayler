package sandbox;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import javax.tools.SimpleJavaFileObject;

/**
 * Java source file object implementation.
 * 
 * @author Christian Stein
 */
public class Source extends SimpleJavaFileObject {

  /**
   * To be compiled.
   */
  private List<String> linesOfCode;

  /**
   * Like "java.util"
   */
  private String packageName;

  /**
   * Like "Date"
   */
  private String simpleClassName;

  /**
   * This constructor will store the source code and register it as a source code, using a URI containing the class full name.
   * 
   * @param className
   *          name of the public class in the source code
   * @param linesOfCode
   *          source code to compile
   */
  public Source(String packageName, String simpleClassName, List<String> linesOfCode) {
    super(URI.create("string:///" + packageName.replace('.', '/') + '/' + simpleClassName + Kind.SOURCE.extension), Kind.SOURCE);
    this.packageName = packageName;
    this.simpleClassName = simpleClassName;
    this.linesOfCode = linesOfCode;
  }

  /**
   * Answers the CharSequence to be compiled.
   */
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    StringBuilder builder = new StringBuilder();
    for (String line : linesOfCode) {
      builder.append(line).append(System.lineSeparator());
    }
    return builder.toString();
  }

  public List<String> getLinesOfCode() {
    return linesOfCode;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getSimpleClassName() {
    return simpleClassName;
  }

  public Charset getCharset() {
    return Charset.forName("UTF-8");
  }

}
