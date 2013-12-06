selenium4j
==========

Selenium4j is a tool translating Selenium IDE HTML scripts to Java JUnit tests.

Selenium4j is a tool translating HTML Selenium scripts to Java JUnit tests allowing easy integration to an automated testing environment. 

### Problems with Selenium IDE HTML tests 

 * While Selenium IDE is great tool for creating HTML tests, the HTML scripts are not Java tests and as such are not usable outside of the Selenium IDE environment
 * Using the IDE to create the Java JUnit test works one way only - from HTML to Java. Re-using the java test within Selenium IDE proved to be unreliable and buggy
 * Using Selenium RC to invoke the HTML files is not practical solution as it allows only a limited configuration such as browser, server and a single suite. E.g.

        java -jar selenium-server.jar -multiwindow -htmlSuite "*iexplore" "https://www.website.com" "C:\suite.html" "C:\results.html"


### Motivation for Selenium4j 
In reality we would like to:
 * Reuse the Selenium HTML suites for regression testing
 * To be able to integrate them within an automated environment (think CruiseControl f.i.)
 * Make the test configurable

### WebDriver (Selenium2) solution? 

Looking into WebDriver there is no analog of the Selenium IDE. The IDE is an amazing tool for recording the user clicks and it should be great if WebDriver solve the HTML automation integration. It seems that the later version of the IDE (1.0.5 as of writing) provides better support for the new Selenium2 API. Yet, the problem with HTML test integration is still on the table. As long as you record your script and export it to HTML,  you are on your own. You need to manually use the IDE to translate the HTML scripts to the desired language (Java in my case), then you need to copy the source, paste it to you Eclipse/Netbeans, make sure compiles, configure it properly. In other words, it's not fun.  WebDriver should provide a solution that simplifies that process and at the end the recorded Selenium IDE script should be seamlessly integrated within an automated environment. In that context, Selenium4j is an example how HTML could be easily translated to a Java JUnit sources.


### How does Selenium4j work? 
In short Selenium4j translates the HTML tests to Java JUnti tests. It iterates through the HTML suite and tests, digest them and discovers their Selenium commands. Each command is being subsequently transformed to a Java JUnit method. At the end of the transformation the HTML scripts are being translated to Java JUnit sources following same directory(package) structure as the suite and tests. In addition, Selenium4j have suite setup/teardown utilities as well as external configuration.

### Using Selenium4j 

http://code.google.com/p/selenium/
http://seleniumhq.org/


