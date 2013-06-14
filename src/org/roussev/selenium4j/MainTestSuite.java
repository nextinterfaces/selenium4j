package org.roussev.selenium4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import junit.framework.TestSuite;

/**
 * 
 * <p>
 * @author Atanas Roussev (http://www.roussev.org)
 */
public abstract class MainTestSuite extends TestSuite {

  private static Logger logger = Logger.getLogger(MainTestSuite.class);
  
	private static Map<String, String> ctx;

	public MainTestSuite() {
		init();
	}

	public MainTestSuite(String name) {
		super(name);
		init();
	}

	public void init() {		
		if(ctx != null){
			return;
		}
		ctx = new HashMap<String, String>();
		
		Properties props = new Properties();
		try {
			props.load(getClass().getResourceAsStream(Globals.CONF_FILE));
			for (Object key : props.keySet()) {
				String key_ = (String) key;
				ctx.put(key_, (String) props.getProperty(key_));
			}

			Map<String, String> ctxVals = new HashMap<String, String>();
			for (String key : ctx.keySet()) {
				if (key.startsWith("context.")) {
					try {
						ctxVals.put(key, Utilities.getContextValue(ctx.get(key),
																ctx));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// putting the populated values back to ctx
			ctx.putAll(ctxVals);

			Map<String, String> subsVals = new HashMap<String, String>();
			for (String key : ctx.keySet()) {
				if (key.startsWith("substitute.")) {
					subsVals.put(key, Utilities.getContextValue(ctx.get(key), ctx));
				}
			}

			// putting the populated values back to ctx
			ctx.putAll(subsVals);

			logger.debug("Running " + this + " with context: " + ctx);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Map<String, String> getContext() {
		return ctx;
	}
}
