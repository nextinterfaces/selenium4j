package org.roussev.selenium4j;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * This class transforms Selenium Suite from HTML to Junit Java source file using
 * velocity "AllSuites.vm" template
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
class VelocitySuiteTranslator {

  private static Logger logger = Logger.getLogger(VelocitySuiteTranslator.class);
  
  private String templateFile;

  VelocitySuiteTranslator(String templateFile) {
    this.templateFile = templateFile;
  }

  void doWrite(Collection<String> classesList, VelocityBean velocityBean,
      String packageName, String fileOut, boolean setupDirExist,
      boolean teardownDirExist) {
    logger.debug("Flushing content to java files ...");

    try {

      Velocity.init();

      /*
       * Make a context object and populate with the data. This is where the
       * Velocity engine gets the data to resolve the references (ex. $list) in
       * the template
       */

      VelocityContext context = new VelocityContext();
      if (velocityBean != null) {
        context.put("loopCount", velocityBean.getLoopCount());
        context.put("cuncurrentUsers", velocityBean.getCuncurrentUsers());
      }
      context.put("testClasses", classesList);
      context.put("package", packageName);
      context.put("setupDirExist", setupDirExist);
      context.put("teardownDirExist", teardownDirExist);

      /*
       * get the Template object. This is the parsed version of your template
       * input file. Note that getTemplate() can throw ResourceNotFoundException
       * : if it doesn't find the template ParseErrorException : if there is
       * something wrong with the VTL Exception : if something else goes wrong
       * (this is generally indicative of as serious problem...)
       */

      Template template = null;

      try {
        template = Velocity.getTemplate(templateFile);
      } catch (ResourceNotFoundException rnfe) {
        System.out
            .println("VelocityTestTranslator : error : cannot find template "
                + templateFile);
      } catch (ParseErrorException pee) {
        logger.debug("VelocityTestTranslator : Syntax error in template "
            + templateFile + ":" + pee);
      }

      /*
       * Now have the template engine process your template using the data
       * placed into the context. Think of it as a 'merge' of the template and
       * the data to produce the output stream.
       */

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(fileOut)));

      if (template != null)
        template.merge(context, writer);

      /*
       * flush and cleanup
       */

      writer.flush();
      writer.close();
    } catch (Exception e) {
      System.err.println(e);
    }
    logger.debug("AllTest suite transformation done.");
  }

}
