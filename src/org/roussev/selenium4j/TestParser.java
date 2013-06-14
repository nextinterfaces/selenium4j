package org.roussev.selenium4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang.StringEscapeUtils;
/**
 * 
 * <p>
 * @author Atanas Roussev (http://www.roussev.org)
 */
class TestParser {

	static Collection<File> parseSuite(File suiteFile) throws Exception {

		String line;

		BufferedReader br = new BufferedReader(new FileReader(suiteFile));
		Collection<File> testFiles = new ArrayList<File>();
		while ((line = br.readLine()) != null) {
			if (line.contains("<a href=")) {
				String[] parts = line.split("\"");
				File f = new File(suiteFile.getParentFile() + File.separator
						+ parts[1]);
				if (!f.exists()) {
					throw new RuntimeException("Missing \""
							+ suiteFile.getParentFile() + File.separator + f
							+ ".");
				}
				testFiles.add(f);
			}
		}
		return testFiles;
	}

	static Collection<Command> parseHTML(File testHtml) throws Exception {
		String line;

		BufferedReader br = new BufferedReader(new FileReader(testHtml));
		boolean parsingFile = false;
		boolean parsingCommand = false;
		LinkedList<Command> commands = new LinkedList<Command>();
		while ((line = br.readLine()) != null) {
			if (parsingFile && line.contains("</tbody>")) {
				parsingFile = false;
			}
			if (parsingFile) {

				if (line.contains("</tr>")) {
					parsingCommand = false;
				}

				if (parsingCommand) {
					Command cmd = commands.getLast();
					if (cmd.getName() == null) {
						cmd.setName(filterTD(line));
					} else if (cmd.getTarget() == null) {
						cmd.setTarget(filterTD(line));
					} else {
						cmd.setValue(filterTD(line));
					}
				}
				if (line.contains("<tr>")) {
					parsingCommand = true;
					Command cmd = new Command();
					commands.add(cmd);
				}
			}
			if (!parsingFile && line.contains("<tbody>")) {
				parsingFile = true;
			}
		}
		return commands;
	}

	private static String filterTD(String line) {
		line = line.trim();
		line = line.substring(4);
		line = line.substring(0, line.length() - 5);
        line = line.replaceAll("&nbsp;", " ");  // unescapeHTML doesn't handle this one well
        line = StringEscapeUtils.unescapeHtml(line);

		return line;
	}

}
