/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import play.libs.concurrent.*;
import java.util.concurrent.*;
import akka.actor.*;

import javax.inject.Inject;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class WorkerExecutionContext extends CustomExecutionContext {

    @javax.inject.Inject
    public WorkerExecutionContext(ActorSystem actorSystem) {
        // See application.conf for definition
        super(actorSystem, "small-worker-dispatcher");
    }

}