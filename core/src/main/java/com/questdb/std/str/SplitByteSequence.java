/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2017 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.std.str;

import com.questdb.std.Mutable;
import com.questdb.std.ObjectFactory;

public class SplitByteSequence extends AbstractCharSequence implements ByteSequence, Mutable {
    public static final ObjectFactory<SplitByteSequence> FACTORY = SplitByteSequence::new;

    private ByteSequence lhs;
    private ByteSequence rhs;
    private int rl;
    private int split;

    @Override
    public byte byteAt(int index) {
        return (byte) charAt(index);
    }

    @Override
    public void clear() {
    }

    @Override
    public int length() {
        return split + rl;
    }

    @Override
    public char charAt(int index) {
        if (index < split) {
            return lhs.charAt(index);
        } else {
            return rhs.charAt(index - split);
        }
    }

    public ByteSequence of(ByteSequence lhs, ByteSequence rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.rl = rhs == null ? 0 : rhs.length();
        this.split = lhs == null ? 0 : lhs.length();
        return this;
    }
}