/**
 * 
 */
package server.solace.generals;

import javax.jms.Connection;

import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

/**
 * @author gdimitriu
 *
 */
public class SolaceUtils {
	private static String username = "admin";
	private static String password = "admin";
	private static String vpn ="default";
	private static int port=55555;
	private static String host="localhost";

	/**
	 * @param username the username to set
	 */
	public static void setUsername(String username) {
		SolaceUtils.username = username;
	}

	/**
	 * @param password the password to set
	 */
	public static void setPassword(String password) {
		SolaceUtils.password = password;
	}

	/**
	 * @param vpn the vpn to set
	 */
	public static void setVpn(String vpn) {
		SolaceUtils.vpn = vpn;
	}

	/**
	 * @param port the port to set
	 */
	public static void setPort(int port) {
		SolaceUtils.port = port;
	}

	/**
	 * @param host the host to set
	 */
	public static void setHost(String host) {
		SolaceUtils.host = host;
	}

	/**
	 * 
	 */
	public static  Connection createConnection() throws Exception {
		SolConnectionFactory cf = SolJmsUtility.createConnectionFactory();
		cf.setUsername(username);
		cf.setPassword(password);
		cf.setVPN(vpn);
		cf.setHost(host);
		cf.setPort(port);
		return cf.createConnection();
	}
	
	public static JCSMPSession createJMSPSession() throws InvalidPropertiesException {
		final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, host + ":" + port);     // host:port
        properties.setProperty(JCSMPProperties.USERNAME, username); // client-username
        properties.setProperty(JCSMPProperties.VPN_NAME,  vpn); // message-vpn
        properties.setProperty(JCSMPProperties.PASSWORD, password); // client-password
		return JCSMPFactory.onlyInstance().createSession(properties);
	}
}
