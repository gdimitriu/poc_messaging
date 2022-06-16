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

public class Subscriber implements IMqttMessageListener {
    public static int qos = 1;
    public static String server = "localhost";
    public static String port = "1883";
    public static String broker = "tcp://" + server + ":" + port;
    public static String clientId = "MQTT_subscriber_JAVA";
    public static String topic = "topic1";
    // For additional reliability, choose FilePersistence instead
    // this not affect how the message is persisted on broker
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client;

    public Subscriber() {

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

    public void listen(String topic) throws Exception {
        try {
            System.out.println("Subscribing to topic " + topic);

            MqttSubscription sub =
                    new MqttSubscription(topic, qos);

            IMqttToken token = client.subscribe(
                    new MqttSubscription[] { sub },
                    new IMqttMessageListener[] { this });
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String messageText = new String(mqttMessage.getPayload());
        System.out.println("Message received " + messageText + " on topic " + topic);
        MqttProperties props = mqttMessage.getProperties();
        String responseTopic = props.getResponseTopic();
        if (responseTopic != null) {
            System.out.println("Send back message on response topic " + responseTopic);
            String correlationData = new String(props.getCorrelationData());
            MqttMessage response = new MqttMessage();
            props = new MqttProperties();
            props.setCorrelationData(correlationData.getBytes());
            String payload = "Received message with correlation data " + correlationData;
            response.setPayload(payload.getBytes());
            response.setProperties(props);
            client.publish(responseTopic,response);
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

        Subscriber subscriber = new Subscriber();

        try {
            subscriber.connect();
            subscriber.listen(topic);
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
