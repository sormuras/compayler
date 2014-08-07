package prevaylertutorial.e101;

import java.io.Serializable;

public class Person implements Serializable {

  private static final long serialVersionUID = 1l;

  private String identity;
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Person person = (Person) o;

    if (identity != null ? !identity.equals(person.identity) : person.identity != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return identity != null ? identity.hashCode() : 0;
  }
}
