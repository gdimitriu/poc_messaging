/**
 * 
 */
package activemq.transaction_groups;

import java.util.Set;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;

/**
 * @author Gabriel Dimitriu
 *
 */
public class MySecurityManager implements ActiveMQSecurityManager {

	/**
	 * 
	 */
	public MySecurityManager() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager#validateUser(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean validateUser(String user, String password) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager#validateUserAndRole(java.lang.String, java.lang.String, java.util.Set, org.apache.activemq.artemis.core.security.CheckType)
	 */
	@Override
	public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
		if(!validateUser(user, password)) {
			return false;
		}
		if (checkType.equals(CheckType.CREATE_NON_DURABLE_QUEUE) || checkType.equals(CheckType.DELETE_NON_DURABLE_QUEUE)) {
			return true;
		}
		for (Role role : roles) {
			if(role.getName().equals(user) && checkType.hasRole(role)) {
				return true;
			}
		}
		return false;
	}

}
