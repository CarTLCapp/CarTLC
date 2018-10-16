package com.cartlc.tracker.model.flow

enum class Stage {
    LOGIN,
    PROJECT,
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
    NEW_PROJECT,
    VIEW_PROJECT,
    TRUCK,
    EQUIPMENT,
    ADD_EQUIPMENT,
    NOTES,
    STATUS,
    PICTURE_1,
    PICTURE_2,
    PICTURE_3,
    ADD_PICTURE,
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
