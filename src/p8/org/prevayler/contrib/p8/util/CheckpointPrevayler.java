package org.prevayler.contrib.p8.util;

import java.io.File;

public class CheckpointPrevayler<P> extends VolatilePrevayler<P> {

  private final String baseName;

  public CheckpointPrevayler(P prevalentSystem, String baseName) {
    super(Serialization.toPrevalentSystem(baseName, prevalentSystem, null));
    this.baseName = baseName;
  }

  @Override
  public File takeSnapshot() throws Exception {
    return Serialization.toSnapshot(prevalentSystem(), new File(baseName)).toFile();
  }

}