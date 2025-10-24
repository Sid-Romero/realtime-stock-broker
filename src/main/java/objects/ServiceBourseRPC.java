package objects;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP;

import java.util.HashMap;
import java.util.Map;

public class ServiceBourseRPC {
    private static final String RPC_QUEUE_NAME = "rpc_bourse";
    private static final Map<String, TitreBoursier> titres = new HashMap<>();
    private static final Gson gson = new Gson();

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.queuePurge(RPC_QUEUE_NAME);
        channel.basicQos(1);

        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                String operation = delivery.getProperties().getHeaders().get("operation").toString();

                switch (operation) {
                    case "CREATE":
                        TitreBoursier nouveau = gson.fromJson(message, TitreBoursier.class);
                        titres.put(nouveau.getCode(), nouveau);
                        response = "Created " + nouveau.getCode();
                        break;
                    case "UPDATE":
                        TitreBoursier maj = gson.fromJson(message, TitreBoursier.class);
                        titres.put(maj.getCode(), maj);
                        response = "Updated " + maj.getCode();
                        break;
                    case "DELETE":
                        titres.remove(message);
                        response = "Deleted " + message;
                        break;
                    case "GET":
                        TitreBoursier titre = titres.get(message);
                        response = titre != null ? gson.toJson(titre) : "Not found";
                        break;
                    case "LIST":
                        response = gson.toJson(titres.keySet());
                        break;
                    default:
                        response = "Unknown operation: " + operation;
                }
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            } finally {
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
                        response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }
}
