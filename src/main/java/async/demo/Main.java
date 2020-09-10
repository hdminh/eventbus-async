package async.demo;

import async.demo.verticle.PrintOutVerticle;
import async.demo.verticle.SenderVerticle;
import async.demo.verticle.TimerVerticle;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TimerVerticle());
        vertx.deployVerticle(new PrintOutVerticle());
        vertx.deployVerticle(new SenderVerticle());
    }
}
