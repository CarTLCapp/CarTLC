package modules;

import play.libs.concurrent.*;
import java.util.concurrent.*;
import akka.actor.*;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class WorkerExecutionContext extends CustomExecutionContext {

    @javax.inject.Inject
    public WorkerExecutionContext(ActorSystem actorSystem) {
        // uses a custom thread pool defined in application.conf
        super(actorSystem, "custom.dispatcher");
    }

}