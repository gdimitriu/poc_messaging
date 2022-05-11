package from_jms_activemq.b2bi;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
//to use transaction manager use _XA_CF versions
public class RetailerTransaction implements MessageListener {
    private TopicConnection connect = null;
    private TopicSession session = null;
    private TopicPublisher publisher = null;
    private Topic hotDealsTopic = null;
    private TopicSubscriber subscriber = null;
    private boolean inRollback = false;
    private boolean rollbackOnly = false;

    public RetailerTransaction(String broker, String userName, String password) {
        try {
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            env.put("java.naming.provider.url", "tcp://ubuntu:61616?type=TOPIC_CF");
            env.put("topic.Hot Deals", "Hot Deals");
            InitialContext jndi = new InitialContext(env);
            TopicConnectionFactory factory = (TopicConnectionFactory) jndi.lookup(broker);
            connect = factory.createTopicConnection(userName, password);
            connect.setClientID("DurableRetailer");
            session = connect.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
            hotDealsTopic = (Topic) jndi.lookup("Hot Deals");
            subscriber = session.createDurableSubscriber(hotDealsTopic,"Hot Deals Subscription");
            subscriber.setMessageListener(this);
            connect.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    @Override
    public void onMessage(Message message) {
        try {
            autoBuy(message);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    private void autoBuy(Message message) {
        int count = 1000;
        try {
            boolean redelivered = message.getJMSRedelivered();
            StreamMessage streamMessage = (StreamMessage) message;
            if (redelivered) {
                System.out.println("\nMessage redelivered, inRollback: " + inRollback + " rollbackOnly: " + rollbackOnly);
                streamMessage.reset();
            }
            if (streamMessage.propertyExists("SEQUENCE_MARKER")) {
                String sequence = streamMessage.getStringProperty("SEQUENCE_MARKER");
                if (sequence.equalsIgnoreCase("END_SEQUENCE")) {
                    if (redelivered && inRollback) {
                        inRollback = false;
                        rollbackOnly = false;
                        session.commit();
                    } else if (rollbackOnly) {
                        inRollback = true;
                        session.rollback();
                    } else {
                        session.commit();
                    }
                }
                return;
            }
            if (rollbackOnly)
                return;
            String dealDesc = streamMessage.readString();
            String itemDesc = streamMessage.readString();
            float oldPrice = streamMessage.readFloat();
            float newPrice = streamMessage.readFloat();
            System.out.println("Received Hot Buy :" + dealDesc);
            if (newPrice == 0 || oldPrice/newPrice > 1.1) {
                System.out.println("\nBuying " + count + " " + itemDesc);
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText(count + " " + itemDesc);
                Topic buyTopic = (Topic) message.getJMSReplyTo();
                publisher = session.createPublisher(buyTopic);
                textMessage.setJMSCorrelationID("DurableRetailer");
                publisher.publish(textMessage, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, 180000);
            } else {
                System.out.println("\nBad Deal - Not buying.");
                rollbackOnly = true;
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    private void exit(String s) {
        try {
            if (s != null && s.equalsIgnoreCase("unsubscribe")) {
                subscriber.close();
                session.unsubscribe("Hot Deals Subscription");
            }
            connect.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String...args) {
        String broker, username, password;
        if (args.length == 3) {
            broker = args[0];
            username = args[1];
            password = args[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java Retailer broker username password");
            return;
        }
        RetailerTransaction retailer = new RetailerTransaction(broker,username,password);
        try {
            System.out.println("\nRetailer application started.\n");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String s = stdin.readLine();
                if (s == null)
                    retailer.exit(null);
                else if (s.equalsIgnoreCase("unsubscribe"))
                    retailer.exit(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