1. Open Firefox and [http://seleniumhq.org/download/ Install Selenium IDE]

2. Open [http://seleniumhq.org/projects/ide/ Selenium IDE]

3. Record your test and save it as suite and note the directory. The ant build file assumes */selenium4j/test-html*. It should contain the following structure:

         /selenium4j

            /test-html
        
                /MyTests
            
                     suite.html
                     test1.html
                     test2.html

4. Be aware `MyTests` directory as well as `test1`, `test2` names are being transformed to Java package and classes respectively, thus you should follow Java naming conventions when naming them.

5. Create *selenium4j.properties* file under your test suite as bellow. `selenium4j.properties` is a configuration file containing the web site being tested, the used driver and the number of times the test to be invoked.

        /selenium4j

            /test-html

                # MyTests is the directory where you can store your Selenium IDE HTML suite and test files
                /MyTests
                    suite.html
                    test1.html
                    ...
                    selenium4j.properties
	
6. selenium4j.properties should contain:

        # the web site being tested

        webSite=http://www.website.com

        # A comma separated values of the WebDrivers being used. Accepted drivers: 
        # HtmlUnitDriver, FirefoxDriver, ChromeDriver, InternetExplorerDriver
        driver=FirefoxDriver,ChromeDriver
	
        # How many times we want to iterate and test
        loopCount=1

7. Invoke ANT and execute build.xml. This will iterate through MyTests HTML files and will magically transform them to JUnit source files under `/selenium4j/test-java` directory:

    /selenium4j
        /test-java
            /MyTests
                # this folder will be populated with the HTML tests being converted to Java JUnit sources
                ..
                Test1.java
                Test2.java
                Suite.java

8. Add `test-java` folder to your Eclipse/Netbeans source path and execute the generated JUnit tests. The latter will open the given driver(browser) and follow the same steps as in the HTML test.

### Using the distibutable jar file
As above with an alternate step 7.

Build the jar: `ant dist`  The `selenium4j-transform.jar` will be located in the `/selenium4j/dist` directory.  Feel free to move to a more convenient directory, the dependencies are all included in the jar file. 

Invoke `java -jar dist/selenium4j-transform.jar -htmlDir test-html -junitDir test-java`.  The selenium4j-transform.jar takes the parameters 

    htmlDir: Defaults to ./test-html. The selenese html source.

    junitDir: Defaults to ./test-java. The directory the JUnit tests will be created in.

    dieOnUnimplemented:  Defaults to true.  If your html tests contain a method that has yet to be implemented in java, a string will be written into the test source that will cause a compile error.  If set to false, this string will be commented out, allowing the tests to continue.  (Though if you're dependent on the missing test method, results may be, interesting.)

    createFrom: Defaults to 'suite'.  The name of the file (without extension) to look for to create the JUnit Suite.

### Parallel Testing
Selenium4j test suite derives from `org.kohsuke.junit.ParallelTestSuite` [https://parallel-junit.dev.java.net/ Parallel JUnit] allowing parallel suite testing. Bear in mind that while multiple suites are running concurrently, the tests within a single suite are still being executed sequentially.

### Setting up and Tearing down the tests
Selenium4j supports setup and teardown. Preview the example suite at `/selenium4j/test-html/google` for setup and teardown samples.
    
    /selenium4j
        /test-html
            /MyTests
                suite.html
                test1.html
                ...
                selenium4j.properties
			
                # setup directory follows the same structure as MyTests, 
                # except it will be executed before MyTests, thus it can be used to init suite preconditions
                /setup
                    suite.html
                    setup.html
                    ...
                    selenium4j.properties

                # teardown directory follows the same structure as MyTests, 
                # except it will be executed after MyTests, thus it can be used to cleanup suite resources
                /teardown
                    suite.html
                    setup.html
                    ...
                    selenium4j.properties





1. Open firefox and install Selenium IDE

2. Open Selenium IDE

3. Record your test and save it as suite under /selenium4j/test-html directory.
	
	It should follow the following structure (Assuming you are testing google "GoogleTests" ):
	
    /selenium4j
        /test-html
            /GoogleTests
                suite.html
                test1.html
                test2.html
			
4. Create and store a selenium4j.properties file under your test suite as below

	/selenium4j
		/test-html
			/GoogleTests
				suite.html
				test1.html
				...
				selenium4j.properties

	  If the file selenium4j.properties is not found, selenium4j will hunt up the tree up to the test-html directory looking for a file of that name.  It will use the first one it finds.

5. selenium4j.properties should contain:

        # the web site being tested

        webSite=http://www.nextinterfaces.com

        # The WebDriver being used. Accepted drivers: HtmlUnitDriver, FirefoxDriver, ChromeDriver, InternetExplorerDriver
        driver=FirefoxDriver
	
        # How many times we want to iterate and test
        loopCount=1


6. Edit build.properties to your satisfaction.  Values will determine how and where tests are built.
    The following properties are expected:

    src.test:  Location off the project root where html format tests are to be found.  If not specified, defaults to "test-html".

    src.test.dir:  Location off the project root where java tests will be written.  Defaults to "test-java".

    dieOnUnimplemented:  Defaults to true.  If your html tests contain a method that has yet to be implemented in java, a string will be
        written into the test source that will cause a compile error.  If set to false, this string will be commented out, allowing
        the tests to continue.  (Though if you're dependent on the missing test method, results may be, interesting.)

7. Invoke ANT and execute build.xml. This will iterate through GoogleTests HTML files and will tranform them to
Java Junit source files under "/selenium4j/test-java" directory:

        /selenium4j
            /test-java
                /GoogleTests
                    ..
                    Java JUnit source

8. Add test-java folder to your source path and execute the JUnit tests. The same test will get started opening the
selected driver (browser) 
