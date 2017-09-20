/**
 * 
 */
package servers.jms.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gdimitriu
 *
 */
public interface ICommands {

	String COMMAND_PROPERTY = "command";
	String LOGIN = "login";
	String LOGOUT = "logout";
	String FLOW = "flow";
	String TRANSFORMER = "transformer";
	
	public static List<String> getAllCommands() {
		List<String >commands = new ArrayList<String>();
		commands.add(LOGIN);
		commands.add(LOGOUT);
		commands.add(FLOW);
		commands.add(TRANSFORMER);
		return commands;
	}
}
