package from_jms_activemq.b2bi;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

public class WholesalerReconnect implements MessageListener,ExceptionListener {
    private TopicConnection connect = null;
    private TopicSession pubSession = null;
    private TopicSession subSession = null;
    private TopicPublisher publisher = null;
    private TopicSubscriber subscriber = null;
    private Topic hotDealsTopic = null;
    private TemporaryTopic buyOrdersTopic = null;
    private String mBroker = null;
    private String mUserName = null;
    private String mPassword = null;

    public WholesalerReconnect(String broker, String userName, String password) {
        mBroker = broker;
        mUserName = userName;
        mPassword = password;
        establishConnection(broker, userName, password);
    }

    private void establishConnection(String broker, String userName, String password) {
        try {
            Properties env = new Properties();
            env.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            env.put("java.naming.provider.url","tcp://ubuntu:61616?type=TOPIC_CF");
            env.put("topic.Hot Deals","Hot Deals");
            InitialContext jndi = new InitialContext(env);
            TopicConnectionFactory factory = (TopicConnectionFactory) jndi.lookup(broker);
            while(connect == null) {
                try {
                    connect = factory.createTopicConnection(userName, password);
                    System.out.println("\nConnection established.");
                } catch (JMSException e) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        continue;
                    }
                }
            }
            pubSession = connect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            subSession = connect.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
            hotDealsTopic = (Topic) jndi.lookup("Hot Deals");
            publisher = pubSession.createPublisher(hotDealsTopic);
            buyOrdersTopic = subSession.createTemporaryTopic();
            subscriber = subSession.createSubscriber(buyOrdersTopic);
            subscriber.setMessageListener(this);
            connect.start();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void publishPriceQuotes(String dealDesc, String username, String itemDesc, float oldPrice, float newPrice) {
        try {
            StreamMessage message = pubSession.createStreamMessage();
            message.writeString(dealDesc);
            message.writeString(itemDesc);
            message.writeFloat(oldPrice);
            message.writeFloat(newPrice);
            message.setStringProperty("Username",username);
            message.setStringProperty("Itemdesc",itemDesc);
            message.setJMSReplyTo(buyOrdersTopic);
            publisher.publish(message,DeliveryMode.PERSISTENT,Message.DEFAULT_PRIORITY,1800000);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("\nOrder received - " + text + " from " + message.getJMSCorrelationID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void exit() {
        try {
            connect.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    public static void main(String...argv) {
        String broker, username, password;
        if (argv.length == 3) {
            broker = argv[0];
            username = argv[1];
            password = argv[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java Wholesaler broker username password");
            return;
        }
        WholesalerReconnect wholesaler = new WholesalerReconnect(broker,username,password);
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter: Item, Old Price, New Price");
            System.out.println("\ne.g.,Bowling Shoes, 100.00, 55.00");
            while(true) {
                String dealDesc = stdin.readLine();
                if (dealDesc != null && dealDesc.length() > 0) {
                    StringTokenizer tokenizer = new StringTokenizer(dealDesc,",");
                    String itemDesc = tokenizer.nextToken();
                    String temp = tokenizer.nextToken();
                    float oldPrice = Float.valueOf(temp.trim()).floatValue();
                    temp = tokenizer.nextToken();
                    float newPrice = Float.valueOf(temp.trim()).floatValue();
                    wholesaler.publishPriceQuotes(dealDesc, username, itemDesc,oldPrice,newPrice);
                } else {
                    wholesaler.exit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(JMSException e) {
        System.err.println("\n\nThere is a problem with the connection.");
        System.err.println(" JMSException: " + e.getMessage());
        connect = null;
        establishConnection(mBroker,mUserName,mPassword);
    }
}
