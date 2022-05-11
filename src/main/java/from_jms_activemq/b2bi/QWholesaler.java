package from_jms_activemq.b2bi;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;

public class QWholesaler implements MessageListener {
    private TopicConnection tConnect = null;
    private TopicSession tSession = null;
    private TopicPublisher tPublisher = null;
    private QueueConnection qConnect = null;
    private QueueSession qSession = null;
    private Queue receiveQueue  = null;
    private Topic hotDealsTopic = null;
    private TemporaryTopic buyOrdersTopic = null;

    public QWholesaler(String broker, String username, String password) {
        try {
            TopicConnectionFactory tFactory = null;
            QueueConnectionFactory qFactory = null;
            InitialContext jndi = null;
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
            env.put("java.naming.provider.url", "tcp://ubuntu:61616?type=TOPIC_CF,tcp://ubuntu:61616?type=QUEUE_CF");
            env.put("topic.Hot Deals", "Hot Deals");
            env.put("queue.SampleQ1","SampleQ1");
            jndi = new InitialContext(env);
            tFactory = (TopicConnectionFactory) jndi.lookup("Topic" + broker);
            qFactory = (QueueConnectionFactory) jndi.lookup("Queue" + broker);
            tConnect = tFactory.createTopicConnection(username,password);
            qConnect = qFactory.createQueueConnection(username,password);
            tSession = tConnect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            qSession = qConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            hotDealsTopic = (Topic) jndi.lookup("Hot Deals");
            receiveQueue = (Queue) jndi.lookup("SampleQ1");
            tPublisher = tSession.createPublisher(hotDealsTopic);
            QueueReceiver qReceiver = qSession.createReceiver(receiveQueue);
            qReceiver.setMessageListener(this);
            qConnect.start();
            tConnect.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
    private void publishPriceQuotes(String dealDesc,String username, String itemDesc, float oldPrice, float newPrice) {
        try {
            StreamMessage message = tSession.createStreamMessage();
            message.writeString(dealDesc);
            message.writeString(itemDesc);
            message.writeFloat(oldPrice);
            message.writeFloat(newPrice);
            message.setStringProperty("Username",username);
            message.setStringProperty("itemDesc",itemDesc);
            message.setJMSReplyTo(receiveQueue);
            tPublisher.publish(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY,180000);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Order received - " + text + " from " + message.getJMSCorrelationID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void exit() {
        try {
            tConnect.close();
            qConnect.close();
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
            System.out.println("java QWholesaler broker username password");
            return;
        }
        QWholesaler wholesaler = new QWholesaler(broker,username,password);
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

}
