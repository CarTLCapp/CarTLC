/*
 * *
 *   * Copyright 2019, FleetTLC. All rights reserved
 *
 */
package models.flow;

import java.util.ArrayList;
import java.util.List;

public class Collect {

    static final int LINE_LENGTH = 80;
    static ArrayList<String> mCommonSet = new ArrayList<String>();
    static int mBitCount;
    static int mBitLen;

    public static List<FlowElement> compute(List<FlowElement> elements) {
        mCommonSet.clear();
        ArrayList<String> strings = new ArrayList<String>();
        for (FlowElement element : elements) {
            for (Bit bit : element.getBits()) {
                if (bit.detail != null) {
                    strings.add(bit.detail);
                }
            }
        }
        for (int i = 0; i < strings.size(); i++) {
            for (int j = i+1; j < strings.size(); j++) {
                String str0 = strings.get(i);
                String str1 = strings.get(j);
                int matchPos = match(str0, str1);
                if (matchPos > 0) {
                    mCommonSet.add(str0.substring(0, matchPos));
                }
            }
        }
        return elements;
    }

    private static int match(String alpha, String beta) {
        int pos;
        for (pos = 0; pos < alpha.length() && pos < beta.length(); pos++) {
            if (alpha.charAt(pos) != beta.charAt(pos)) {
                break;
            }
        }
        if (pos <= 1 || pos == alpha.length() || pos == beta.length()) {
            return 0;
        }
        if (alpha.charAt(pos-1) != ' ' || beta.charAt(pos-1) != ' ') {
            return 0;
        }
        return pos-1;
    }

    public static List<Bit> count(List<Bit> bits) {
        mBitCount = bits.size();
        if (mBitCount > 0) {
            mBitLen = LINE_LENGTH / mBitCount;
        } else {
            mBitLen = LINE_LENGTH;
        }
        return bits;
    }

    public static String reduce(String value) {
        String found = null;
        for (String compare : mCommonSet) {
            if (value.startsWith(compare)) {
                if (found == null || found.length() < compare.length()) {
                    found = compare;
                }
            }
        }
        if (found != null) {
            return value.substring(found.length(), end(found, value)).trim();
        }
        return value;
    }

    private static int end(String found, String value) {
        int endPos = found.length() + mBitLen;
        if (endPos > value.length()) {
            endPos = value.length();
        }
        return endPos;
    }

}