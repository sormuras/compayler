package org.prevayler.contrib.compayler.service;

import static org.prevayler.contrib.compayler.Util.merge;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.CRC32;

import org.prevayler.contrib.compayler.Generator;
import org.prevayler.contrib.compayler.Param;
import org.prevayler.contrib.compayler.Repository;
import org.prevayler.contrib.compayler.Unit;

public class DefaultGenerator implements Generator {

  @Override
  public String generateChecksum(Unit unit) {
    CRC32 crc32 = new CRC32();
    crc32.reset();
    for (Param param : unit.getParams()) {
      crc32.update(param.getType().getBinaryName().getBytes());
    }
    return Long.toString(crc32.getValue(), Character.MAX_RADIX).toUpperCase();
  }

  @Override
  public String generateClassName(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append(unit.getName().toUpperCase().charAt(0));
    builder.append(unit.getName().substring(1));
    if (!unit.isUnique())
      builder.append(generateChecksum(unit));
    return builder.toString();
  }

  @Override
  public String generateImplements(Repository repository, Unit unit) {
    StringBuilder builder = new StringBuilder();
    Executable executable = Executable.forUnit(unit);
    builder.append(executable.getType());
    builder.append('<');
    builder.append(repository.getInterfaceType());
    if (executable != Executable.TRANSACTION) {
      builder.append(',').append(' ').append(unit.getReturnType().getCanonicalName(true));
    }
    builder.append('>');
    return builder.toString();
  }

  @Override
  public String generateMethodDeclaration(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append("public");
    builder.append(" ");
    builder.append(unit.getReturnType());
    builder.append(" ");
    builder.append(unit.getName());
    builder.append(unit.getParams().isEmpty() ? "()" : merge("(", ")", ", ", unit.getParams()));
    if (unit.getThrowables().isEmpty())
      return builder.toString();
    builder.append(" throws ");
    builder.append(merge("", "", ", ", unit.getThrowables().toArray()));
    return builder.toString();
  }

  @Override
  public String generateParameterParentheses(Unit unit) {
    List<Param> params = unit.getParams();
    int length = params.size();
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + params.get(0).getName() + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(params.get(0).getName());
    for (int index = 1; index < length; index++) {
      builder.append(", ").append(params.get(index).getName());
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String generateParameterParenthesesWithExecutionTime(Unit unit) {
    String parantheses = generateParameterParentheses(unit);
    for (Param param : unit.getParams()) {
      if (param.isTime())
        return parantheses.replace(param.getName(), "executionTime");
    }
    return parantheses;
  }

  @Override
  public String generateParameterSignature(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (Param param : unit.getParams()) {
      if (param.getIndex() > 0)
        builder.append(", ");
      builder.append(param.getType());
      builder.append(' ').append(param.getName());
    }
    builder.append(')');
    return builder.toString();
  }

  @Override
  public String now() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    return df.format(new Date());
  }

}
