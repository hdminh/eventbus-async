package server.demo;

import io.vertx.core.Vertx;
import server.demo.verticle.PrintOutVerticle;
import server.demo.verticle.SenderVerticle;
import server.demo.verticle.TimerVerticle;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TimerVerticle());
        vertx.deployVerticle(new PrintOutVerticle());
        vertx.deployVerticle(new SenderVerticle());
    }
}
