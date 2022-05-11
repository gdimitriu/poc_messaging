package from_jms_activemq.b2bi;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class QRetailer implements MessageListener {
    private QueueConnection qConnect = null;
    private QueueSession qSession = null;
    private QueueSender qSender = null;
    private TopicConnection tConnect = null;
    private TopicSession tSession = null;
    private Topic hotDealsTopic = null;
    private TopicSubscriber tsubscriber = null;
    private static boolean useJNDI = false;
    private static String uname = null;
    public QRetailer(String broker, String username, String password) {
        try {
            TopicConnectionFactory tFactory = null;
            QueueConnectionFactory qFactory = null;
            InitialContext jndi = null;
            uname = username;
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            env.put("java.naming.provider.url", "tcp://ubuntu:61616?type=TOPIC_CF,tcp://ubuntu:61616?type=QUEUE_CF");
            env.put("topic.Hot Deals", "Hot Deals");
            jndi = new InitialContext(env);
            tFactory = (TopicConnectionFactory) jndi.lookup("Topic" + broker);
            qFactory = (QueueConnectionFactory) jndi.lookup("Queue" + broker);
            tConnect = tFactory.createTopicConnection(username,password);
            qConnect = qFactory.createQueueConnection(username,password);
            tConnect.setClientID(username+"topic");
            qConnect.setClientID(username+"queue");
            tSession = tConnect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            qSession = qConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            hotDealsTopic = (Topic) jndi.lookup("Hot Deals");
            tsubscriber = tSession.createDurableSubscriber(hotDealsTopic,"Hot Deals Subscription");
            tsubscriber.setMessageListener(this);
            tConnect.start();
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
        try {
            StreamMessage streamMessage = (StreamMessage) message;
            String dealDesc = streamMessage.readString();
            String itemDesc = streamMessage.readString();
            float oldPrice = streamMessage.readFloat();
            float newPrice = streamMessage.readFloat();
            System.out.println("Received Hot Buy :" + dealDesc);
            if (newPrice == 0 || oldPrice /newPrice > 1.1) {
                int count = (int)(Math.random()*(double)1000);
                System.out.println("\nBuying " + count + " " + itemDesc);
                TextMessage textMessage = tSession.createTextMessage();
                textMessage.setText(count + " " + itemDesc);
                textMessage.setIntProperty("QTY",count);
                textMessage.setJMSCorrelationID(uname);
                Queue buyQueue = (Queue) message.getJMSReplyTo();
                qSender = qSession.createSender(buyQueue);
                qSender.send(textMessage,DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY,1800000);
            } else {
                System.out.println("\nBad Deal. Not buying");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    private void exit(String s) {
        try {
            if (s != null && s.equalsIgnoreCase("unsubscribe")) {
                tsubscriber.close();
                tSession.unsubscribe("Hot Deals Subscription");
            }
            tConnect.close();
            qConnect.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    public static void main(String...args) {
        String broker,username, password;
        if (args.length == 3) {
            broker = args[0];
            username = args[1];
            password = args[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java QRetailer broker username password");
            return;
        }
        QRetailer retailer = new QRetailer(broker,username,password);
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
