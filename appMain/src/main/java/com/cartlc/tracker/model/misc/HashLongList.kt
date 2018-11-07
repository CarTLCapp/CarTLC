package com.cartlc.tracker.model.misc

import com.cartlc.tracker.model.table.TableString

class HashLongList(private val db: TableString): HashSet<Long>() {

    constructor(db: TableString, text: String): this(db) {
        unmash(text)
    }

    fun add(text: String) {
        add(db.add(text))
    }

    fun set(text: String, selected: Boolean) {
        val id = db.add(text)
        if (selected) {
            add(id)
        } else {
            remove(id)
        }
    }

    fun expand(): HashStringList {
        val list = HashStringList()
        for (ele in toList()) {
            db.query(ele)?.value?.let { list.add(it) }
        }
        return list
    }

    fun mash(): String {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in toList().sorted()) {
            if (comma) {
                sbuf.append(",")
            } else {
                comma = true
            }
            sbuf.append(ele)
        }
        return sbuf.toString()
    }

    fun unmash(text: String): HashLongList {
        clear()
        for (ele in text.split(",")) {
            add(ele.toLong())
        }
        return this
    }
}