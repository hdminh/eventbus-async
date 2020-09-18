package com.gateway.demo.client;

import com.common.demo.message.RabbitMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class RabbitClient extends AbstractVerticle {

    private Map<String, Handler<JsonObject>> mapJobs = new ConcurrentHashMap<>();
    protected RabbitMQClient client;
    private static final String SERVER_QUEUE = "server.queue";
    private static final String CLIENT_QUEUE = "client.queue";

    @Override
    public void start(Promise<Void> promise) {

        RabbitMQOptions config = new RabbitMQOptions();
        config.setUser("guest");
        config.setPassword("guest");
        config.setHost("localhost");
        config.setPort(5672);

        client = RabbitMQClient.create(vertx, config);
        client.start(this::handleStart);

        vertx.eventBus().consumer("vertx.getAll", handler -> {
            sendMessage("a", "a", handler);
        });
    }

    private void sendMessage(String service, String action, Message<Object> handler) {
        String correlationId = UUID.randomUUID().toString();
        JsonObject message = RabbitMessage.builder()
                .correlationId(correlationId)
                .replyQueue(CLIENT_QUEUE)
                .service(service).action(action).build().toJson();
        publishMessageToQueue(message, handler::reply);
    }

    private void handleStart(AsyncResult<Void> result) {
        if (result.succeeded()) {
            log.info("Start {}", CLIENT_QUEUE);
            client.queueDeclare(CLIENT_QUEUE, false, true, true, handle -> {
                if (handle.succeeded()) {
                    client.basicConsumer(CLIENT_QUEUE, this::handleComsumer);
                    log.info("Queue {} Declare Succeeded!", CLIENT_QUEUE);
                } else {
                    log.error("Queue Declare Failed! Message: {}", handle.cause().getMessage());
                }
            });
        } else {
            log.error("Queue {} can not start!", CLIENT_QUEUE);
        }
    }

    private void handleComsumer(AsyncResult<RabbitMQConsumer> result) {
        if (result.failed()) {
            log.error("Create Consumer Failed! Message: {}", result.cause().getMessage());
        } else {
            log.info("Create Comsumer succeeded! Queue: {}", CLIENT_QUEUE);
            result.result().handler(this::handleMessage);
        }

    }

    private void handleMessage(RabbitMQMessage message) {
        String correlationId = message.properties().correlationId();
        if (mapJobs.containsKey(correlationId)) {
            mapJobs.remove(correlationId)
                    .handle(new JsonObject(message.body()));
        }
    }

    private void publishMessageToQueue(JsonObject message, Handler<JsonObject> callback) {
        String correlationId = message.getJsonObject("properties").getString("correlationId");
        client.basicPublish("", SERVER_QUEUE, message, handler -> handlePublishMessage(handler, correlationId, callback));
    }

    private void handlePublishMessage(AsyncResult<Void> result, String correlationId, Handler<JsonObject> callback) {
        if (result.failed()) {
            log.info("Message Published!");
            log.error("Message Publish failed!");
        } else {
            mapJobs.put(correlationId, callback);
        }
    }
}
