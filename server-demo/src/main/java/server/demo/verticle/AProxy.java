package server.demo.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.client.WebClient;

public class AProxy extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        super.start();

        EventBus eb = vertx.eventBus();

        eb.consumer(SenderVerticle.class.getName(), mess -> {
            WebClient client = WebClient.create(vertx);
            client.getAbs("127.0.0.1/api/getSleepTime").send(httpResponseAsyncResult -> {
                mess.reply(httpResponseAsyncResult.result());
            });
        });
    }
}

