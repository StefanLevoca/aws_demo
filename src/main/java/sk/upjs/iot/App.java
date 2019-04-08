package sk.upjs.iot;

import com.amazonaws.services.iot.client.*;
import org.json.JSONObject;

public class App {

    public static final String folder = "";
    public static final String clientEndpoint = "<prefix>.iot.us-east-1.amazonaws.com";
    public static final String clientId = "";
    public static final String certificateFile = folder + "<certificate>-certificate.pem.crt";
    public static final String privateKeyFile = folder + "<certificate>-private.pem.key";

    public static final AWSIotQos qos = AWSIotQos.QOS0;
    public static final long timeout = 1000;


    public static void main(String[] args) {
        try {
            new App().run();
        } catch (AWSIotException e) {
            e.printStackTrace();
        }
    }

    public void run() throws AWSIotException {
        SampleUtil.KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
        AWSIotMqttClient client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
        client.connect();

        // SUBSCRIBE
        subscribe(client, "");

        // PUBLISH
        int value = generateValue();
        String payload = json(value);
        publish(client, "", payload);

    }

    private int generateValue() {
        return 0;
    }

    private String json(int value) {
        JSONObject o = new JSONObject();
        o.put("timestamp", System.currentTimeMillis());
        o.put("value", value);
        return o.toString();
    }

    private void publish(AWSIotMqttClient client, String topic, String payload) throws AWSIotException {
        PublishMessage message = new PublishMessage(topic, qos, payload);
        client.publish(message, timeout);
    }

    private void subscribe(AWSIotMqttClient client, String topic) throws AWSIotException {
        client.subscribe(new SubscribeTopic(topic, qos));
    }

    public class PublishMessage extends AWSIotMessage {
        public PublishMessage(String topic, AWSIotQos qos, String payload) {
            super(topic, qos, payload);
        }

        @Override
        public void onSuccess() {
            // called when message publishing succeeded
            String res = new String(payload);
            System.out.println("Publishing to " + topic + ": " + res);

        }

        @Override
        public void onFailure() {
            // called when message publishing failed
            System.out.println("Publish failed");
        }

        @Override
        public void onTimeout() {
            // called when message publishing timed out
            System.out.println("Publish timeout");
        }
    }

    public class SubscribeTopic extends AWSIotTopic {
        public SubscribeTopic(String topic, AWSIotQos qos) {
            super(topic, qos);
        }

        @Override
        public void onMessage(AWSIotMessage message) {
            // called when a message is received
            System.out.println("Received from " + message.getTopic() + ": " + message.getStringPayload());

        }
    }
}
