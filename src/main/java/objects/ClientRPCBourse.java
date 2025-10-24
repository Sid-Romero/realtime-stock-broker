package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ClientRPCBourse implements AutoCloseable {
    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_bourse";
    private Gson gson = new Gson();

    public ClientRPCBourse() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public String call(String operation, String message) throws Exception {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = channel.queueDeclare().getQueue();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .headers(Map.of("operation", operation))
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        final CompletableFuture<String> response = new CompletableFuture<>();

        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {});

        String result = response.get();
        channel.basicCancel(ctag);
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }

    public static void main(String[] args) throws Exception {
        try (ClientRPCBourse client = new ClientRPCBourse()) {
            // example : create a title
            TitreBoursier apple = new TitreBoursier("AAPL", "Apple Inc.", 200.0, "USD");
            String res = client.call("CREATE", new Gson().toJson(apple));
            System.out.println(res);

            // list titles
            res = client.call("LIST", "");
            System.out.println(res);

            // get a title
            res = client.call("GET", "AAPL");
            System.out.println(res);
        }
    }
}
