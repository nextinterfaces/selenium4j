package org.roussev.selenium4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * This class contains utility helper methods
 * <p>
 * 
 * @author Atanas Roussev (http://www.roussev.org)
 */
public class Utilities {

  public static void copyFile(File in, File out) throws IOException {
    FileChannel inChannel = new FileInputStream(in).getChannel();
    FileChannel outChannel = new FileOutputStream(out).getChannel();
    try {
      inChannel.transferTo(0, inChannel.size(), outChannel);
    } catch (IOException e) {
      throw e;
    } finally {
      if (inChannel != null)
        inChannel.close();
      if (outChannel != null)
        outChannel.close();
    }
  }

  public static String getContextValue(String key, Map<String, String> ctx) {
    if (key.startsWith("${")) {
      key = key.replace("${", "");
      key = key.replace("}", "");
      return ctx.get(key);
    }
    try {
      return Class.forName(key).newInstance().toString();
    } catch (Exception ignore) {
    }
    return key;
  }

}