package com.cartlc.tracker.model.msg

import com.cartlc.tracker.model.misc.HashStringList

class StringBuilderWithComma {

    private val sbuf = StringBuilder()

    fun append(text: String?, prefix: String? = null): StringBuilderWithComma {
        if (text.isNullOrBlank()) {
            return this
        }
        if (sbuf.length > 0) {
            sbuf.append(", ")
        }
        if (prefix != null) {
            sbuf.append(prefix)
            sbuf.append("=")
        }
        sbuf.append(text)
        return this
    }

    fun append(value: Long, prefix: String? = null): StringBuilderWithComma {
        if (value <= 0) {
            return this
        }
        return append(value.toString(), prefix)
    }

    fun append(value: Int, prefix: String? = null): StringBuilderWithComma {
        if (value <= 0) {
            return this
        }
        return append(value.toString(), prefix)
    }

    fun append(value: Boolean, prefix: String? = null): StringBuilderWithComma {
        return append(value.toString(), prefix)
    }

    fun append(value: HashStringList, prefix: String? = null): StringBuilderWithComma {
        if (value.size <= 0) {
            return this
        }
        if (sbuf.length > 0) {
            sbuf.append(", ")
        }
        if (prefix != null) {
            sbuf.append(prefix)
            sbuf.append("=")
        }
        sbuf.append("[")
        var comma = false
        for (ele in value) {
            if (comma) {
                sbuf.append(", ")
            } else {
                comma = true
            }
            sbuf.append(ele)
        }
        sbuf.append("]")
        return this
    }

    override fun toString(): String {
        return sbuf.toString()
    }
}