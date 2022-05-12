package from_jms_activemq.b2bi;

import javax.jms.XATopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class ValidateJNDIArtemisTransactionManager {
    public ValidateJNDIArtemisTransactionManager(String broker, String userName, String password) {
        try {
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            env.put("java.naming.provider.url", "tcp://ubuntu:61616?type=TOPIC_XA_CF");
            env.put(Context.SECURITY_PRINCIPAL,"user");
            env.put(Context.SECURITY_CREDENTIALS,"password");
            env.put("topic.Hot Deals", "Hot Deals");
            InitialContext jndi = new InitialContext(env);
            XATopicConnectionFactory tFactory = (XATopicConnectionFactory) jndi.lookup("XA" + broker);
            //TransactionManager txManager = (TransactionManager) jndi.lookup("../../tx/txmngr");
/*            TransactionManager txManager = TransactionManager.transactionManager();
            txManager.begin();
            txManager.rollback(); */
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public static void main(String...args) {
        String broker, username, password;
        if (args.length == 3) {
            broker = args[0];
            username = args[1];
            password = args[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java ValidateJNDIArtemisTransactionManager broker username password");
            return;
        }
        ValidateJNDIArtemisTransactionManager retailer = new ValidateJNDIArtemisTransactionManager(broker,username,password);
    }
}
