package com.cartlc.tracker.fresh.model.misc

class HashStringList : HashSet<String>() {
    fun set(text: String, selected: Boolean) {
        if (selected) {
            add(text)
        } else {
            remove(text)
        }
    }
}