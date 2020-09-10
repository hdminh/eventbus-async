package async.demo.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrintOutVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startFuture) {
        System.out.println("C start");
        vertx.eventBus().consumer("address.c", this::handler);
        startFuture.complete();
    }

    private void handler(Message<String> msg) {
        msg.reply("Reply from C");
        System.out.println("C reply");
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}