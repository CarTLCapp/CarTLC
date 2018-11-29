/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.mock.model.flow

import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.Stage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TestFlow {

    var processedFlow: Flow? = null
    var processedAction: Action? = null

    @Before
    fun onBefore() {
        Flow.processStageEvent = { flow -> processedFlow = flow }
        Flow.processActionEvent = { action -> processedAction = action }
        processedFlow = null
        processedAction = null
    }

    @Test
    fun `verify next,prev,center buttons`() {

        for (stage in Stage.values()) {
            val flow = Flow.from(stage)
            if (flow != null) {
                if (flow.hasNext) {
                    flow.next()
                    if (flow.nextAction != null) {
                        assertEquals(flow.nextAction, processedAction)
                    } else {
                        assertEquals(flow.nextStage, processedFlow?.stage)
                    }
                }
                if (flow.hasPrev) {
                    flow.prev()
                    if (flow.prevAction != null) {
                        assertEquals(flow.prevAction, processedAction)
                    } else {
                        assertEquals(flow.prevStage, processedFlow?.stage)
                    }
                }
                if (flow.hasCenter) {
                    flow.center()
                    if (flow.centerAction != null) {
                        assertEquals(flow.centerAction, processedAction)
                    } else {
                        assertEquals(flow.centerStage, processedFlow?.stage)
                    }
                }
            }
        }
    }

}