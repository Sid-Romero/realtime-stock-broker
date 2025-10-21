package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class ClientCourtier {
    private final static String EXCHANGE_NAME = "bourse_direct";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // echangeur
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String queueName = channel.queueDeclare().getQueue();

        String routingKey = "AAPL";
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

        System.out.println(" [*] Waiting for " + routingKey + " quotes...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            TitreBoursier titre = gson.fromJson(message, TitreBoursier.class);
            System.out.println(" [x] Received: " + titre);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
