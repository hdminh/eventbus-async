package async.demo.verticle;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SenderVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/api/sync").handler(this::syncEvent);
        router.get("/api/async").handler(this::aSyncEvent);

        Promise promise = Promise.promise();
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);
        server.listen(config().getInteger("http.port", 8000),
                result -> {
                    if (result.succeeded()) {
                        log.info("server started at port 8000");
                        promise.complete();
                    } else {
                        promise.fail(result.cause());
                    }
                });
    }

    private void syncEvent(RoutingContext ctx){
        String msg = ctx.getBodyAsString();

        Promise promise = Promise.promise();

        AsyncResult asyncResult = callB(msg).future();
        if (asyncResult.succeeded()) {
            AsyncResult asyncC = callC(msg).future();
            if (asyncC.succeeded()){
                System.out.println("Done!");
            }
            promise.complete();
        }
        else {
            promise.fail(asyncResult.cause());
            System.out.println("Failed");
        }

    }

    public void aSyncEvent(RoutingContext ctx) {
        String msg = ctx.getBodyAsString();
        CompositeFuture.all(callB(msg).future(), callC(msg).future()).setHandler(h -> {
            if (h.succeeded()) {
                System.out.println("I'm done my task!");
            } else {
                System.out.println("I'm failed!");
            }
        });
    }


    private Promise callB(String msg){
        Promise promise = Promise.promise();
        vertx.eventBus().request("address.b", "Message from A to B", reply -> {
            if (reply.succeeded()){
                System.out.println("B done");
                promise.complete();
            } else {
                promise.fail(reply.cause());
            }
        });
        return promise;
    }

    private Promise callC(String msg){
        Promise promise = Promise.promise();
        vertx.eventBus().request("address.c", "Message from A to C", reply -> {
            if (reply.succeeded()){
                System.out.println("C done");
                promise.complete(reply.result());
            } else {
                promise.fail(reply.cause());
            }
        });
        return promise;
    }

    @Override
    public void stop(Future<Void> stopFuture) {
    }
}
