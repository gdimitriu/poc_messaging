package from_activeMQ;
import static java.lang.System.out;
 
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
 
import org.apache.activemq.ActiveMQConnectionFactory;
 
public final class ActiveMQSampleReceiveQueue {
 
  public static void main(final String[] args) throws Exception {
    final ConnectionFactory connFactory = new ActiveMQConnectionFactory();
 
    final Connection conn = connFactory.createConnection();
 
    final Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
    final Destination dest = sess.createQueue("SampleQueue");
 
    final MessageConsumer cons = sess.createConsumer(dest);
 
    conn.start();
 
    final Message msg = cons.receive();
 
    out.println(msg);
 
    conn.close();
  }
 
}
 