package org.prevayler.contrib.compayler;

public interface Generator {

  String generateChecksum(Unit unit);

  String generateClassName(Unit unit);

  String generateImplements(Repository repository, Unit unit);

  String generateMethodDeclaration(Unit unit);

  String generateParameterParentheses(Unit unit);

  String generateParameterParenthesesWithExecutionTime(Unit unit);

  String generateParameterSignature(Unit unit);

  String now();

}