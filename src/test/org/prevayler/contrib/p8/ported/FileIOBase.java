package org.prevayler.contrib.p8.ported;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.prevayler.foundation.Cool;

public abstract class FileIOBase extends Assert {

  protected String _testDirectory;
  static private long counter = 0;

  @Before
  public void setUp() throws Exception {
    File tempFile = new File("Test" + System.currentTimeMillis() + counter++);
    assertTrue("Unable to create directory " + tempFile, tempFile.mkdirs());
    _testDirectory = tempFile.getAbsolutePath();
  }

  @After
  public void tearDown() throws Exception {
    delete(_testDirectory);
  }

  protected void deleteFromTestDirectory(String fileName) {
    delete(new File(_testDirectory + File.separator + fileName));
  }

  static public void delete(String fileName) {
    delete(new File(fileName));
  }

  static public void delete(File file) {
    if (file.isDirectory())
      deleteDirectoryContents(file);
    assertTrue("File does not exist: " + file, file.exists());
    if (!file.delete()) {
      System.gc();
      Cool.sleep(666);
      if (file.exists() && !file.delete()) {
        Cool.sleep(666);
        System.gc();
        assertTrue("Unable to delete " + file, file.delete());
      }
    }
  }

  static private void deleteDirectoryContents(File directory) {
    File[] files = directory.listFiles();
    if (files == null)
      return;
    for (int i = 0; i < files.length; i++)
      delete(files[i]);
  }

  protected String journalContents(final String suffix) throws IOException {
    File journal = findJournal(suffix);

    FileInputStream file = new FileInputStream(journal);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int n;
    byte[] b = new byte[1024];
    while ((n = file.read(b)) != -1) {
      buffer.write(b, 0, n);
    }

    file.close();

    return buffer.toString("ISO-8859-1");
  }

  protected File findJournal(final String suffix) {
    File[] files = new File(_testDirectory).listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith("." + suffix);
      }
    });
    assertEquals(1, files.length);
    return files[0];
  }

}
