package com.server.demo.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Getter
@Setter
public class RabbitServer extends AbstractVerticle {

    protected RabbitMQClient client;
    protected static final String SERVER_QUEUE = "server.queue";
    protected final Map<String, Handler<String>> handlers = new ConcurrentHashMap<>();
    protected String replyQueueName;

    @Override
    public void start(Promise<Void> startPromise) {
        RabbitMQOptions config = new RabbitMQOptions();
        config.setUser("guest");
        config.setPassword("guest");
        config.setHost("localhost");
        config.setPort(5672);

        client = RabbitMQClient.create(vertx, config);
        client.start(this::handleStart);
    }

    protected void handleStart(AsyncResult<Void> result) {
        if (result.succeeded()) {
            client.queueDeclare(SERVER_QUEUE, false, true, true, handle -> {
                if (handle.succeeded()) {
                    client.basicConsumer(SERVER_QUEUE, this::handleComsumer);
                    log.info("Queue Declare Succeeded!");
                } else {
                    log.error("Queue Declare Failed! Message: {}", handle.cause().getMessage());
                }
            });
        }
    }

    protected void handleComsumer(AsyncResult<RabbitMQConsumer> result) {
        if (result.failed()) {
            log.error("Create Consumer Failed! Message: {}", result.cause().getMessage());
        } else {
            log.info("Create Comsumer succeeded! Queue: {}", SERVER_QUEUE);
            result.result().handler(this::handleMessage);
        }
    }

    protected void handleMessage(RabbitMQMessage message) {
        String correlationId = message.properties().correlationId();
        replyQueueName = message.properties().replyTo();
        JsonObject bodyObj = message.body().toJsonObject();
        String value = bodyObj.getString("value");
        JsonObject replyObj = buildMessage(value, correlationId);

        publishMessageToQueue(client, replyObj, replyQueueName);

    }

    protected JsonObject buildMessage(String value, String correlationId) {
        return new JsonObject()
                .put("properties", buildProperties(correlationId))
                .put("body", buildBody(value));
    }

    private JsonObject buildBody(String value) {
        return new JsonObject()
                .put("value", value);
    }

    private JsonObject buildProperties(String correlationId) {
        return new JsonObject()
                .put("correlationId", correlationId)
                .put("replyTo", replyQueueName)
                .put("contentType", "application/json")
                .put("contentEncoding", "UTF-8");
    }

    private static void publishMessageToQueue(RabbitMQClient client, JsonObject message, String queueName) {
        client.basicPublish("", queueName, message,
                result -> handlePublishMessageToQueue(result, queueName));
    }

    private static void handlePublishMessageToQueue(AsyncResult<Void> result, String queueName) {
        if (result.succeeded()) {
            log.info("Message publish to {}", queueName);
        } else {
            log.error("Message publish to {} failed!", queueName);
        }
    }

}
