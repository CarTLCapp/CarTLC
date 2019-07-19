package com.cartlc.tracker.fresh.model.flow

enum class VehicleStage {
    STAGE_1,
    STAGE_2,
    STAGE_3,
    STAGE_4,
    STAGE_5,
    STAGE_6;

    companion object {

        fun from(ord: Int): VehicleStage {
            for (s in VehicleStage.values()) {
                if (s.ordinal == ord) {
                    return s
                }
            }
            return STAGE_1
        }
    }

    fun advance(): VehicleStage {
        return from(ordinal + 1)
    }

    fun previous(): VehicleStage {
        return from(ordinal - 1)
    }
}