package com.cartlc.tracker.model.misc

class HashList : HashSet<String>() {
    fun set(text: String, selected: Boolean) {
        if (selected) {
            add(text)
        } else {
            remove(text)
        }
    }
}