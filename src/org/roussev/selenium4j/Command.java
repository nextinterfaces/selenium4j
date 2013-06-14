package org.roussev.selenium4j;

/**
 * This class holds Selenium Command data
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
public class Command {

  private String name;
  private String target;
  private String value;

  public void setName(String name) {
    this.name = name;
  }

  public void setTarget(String t) {
    this.target = t;
  }

  public void setValue(String v) {
    this.value = v;
  }

  public String getName() {
    return name;
  }

  public String getTarget() {
    return target;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Command{" + name + "\n" + target + "\n" + value + "}";
  }

}
