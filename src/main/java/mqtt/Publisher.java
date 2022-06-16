package mqtt;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

public class Publisher {
    public static int qos = 1;
    public static String server = "localhost";
    public static String port = "1883";
    public static String broker = "tcp://" + server + ":" + port;
    public static String clientId = "MQTT_publisher_JAVA";
    public static String topic = "topic1";
    public static final String RESPONSE_TOPIC = "response";
    Object shutdownMonitor = new Object();
    // For additional reliability, choose FilePersistence instead
    // this not affect how the message is persisted on broker
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client;

    public Publisher() {

    }

    /**
     * this is need because we don't want to start later the thread after you receive a message
     * we should have this up and running before sending any messages
     * if you don't use response topic to send back messages this is not needed
     */
    class ResponseSubscriber extends Thread implements IMqttMessageListener {
        String topic;
        public ResponseSubscriber(String topic) {
            this.topic = topic;
        }
        public void run() {
            try {
                synchronized (shutdownMonitor) {
                    MqttSubscription sub = new MqttSubscription(topic, 0);
                    IMqttToken token = client.subscribe(new MqttSubscription[] { sub}, new IMqttMessageListener[]{this});
                    synchronized (this) {
                        this.notify();
                    }
                    shutdownMonitor.wait();
                }
                client.unsubscribe(topic);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Response subscriber thread terminated");
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            MqttProperties props = mqttMessage.getProperties();
            String correlationData = new String(props.getCorrelationData());
            System.out.println("Response received on " + topic +": " + mqttMessage.toString() +", correlation data" + correlationData);
        }
    }
    public void connect() throws MqttException {
        try {
            System.out.println("Start connecting to MQTT broker: " + broker);
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(false);
            client = new MqttClient(broker,clientId, persistence);
            client.connect(connOpts);
            System.out.println("End Connecting.");
        } catch (MqttException e) {
            System.out.println("reason=" + e.getReasonCode());
            System.out.println("message=" + e.getMessage());
            System.out.println("localized message=" + e.getLocalizedMessage());
            System.out.println("cause=" + e.getCause());
            e.printStackTrace();
            throw e;
        }
    }

    public void disconnect() throws MqttException {
        if (client.isConnected())
            client.disconnect();
        System.out.println("Client disconnected");
    }

    public void startResponseListener() {
        try {
            ResponseSubscriber responseSubscriber = new ResponseSubscriber(RESPONSE_TOPIC);
            responseSubscriber.start();
            //wait for the thread to start
            synchronized (responseSubscriber) {
                responseSubscriber.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopResponseListener() {
        try {
            synchronized (shutdownMonitor) {
                shutdownMonitor.notify();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessages() {
        try {
            System.out.println("Publishing to topic " + topic);
            MqttMessage message = new MqttMessage();
            for (int i = 0 ; i < 10; i++) {
                int messageId = i+1;
                String content = "Message nr " + messageId;
                message.setQos(qos);
                message.setPayload(content.getBytes());
                message.setRetained(false);
                MqttProperties props = new MqttProperties();
                props.setResponseTopic(RESPONSE_TOPIC);
                props.setCorrelationData((""+messageId).getBytes());
                message.setProperties(props);
                System.out.println("Sending now the message " + message.toString() + " to topic " + topic);
                client.publish(topic,message);
                Thread.sleep(100);
            }
            synchronized (shutdownMonitor) {
                shutdownMonitor.notify();
            }
        } catch (MqttException e) {
            System.out.println("reason=" + e.getReasonCode());
            System.out.println("message=" + e.getMessage());
            System.out.println("localized message=" + e.getLocalizedMessage());
            System.out.println("cause=" + e.getCause());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        if ( args.length == 1) {
            topic = args[0];
        }
        else {
            for ( int i = 0; i < args.length; i++ ) {
                switch ( args[i] ) {
                    case "server":
                        server = args[++i];
                        break;
                    case "port":
                        port = args[++i];
                        break;
                    case "topic":
                        topic = args[++i];
                        break;
                }
            }
        }
        broker = "tcp://"+server+":"+port;
        try {
            Publisher sender = new Publisher();

            sender.connect();
            sender.startResponseListener();
            sender.sendMessages();

            Thread.sleep(1000);

            sender.stopResponseListener();
            sender.disconnect();
            System.exit(0);

        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
