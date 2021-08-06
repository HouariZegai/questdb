/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2020 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.std.str;

import io.questdb.std.Numbers;
import io.questdb.std.Sinkable;
import io.questdb.std.datetime.microtime.TimestampFormatUtils;

public interface CharSink {

    default CharSink encodeUtf8(CharSequence cs) {
        return encodeUtf8(cs, 0, cs.length());
    }

    default CharSink encodeUtf8(CharSequence cs, int lo, int hi) {
        int i = lo;
        while (i < hi) {
            char c = cs.charAt(i++);
            if (c < 128) {
                putUtf8Special(c);
            } else {
                i = putUtf8Internal(cs, hi, i, c);
            }
        }
        return this;
    }

    default CharSink encodeUtf8AndQuote(CharSequence cs) {
        put('\"').encodeUtf8(cs).put('\"');
        return this;
    }

    default CharSink fill(char c, int n) {
        for (int i = 0; i < n; i++) {
            put(c);
        }
        return this;
    }

    default void flush() {
    }

    char[] getDoubleDigitsBuffer();

    default CharSink put(CharSequence cs) {
        throw new UnsupportedOperationException();
    }

    default CharSink put(CharSequence cs, int lo, int hi) {
        for (int i = lo; i < hi; i++) {
            put(cs.charAt(i));
        }
        return this;
    }

    CharSink put(char c);

    default CharSink put(int value) {
        Numbers.append(this, value);
        return this;
    }

    default CharSink put(long value) {
        Numbers.append(this, value);
        return this;
    }

    default CharSink put(float value, int scale) {
        Numbers.append(this, value, scale);
        return this;
    }

    default CharSink put(double value) {
        Numbers.append(this, value);
        return this;
    }

    default CharSink put(double value, int scale) {
        Numbers.append(this, value, scale);
        return this;
    }

    default CharSink put(boolean value) {
        this.put(value ? "true" : "false");
        return this;
    }

    default CharSink put(Throwable e) {
        throw new UnsupportedOperationException();
    }

    default CharSink put(Sinkable sinkable) {
        sinkable.toSink(this);
        return this;
    }

    default CharSink put(char[] chars, int start, int len) {
        for (int i = 0; i < len; i++) {
            put(chars[i + start]);
        }
        return this;
    }

    default CharSink putISODate(long value) {
        TimestampFormatUtils.appendDateTimeUSec(this, value);
        return this;
    }

    default CharSink putISODateMillis(long value) {
        io.questdb.std.datetime.millitime.DateFormatUtils.appendDateTime(this, value);
        return this;
    }

    default CharSink putQuoted(CharSequence cs) {
        put('\"').put(cs).put('\"');
        return this;
    }

    default CharSink putUtf8(char c) {
        if (c < 128) {
            putUtf8Special(c);
        } else if (c < 2048) {
            put((char) (192 | c >> 6)).put((char) (128 | c & 63));
        } else if (Character.isSurrogate(c)) {
            put('?');
        } else {
            put((char) (224 | c >> 12)).put((char) (128 | c >> 6 & 63)).put((char) (128 | c & 63));
        }
        return this;
    }

    default int putUtf8Internal(CharSequence cs, int hi, int i, char c) {
        if (c < 2048) {
            put((char) (192 | c >> 6)).put((char) (128 | c & 63));
        } else if (Character.isSurrogate(c)) {
            i = encodeSurrogate(c, cs, i, hi);
        } else {
            put((char) (224 | c >> 12)).put((char) (128 | c >> 6 & 63)).put((char) (128 | c & 63));
        }
        return i;
    }

    default void putUtf8Special(char c) {
        put(c);
    }

    private int encodeSurrogate(char c, CharSequence in, int pos, int hi) {
        int dword;
        if (Character.isHighSurrogate(c)) {
            if (hi - pos < 1) {
                put('?');
                return pos;
            } else {
                char c2 = in.charAt(pos++);
                if (Character.isLowSurrogate(c2)) {
                    dword = Character.toCodePoint(c, c2);
                } else {
                    put('?');
                    return pos;
                }
            }
        } else if (Character.isLowSurrogate(c)) {
            put('?');
            return pos;
        } else {
            dword = c;
        }
        put((char) (240 | dword >> 18)).
                put((char) (128 | dword >> 12 & 63)).
                put((char) (128 | dword >> 6 & 63)).
                put((char) (128 | dword & 63));
        return pos;
    }
}
