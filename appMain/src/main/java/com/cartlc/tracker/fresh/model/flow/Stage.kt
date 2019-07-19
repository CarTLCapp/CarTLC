package com.cartlc.tracker.fresh.model.flow

enum class Stage {
    LOGIN,
    ROOT_PROJECT,
    SUB_PROJECT,
    COMPANY,
    ADD_COMPANY,
    STATE,
    ADD_STATE,
    CITY,
    ADD_CITY,
    STREET,
    ADD_STREET,
    CONFIRM_ADDRESS,
    CURRENT_PROJECT,
    TRUCK,
    EQUIPMENT,
    ADD_EQUIPMENT,
    NOTES,
    STATUS,
    PICTURE_1,
    PICTURE_2,
    PICTURE_3,
    CONFIRM;

    companion object {

        fun from(ord: Int): Stage {
            for (s in values()) {
                if (s.ordinal == ord) {
                    return s
                }
            }
            return LOGIN
        }
    }
}
