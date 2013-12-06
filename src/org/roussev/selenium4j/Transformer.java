package org.roussev.selenium4j;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.roussev.selenium4j.VelocityBean.DriverBean;

/**
 * This is the main class iterating through Selenium HTML suite files,
 * discovering the HTML tests for each suite and transforming the tests and
 * suites into Java JUnti source files.
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
public class Transformer {

  private static Logger logger = Logger.getLogger(Transformer.class);

  private final static String SETUP_DIR = "setup";
  private final static String TEARDOWN_DIR = "teardown";
  private final static String TEST_DIR = "./test-html";
  private final static String TEST_BUILD_DIR = "./test-java";
  private final static String ALL_SUITES_TEMPLATE = "/org/roussev/selenium4j/AllSuites.vm";
  private final static String ALL_SEQUENTIAL_TEMPLATE = "/org/roussev/selenium4j/AllSequentialTests.vm";
  private final static String ALL_TEST_TEMPLATE = "/org/roussev/selenium4j/AllTests.vm";
  private final static String SELENIUM_TEST_TEMPLATE = "/org/roussev/selenium4j/SeleniumJava.vm";
  private final static String SUITE_NAME = "suite";

  private final static String PROP_DRIVER = "driver";
  private final static String PROP_WEBSITE = "webSite";
  private final static String PROP_LOOPCOUNT = "loopCount";
  private final static String PROP_CONCURRENT_USERS = "cuncurrentUsers";

  private final static String HTML_DIR_ARG = "htmlDir";
  private final static String JUNIT_DIR_ARG = "junitDir";
  private final static String DIE_ON_UNIMPLEMENTED_ARG = "dieOnUnimp";
  private final static String CREATE_FROM_ARG = "createFrom";

  private static String testSrcDir;
  private static String testBuildDir;
  private static Boolean dieOnUnimplemented = true;
  private static String createFrom;

  // public final static Properties WEBTEST_PROPERTIES = new Properties();
  // public final static String WEBTEST_PROPERTIES_FILE =
  // "./project.properties";
  // public final static String WEBTEST_PROPERTY_RESOURCE_PATH =
  // "resourcesPath";
  private final static MethodReader SETUP_TEAR_METHOD_READER = new SetupTeardownMethodReader();

  public static void main(String[] args) throws Exception {
    new Transformer().execute(args);
  }

  public void execute(String[] args) throws Exception {

    // File webtestPropFile = new File(WEBTEST_PROPERTIES_FILE);
    // if (!webtestPropFile.exists()) {
    // throw new RuntimeException(WEBTEST_PROPERTIES + " not defined.");
    // }
    // WEBTEST_PROPERTIES.load(new FileInputStream(webtestPropFile));
    // logger.debug("Running with loaded properties: " + WEBTEST_PROPERTIES);


    Options options = new Options();

    options.addOption(HTML_DIR_ARG, true, "Source dir for html tests");
    options.addOption(JUNIT_DIR_ARG, true, "Desination dir for java tests");
    options.addOption(DIE_ON_UNIMPLEMENTED_ARG, true, "Die if a test method is unimplemented");
    options.addOption(CREATE_FROM_ARG, true, "Build tests from reading the filesystem");

    CommandLineParser parser = new GnuParser();
    CommandLine cmd = parser.parse(options, args);


    if (cmd.hasOption(HTML_DIR_ARG)) {
        testSrcDir = cmd.getOptionValue(HTML_DIR_ARG);
    }  else {
        testSrcDir = TEST_DIR;
    }

    if (cmd.hasOption(JUNIT_DIR_ARG)) {
        testBuildDir = cmd.getOptionValue(JUNIT_DIR_ARG);
    } else {
        testBuildDir = TEST_BUILD_DIR;
    }

    if (cmd.hasOption(DIE_ON_UNIMPLEMENTED_ARG)) {
        if (cmd.getOptionValue(DIE_ON_UNIMPLEMENTED_ARG).matches("false")) {
            dieOnUnimplemented = false;
        } else if (cmd.getOptionValue(DIE_ON_UNIMPLEMENTED_ARG).matches("0")) {
            dieOnUnimplemented = false;
        }

        org.roussev.selenium4j.CommandToMethodTranslator.setDieOnUnimplemented(dieOnUnimplemented);

    }

    if (cmd.hasOption(CREATE_FROM_ARG)) {
        createFrom = cmd.getOptionValue(CREATE_FROM_ARG);
    } else {
        createFrom = SUITE_NAME;
    }

    File buildDir = new File(testBuildDir);
    if (buildDir.exists()) {
      deleteDir(buildDir, true);
    }
    buildDir.mkdir();

    read(new DefaultMethodReader(), testSrcDir);
  }

  /**
   * Recursively deletes a directory and all its sub directories.
   */
  private static void deleteDir(File dir, boolean isRoot) {
    if (dir.isDirectory() && !".svn".equals(dir.getName())) {
      for (File file : dir.listFiles()) {
        deleteDir(file, false);
      }
    }

    if (!isRoot) {
      dir.delete();
    }
  }

  private static String getFileNameNoSuffix(File f) {
    String fileName = f.getName();
    return fileName.substring(0, fileName.lastIndexOf('.'));

  }

  private final void read(MethodReader methodReader, String testSrcDir) throws Exception {
    File dir = new File(testSrcDir);

    FileFilter dirFilter = new FileFilter() {
      public boolean accept(File file) {
        return file.isDirectory() && !file.getName().equals(".svn");
      }
    };
    for (File suiteDir : dir.listFiles(dirFilter)) {
      doTests(suiteDir, methodReader, null, testSrcDir);

      createAllSuites(suiteDir.getName());
    }
  }

  private void doTests(File dir, MethodReader methodReader, VelocityBean confBean_, String testSrcDir) throws Exception {
    logger.debug("Reading " + dir + " tests...");
    VelocityBean velocityBean = new VelocityBean();
    if (confBean_ != null) {
      velocityBean.setSubstituteEntries(confBean_.getSubstituteEntries());
      velocityBean.setSuiteContext(confBean_.getSuiteContext());
    }

    loadTestProperties(dir, velocityBean, testSrcDir);

    //Collection<File> files = TestParser.parseSuite(suiteFile);
    Collection<File> files = new ArrayList<File>();

    if (createFrom.matches("filesystem")) {
      files = FileUtils.listFiles(dir, new String[] {"html"}, false);

    } else {
      String createFromParsed = createFrom;

      if (!createFromParsed.matches(".+.html$")) {      // put an html extension on file name if need be
          createFromParsed += ".html";
      }

      File suiteFile = new File(dir, createFromParsed);
      if (!suiteFile.exists()) {
          throw new RuntimeException("Missing \"" + createFromParsed + "\" file at " + dir + ".");
      }

      files = TestParser.parseSuite(suiteFile);
    }

    Collection<String> classBeans = new ArrayList<String>();

    String packName = null;
    if (dir.getName().equals(SETUP_DIR)) {
      packName = dir.getParentFile().getName() + "." + SETUP_DIR;
    } else if (dir.getName().equals(TEARDOWN_DIR)) {
      packName = dir.getParentFile().getName() + "." + TEARDOWN_DIR;
    } else {
      packName = dir.getName();
      loadSuiteContext(dir, velocityBean, testSrcDir);
    }

    for (File f : files) {
      StringBuilder sb = new StringBuilder();
      String className = getFileNameNoSuffix(f);
      Collection<Command> cmds = TestParser.parseHTML(f);
      for (Command c : cmds) {
        String cmdStr = CommandToMethodTranslator.discovery(c);
        cmdStr = getPopulatedCmd(className, cmdStr, velocityBean);
        sb.append("\n\t\t" + cmdStr);
      }

      ClassBean classBean = new ClassBean();
      classBean.setPackageName(packName);
      classBean.setClassName(className);
      classBean.setMethodBody(sb.toString());
      classBean.setWebSite(velocityBean.getWebsite());
      writeTestFile(dir, methodReader, classBean, velocityBean, classBeans);
    }

    createAllTests(classBeans, velocityBean, packName, dir.getName());

    doSetupTeardownTests(dir, methodReader, SETUP_DIR, velocityBean, testSrcDir);
    doSetupTeardownTests(dir, methodReader, TEARDOWN_DIR, velocityBean, testSrcDir);
  }

  private String getPopulatedCmd(String className, String cmdStr,
      VelocityBean velocityBean) {
    Map<String, String> subEntries = velocityBean
        .getSubstituteEntries(className);
    for (String key : subEntries.keySet()) {
      if (cmdStr.contains("\"" + key + "\"")) {
        String ek = subEntries.get(key);
        return cmdStr.replace("\"" + key + "\"", "get(\"" + ek + "\")");
      }
    }
    return cmdStr;
  }

  private void loadTestProperties(File dir, VelocityBean velocityBean, String testSrcDir) throws Exception {
    File propFile = new File(dir, Globals.CONF_FILE);
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(propFile));
      String bp = properties.getProperty(PROP_DRIVER);
      String[] bpArr = bp.split(",");
      for (int i = 0; i < bpArr.length; i++) {
        if (bpArr[i] == null || bpArr[i].trim().equals("")) {
          continue;
        }
        DriverBean bProxy = new DriverBean(bpArr[i]);
        velocityBean.addDriverBean(bProxy);
      }
      velocityBean.setLoopCount(properties.getProperty(PROP_LOOPCOUNT));
      velocityBean.setCuncurrentUsers(properties
          .getProperty(PROP_CONCURRENT_USERS));
      velocityBean.setWebsite(properties.getProperty(PROP_WEBSITE));

    } catch (FileNotFoundException e1) {
      // if we have no prop file, we'll walk up the parent tree, stopping at the testSrcDir, looking for a selenium4j.properties file
         File parent = dir.getParentFile();
         if (dir.getName().matches(testSrcDir)) {  //Stop at the test source dir
             throw new RuntimeException("Missing \"" + Globals.CONF_FILE + "\" file at " + dir + ".");

         } else {
            loadTestProperties(parent, velocityBean,testSrcDir);
         }

    }
  }

  private void loadSuiteContext(File dir, VelocityBean velocityBean, String testSrcDir) throws Exception {
    logger.debug("Loading SuiteContext for '" + dir + "'");
    File propFile = new File(dir, Globals.CONF_FILE);
    Properties properties = new Properties();
    File searchDir = dir;
    if (!propFile.exists()) {
        while (!propFile.exists() && !searchDir.getName().matches(testSrcDir)) {
            searchDir = searchDir.getParentFile();
            propFile = new File(searchDir, Globals.CONF_FILE);

        }

    }

    try {
      properties.load(new FileInputStream(propFile));

      // load the context. entries
      for (Object key : properties.keySet()) {
        String key_ = (String) key;
        if (key_.startsWith("context.")) {
          String ctxKey = key_.replace("context.", "");
          velocityBean.addToContext(ctxKey, properties.getProperty(key_));
        }
      }

      // load the substitute. entries
      for (Object key : properties.keySet()) {
        // logger.debug("Transformer.loadTestProperties()" +
        // key);
        String key_ = (String) key;
        if (key_.startsWith("substitute.")) {
          String ctxKey = key_.replace("substitute.", "");
          velocityBean.addSubstituteEntry(ctxKey, properties.getProperty(key_));
        }
      }

      // logger.debug(confBean);
      File copiedProp = new File(testBuildDir + File.separator + dir.getName());
      copiedProp.mkdir();
      Utilities.copyFile(propFile, new File(testBuildDir + File.separator + dir.getName() + File.separator + Globals.CONF_FILE));
    } catch (FileNotFoundException e1) {
        throw new RuntimeException("Missing \"" + Globals.CONF_FILE + "\" file at " + dir + ".");
    }
  }

  private void doSetupTeardownTests(File dir, MethodReader methodReader, String setupOrTeardown, VelocityBean velocityBean, String testSrcDir) throws Exception {
    if (dir.getName().equals(SETUP_DIR) || dir.getName().equals(TEARDOWN_DIR)) {
      return;
    }
    File setupDir = new File(dir + File.separator + setupOrTeardown);
    if (setupDir.exists()) {
      logger.debug("Preparing '" + setupOrTeardown + "' Tests at '" + setupDir
          + "' ...");
      doTests(setupDir, SETUP_TEAR_METHOD_READER, velocityBean, testSrcDir);
    } else {
      logger.debug("Skipping '" + setupOrTeardown + "' Tests as directory '"
              + setupDir + "' doesn't exist ...");

    }
  }

  private void writeTestFile(File dir, MethodReader methodReader,
      ClassBean classBean, VelocityBean velocityBean,
      Collection<String> classBeans) throws Exception {
    for (DriverBean bp : velocityBean.getDriverBeans()) {
      String subPackage = filterSubPackage(bp);
      methodReader.read(dir, subPackage, classBean, bp);
      classBeans.add(classBean.getPackageName() + "." + subPackage + "."
          + classBean.getClassName());
    }
  }

  private static String filterSubPackage(DriverBean bp) {
    if (FirefoxDriver.class.getSimpleName().equals(bp.getDriver())) {
      return "FF";
    } else if (ChromeDriver.class.getSimpleName().equals(bp.getDriver())) {
      return "CR";
    } else if (HtmlUnitDriver.class.getSimpleName().equals(bp.getDriver())) {
      return "HT";
    } else if (InternetExplorerDriver.class.getSimpleName().equals(
        bp.getDriver())) {
      return "IE";
    } else {
      throw new UnsupportedOperationException("Driver '" + bp.getDriver()
          + "' is not supported.");
    }
  }

  private void createAllTests(Collection<String> classBeans,
      VelocityBean velocityBean, String packageName, String dirName) {
    logger.debug("Building AllTest Suite for " + dirName + "");

    boolean isSetupTeardown = packageName.contains(SETUP_DIR)
        || packageName.contains(TEARDOWN_DIR);

    VelocitySuiteTranslator t = null;
    if (isSetupTeardown) {
      t = new VelocitySuiteTranslator(ALL_SEQUENTIAL_TEMPLATE);
    } else {
      t = new VelocitySuiteTranslator(ALL_TEST_TEMPLATE);
    }

    String[] packageDirs = packageName.split("\\.");
    String allDirName = "";
    for (String s : packageDirs) {
      allDirName = allDirName + File.separator + s;
    }

    for (@SuppressWarnings("unused")
    DriverBean bp : velocityBean.getDriverBeans()) {
      String fileOut = testBuildDir + allDirName + File.separator
          + "AllTests.java";
      t.doWrite(classBeans, velocityBean, packageName, fileOut, false, false);
    }
  }

  private void createAllSuites(String packageName) {
    logger.debug("Building AllTest Suite for " + packageName + "");

    File dirSetup = new File(testBuildDir + File.separator + packageName
        + File.separator + SETUP_DIR);
    File dirTeardown = new File(testBuildDir + File.separator + packageName
        + File.separator + TEARDOWN_DIR);

    VelocitySuiteTranslator t = new VelocitySuiteTranslator(ALL_SUITES_TEMPLATE);

    String fileOut = testBuildDir + File.separator + packageName
        + File.separator + "AllSuite.java";
    t.doWrite(null, null, packageName, fileOut, dirSetup.exists(), dirTeardown
        .exists());
  }

  // -----------
  static class DefaultMethodReader implements MethodReader {
    public void read(File dir, String subPackage, ClassBean classBean,
        DriverBean driverBean) throws Exception {
      File subDir = new File(dir.getName() + File.separator + subPackage);
      String dirName = subDir.getName();
      File packageDir = new File(testBuildDir + File.separator
          + classBean.getPackageName() + File.separator + dirName);
      packageDir.mkdirs();

      VelocityTestTranslator t = new VelocityTestTranslator(
          SELENIUM_TEST_TEMPLATE);

      t.doWrite(classBean, driverBean, dirName, packageDir.getAbsolutePath()
          + File.separator + classBean.getClassName() + ".java");
    }
  }

  static class SetupTeardownMethodReader implements MethodReader {
    public void read(File dir, String subPackage, ClassBean classBean,
        DriverBean driverBean) throws Exception {

      File subDir = new File(dir.getName() + File.separator + subPackage);
      String dirName = dir.getName();
      String subDirName = subDir.getName();
      String parentDirName = dir.getParentFile().getName();

      File packageDir = new File(testBuildDir + File.separator
          + parentDirName + File.separator + dirName + File.separator
          + subDirName);
      packageDir.mkdirs();
      VelocityTestTranslator t = new VelocityTestTranslator(
          SELENIUM_TEST_TEMPLATE);

      t.doWrite(classBean, driverBean, subDirName, packageDir.getAbsolutePath()
          + File.separator + classBean.getClassName() + ".java");
    }
  }

  interface MethodReader {
    void read(File dir, String subPackage, ClassBean classBean,
        DriverBean driverBean) throws Exception;
  }

}
