package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class CourtierAM {
    private final static String EXCHANGE_NAME = "bourse_direct";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        String queueName = channel.queueDeclare().getQueue();

        // Bind sur AAPL et MSFT
        channel.queueBind(queueName, EXCHANGE_NAME, "AAPL");
        channel.queueBind(queueName, EXCHANGE_NAME, "MSFT");

        System.out.println(" [*] CourtierAM waiting for AAPL and MSFT...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            TitreBoursier titre = gson.fromJson(message, TitreBoursier.class);
            System.out.println(" [CourtierAM] Received: " + titre);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
