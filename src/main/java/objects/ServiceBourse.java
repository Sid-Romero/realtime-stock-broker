package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ServiceBourse {
    private final static String EXCHANGE_NAME = "bourse_direct";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {

            // échangeur de type direct
           channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            Gson gson = new Gson();

            TitreBoursier apple = new TitreBoursier("AAPL", "Apple Inc.", 182.91, "USD");
            TitreBoursier google = new TitreBoursier("GOOG", "Alphabet Inc.", 135.12, "USD");
            TitreBoursier microsoft = new TitreBoursier("MSFT", "Microsoft Corp.", 319.67, "USD");

            // clé de routage
            channel.basicPublish(EXCHANGE_NAME, apple.getCode(), null,
                    gson.toJson(apple).getBytes("UTF-8"));
            channel.basicPublish(EXCHANGE_NAME, google.getCode(), null,
                    gson.toJson(google).getBytes("UTF-8"));
            channel.basicPublish(EXCHANGE_NAME, microsoft.getCode(), null,
                    gson.toJson(microsoft).getBytes("UTF-8"));

            System.out.println(" [x] Sent AAPL, GOOG, MSFT");
        }
    }
}
