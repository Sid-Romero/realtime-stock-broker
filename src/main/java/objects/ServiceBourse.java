package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

public class ServiceBourse {
    private final static String EXCHANGE_NAME = "bourse_direct";
    private final Map<String, TitreBoursier> titres = new HashMap<>();
    private Connection connection;
    private Channel channel;
    private final Gson gson = new Gson();

    public ServiceBourse() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // title initialization
        titres.put("AAPL", new TitreBoursier("AAPL", "Apple Inc.", 224.47, "USD"));
        titres.put("GOOG", new TitreBoursier("GOOG", "Alphabet Inc.", 135.12, "USD"));
        titres.put("MSFT", new TitreBoursier("MSFT", "Microsoft Corp.", 319.67, "USD"));
        titres.put("AMZN", new TitreBoursier("AMZN", "Amazon.com Inc.", 128.50, "USD"));
        titres.put("TSLA", new TitreBoursier("TSLA", "Tesla Inc.", 210.23, "USD"));
        titres.put("NFLX", new TitreBoursier("NFLX", "Netflix Inc.", 401.87, "USD"));
        titres.put("META", new TitreBoursier("META", "Meta Platforms Inc.", 290.40, "USD"));
        titres.put("NVDA", new TitreBoursier("NVDA", "NVIDIA Corp.", 450.60, "USD"));
    }

    // broadcasting
    public void broadcastAll() throws Exception {
        for (TitreBoursier titre : titres.values()) {
            String message = gson.toJson(titre);
            channel.basicPublish(EXCHANGE_NAME, titre.getCode(), null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent: " + titre);
        }
    }

    public void close() throws Exception {
        channel.close();
        connection.close();
    }

    public static void main(String[] args) throws Exception {
        ServiceBourse service = new ServiceBourse();
    while (true) {
        service.broadcastAll();
        Thread.sleep(10_000); // 10 secondes
    }
        // service.close();
    }
}
