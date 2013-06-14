package org.roussev.selenium4j;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.roussev.selenium4j.VelocityBean.DriverBean;

/**
 * This class transforms Selenium Test from HTML to JUnit Java source file using
 * velocity "SeleniumJava.vm" template
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
class VelocityTestTranslator {

  private static Logger logger = Logger.getLogger(VelocityTestTranslator.class);
  
  private String templateFile;

  VelocityTestTranslator(String templateFile) {
    this.templateFile = templateFile;
  }

  void doWrite(ClassBean classBean, DriverBean driverBean, String dirName,
      String fileOut) {
    logger.debug("Flushing content to java files ...");
    try {

      Velocity.init();

      /*
       * Make a context object and populate with the data. This is where the
       * Velocity engine gets the data to resolve the references (ex. $list) in
       * the template
       */

      VelocityContext context = new VelocityContext();
      context.put("packageName", classBean.getPackageName() + "." + dirName);
      context.put("className", classBean.getClassName());
      context.put("methodBody", classBean.getMethodBody());
      context.put("driver", driverBean.getDriver());
      context.put("webSite", classBean.getWebSite());

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
      logger.debug(e);
    }
    logger.debug("tests done.");
  }

}
