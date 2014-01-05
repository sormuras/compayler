package sandbox;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import sandbox.Compayler.Directive;
import sandbox.Compayler.ExecutionTime;
import sandbox.Compayler.Mode;

public interface Nestable {

  @Directive(Mode.DIRECT)
  Nestable direct();

  long executionTime(@ExecutionTime Date time);

  Date executionTime(Date seed, @ExecutionTime Date time, Date... dates);

  Map<String, Date> zzz(List<Map<Integer, Number>> list, int... numbers) throws IllegalStateException, IOException;

}
