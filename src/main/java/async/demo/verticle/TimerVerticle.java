package async.demo.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimerVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startFuture) {
        System.out.println("B start");
        vertx.eventBus().consumer("address.b", this::handler);
        startFuture.complete();
    }

    private void handler(Message<String> msg) {
        vertx.setTimer(2000, h -> {
            msg.reply("Reply from B");
            System.out.println("B reply");
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}