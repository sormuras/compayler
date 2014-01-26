package de.sormuras.compayler.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.service.SourceFactory;

public class DefaultSourceFactory implements SourceFactory {
  
  public static String now() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    return df.format(new Date());
  } 

  @Override
  public <X> Source createSource(Compayler compayler, List<Description<X>> descriptions) {
    Lines lines = new Lines();
    lines.add("/**", " * Class " + compayler.getDecoratorClassName() + " generated for " + compayler.getInterfaceName() + ".", " */");
    for (Description<X> description : descriptions) {
      lines.add("// " + description.getSignature().getName());
    }
    return new Source(compayler.getDecoratorPackage(), compayler.getDecoratorName(), lines.getLines());
  }

}
