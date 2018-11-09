package com.cartlc.tracker.model.misc

import com.cartlc.tracker.model.table.TableString

class HashLongList(private val db: TableString) : HashSet<Long>() {

    constructor(db: TableString, text: String) : this(db) {
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

    /**
     * Return a comma separated list of the numbers.
     */
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

    /**
     * Return a comma separated list of strings, if there
     * are not server ids associated with each of the strings.
     *
     * Otherwise if each string DOES have a server id, then
     * return a comma separated list of those ids.
     */
    fun serverMash(): String {
        val result = serverMashServerIds()
        if (result != null) {
            return result
        }
        return serverMashStrings()
    }

    private fun serverMashServerIds(): String? {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in toList()) {
            db.query(ele)?.let {
                if (it.serverId > 0) {
                    if (comma) {
                        sbuf.append(",")
                    } else {
                        comma = true
                    }
                    sbuf.append(it.serverId)
                } else {
                    return null
                }
            } ?: return null
        }
        return sbuf.toString()
    }

    private fun serverMashStrings(): String {
        val sbuf = StringBuilder()
        var comma = false
        for (ele in expand()) {
            if (comma) {
                sbuf.append(",")
            } else {
                comma = true
            }
            sbuf.append(ele)
        }
        return sbuf.toString()
    }


}