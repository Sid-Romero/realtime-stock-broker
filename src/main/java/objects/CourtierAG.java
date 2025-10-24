package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class CourtierAG {
    private final static String EXCHANGE_NAME = "bourse_direct";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");


        String queueName = channel.queueDeclare().getQueue();

        // Bind on AAPL et GOOG
        channel.queueBind(queueName, EXCHANGE_NAME, "AAPL");
        channel.queueBind(queueName, EXCHANGE_NAME, "GOOG");

        System.out.println(" [*] CourtierAG waiting for AAPL and GOOG...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            TitreBoursier titre = gson.fromJson(message, TitreBoursier.class);
            System.out.println(" [CourtierAG] Received: " + titre);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}
