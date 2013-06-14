package org.roussev.selenium4j;

/**
 * This class holds class meta information used for generating the JUnit source
 * class
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
class ClassBean {

  private String packageName;
  private String className;
  private String methodBody;
  private String webSite;
  private String loopCount;
  private String cuncurrentUsers;

  public String getWebSite() {
    return webSite;
  }

  public void setWebSite(String webSite) {
    this.webSite = webSite;
  }

  public String getLoopCount() {
    return loopCount;
  }

  public void setLoopCount(String loopCount) {
    this.loopCount = loopCount;
  }

  public String getCuncurrentUsers() {
    return cuncurrentUsers;
  }

  public void setCuncurrentUsers(String cuncurrentUsers) {
    this.cuncurrentUsers = cuncurrentUsers;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getMethodBody() {
    return methodBody;
  }

  public void setMethodBody(String methodBody) {
    this.methodBody = methodBody;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public String toString() {
    return "ClassBean{" + packageName + "." + className + "" + ", methodBody="
        + methodBody + "}";
  }

}
