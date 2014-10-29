package org.prevayler.contrib.p8.benchmark;

import java.io.File;

import org.prevayler.Prevayler;

@FunctionalInterface
public interface Creator {

  Prevayler<StringBuilder> create(StringBuilder builder, File folder, int numberOfThreads) throws Exception;

}
