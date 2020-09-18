package com.common.demo.message;

import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public class RabbitMessage {
    private final String correlationId;
    private final String replyQueue;
    private final Object data;
    private final String service;
    private final String action;

    public JsonObject toJson() {
        return new JsonObject()
                .put("properties", buildProperties())
                .put("body", buildBody());
    }

    protected JsonObject buildBody() {
        return new JsonObject()
                .put("service", service)
                .put("action", action)
                .put("data", data);
    }

    protected JsonObject buildProperties() {
        return new JsonObject()
                .put("correlationId", correlationId)
                .put("replyTo", replyQueue)
                .put("contentType", "application/json")
                .put("contentEncoding", "UTF-8");
    }


}
