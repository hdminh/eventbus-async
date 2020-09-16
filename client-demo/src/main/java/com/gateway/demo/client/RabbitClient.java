package com.gateway.demo.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.log4j.Log4j2;

import java.util.UUID;

@Log4j2
public class RabbitClient extends AbstractVerticle {
    protected RabbitMQClient client;
    private static final String SERVER_QUEUE = "server.queue";
    private static final String CLIENT_QUEUE = "client.queue";

    @Override
    public void start(Promise<Void> startPromise) {
        RabbitMQOptions config = new RabbitMQOptions();
        config.setUser("guest");
        config.setPassword("guest");
        config.setHost("localhost");
        config.setPort(5672);

        client = RabbitMQClient.create(vertx, config);
        client.start(this::handleStart);
        
        vertx.setTimer(1000, handler -> sendMessage("minh dz"));
    }

    public void sendMessage(String message) {
        String correlationId = UUID.randomUUID().toString();
        publishMessageToQueue(buildMessage(message, correlationId));
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
                .put("replyTo", CLIENT_QUEUE)
                .put("contentType", "application/json")
                .put("contentEncoding", "UTF-8");
    }

    private void handleMessage(RabbitMQMessage message) {
        JsonObject msgObj = message.body().toJsonObject();
        log.info(msgObj);
    }

    private void publishMessageToQueue(JsonObject message) {
        client.basicPublish("", SERVER_QUEUE, message, this::handlePublishMessage);
    }

    private void handlePublishMessage(AsyncResult<Void> result) {
        if (result.succeeded()) {
            log.info("Message Published!");
        } else {
            log.error("Message Publish failed!");
        }
    }
}
