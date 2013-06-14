package org.roussev.selenium4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is being used as a helper class to store configuration values
 * later being used during velocity transformation.
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
class VelocityBean {

  private String loopCount;
  private String cuncurrentUsers;
  private Collection<DriverBean> driverBeans = new ArrayList<DriverBean>();
  private Map<String, String> suiteContext = new HashMap<String, String>();
  private Map<String, String> substituteEntries = new HashMap<String, String>();
  private String website;

  public Map<String, String> getSuiteContext() {
    return suiteContext;
  }

  public void setSuiteContext(Map<String, String> suiteContext) {
    this.suiteContext = suiteContext;
  }

  public Map<String, String> getSubstituteEntries() {
    return substituteEntries;
  }

  public void setSubstituteEntries(Map<String, String> substituteEntries) {
    this.substituteEntries = substituteEntries;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public void addSubstituteEntry(String key, String value) {
    substituteEntries.put(key, value);
  }

  public Map<String, String> getSubstituteEntries(String className) {
    Map<String, String> classSubstitutes = new HashMap<String, String>();
    for (String key : substituteEntries.keySet()) {
      if (key.startsWith(className + ".")) {
        classSubstitutes.put(key.replace(className + ".", ""),
            substituteEntries.get(key));
      }
    }
    return classSubstitutes;
  }

  public void addToContext(String key, String value) {
    suiteContext.put(key, value);
  }

  public String getFromContext(String key) {
    return suiteContext.get(key);
  }

  public void addDriverBean(DriverBean o) {
    driverBeans.add(o);
  }

  public Collection<DriverBean> getDriverBeans() {
    return driverBeans;
  }

  public String getCuncurrentUsers() {
    return cuncurrentUsers;
  }

  public void setCuncurrentUsers(String cuncurrentUsers) {
    this.cuncurrentUsers = cuncurrentUsers;
  }

  public String getLoopCount() {
    return loopCount;
  }

  public void setLoopCount(String loopCount) {
    this.loopCount = loopCount;
  }

  /**
   * Inner help class holding driver String entry
   */
  public static class DriverBean {

    private String driver;

    public DriverBean(String driver) {
      this.driver = driver;
    }

    public String getDriver() {
      return driver;
    }

    @Override
    public String toString() {
      return "DriverBean{" + driver + "}";
    }
  }

  @Override
  public String toString() {
    return "VelocityBean{" + driverBeans + ", loopCount=" + loopCount
        + ", cuncurrentUsers=" + cuncurrentUsers + "," + " ctx=" + suiteContext
        + ",  substitutes=" + substituteEntries + "}";
  }

}
