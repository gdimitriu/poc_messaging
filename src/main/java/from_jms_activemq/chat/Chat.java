package from_jms_activemq.chat;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class Chat implements javax.jms.MessageListener {
    private TopicSession pubSession;
    private TopicSession subSession;
    private TopicPublisher publisher;
    private TopicConnection connection;
    private String userName;
    public Chat(String topicName, String userName, String password) throws NamingException, JMSException {
        this.userName = userName;
        Properties env = new Properties();
        env.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        env.put("java.naming.provider.url","tcp://ubuntu:61616?type=TOPIC_CF");
        env.put("topic.topicQueue","topicQueue");
        InitialContext jndi = new InitialContext(env);
        //get the connection factory
        TopicConnectionFactory conFactory = (TopicConnectionFactory) jndi.lookup("TopicConnectionFactory");
        TopicConnection connection = conFactory.createTopicConnection(userName,password);
        //create jms session objects
        TopicSession pubSession = connection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
        TopicSession subSession = connection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
        Topic chatTopic = (Topic)jndi.lookup(topicName);
        TopicPublisher publisher = pubSession.createPublisher(chatTopic);
        TopicSubscriber subscriber = subSession.createSubscriber(chatTopic);
        subscriber.setMessageListener(this);
        set(connection,pubSession,subSession,publisher,userName);
        connection.start();
    }
    public void set(TopicConnection con, TopicSession pubSess,TopicSession subSess, TopicPublisher pub, String userName) {
        this.connection = con;
        this.pubSession = pubSess;
        this.publisher = pub;
        this.userName = userName;
    }
    @Override
    public void onMessage(Message message) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String text  = textMessage.getText();
                System.out.println(text);
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
    }
    public void writeMessage(String text) throws JMSException {
        TextMessage message = pubSession.createTextMessage();
        message.setText(userName + " : " + text);
        publisher.publish(message);
    }
    public void close() throws JMSException {
        connection.close();
    }
    public static void main(String...args) {
        try {
            if (args.length != 3) {
                System.out.println("Topic or username missing");
                return;
            }
            Chat chat = new Chat(args[0],args[1],args[2]);
            BufferedReader commandLine = new java.io.BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String s = commandLine.readLine();
                if (s.equalsIgnoreCase("exit")) {
                    chat.close();
                    System.exit(0);
                } else
                    chat.writeMessage(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
