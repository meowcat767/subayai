/*
 * Copyright (c) 2015, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package space.meowcats.subayai.nodes.util;

import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.TruffleString;
import space.meowcats.subayai.SubayaiLanguage;
import space.meowcats.subayai.nodes.SubayaiTypes;
import space.meowcats.subayai.runtime.SubayaiBigInteger;
import space.meowcats.subayai.runtime.SubayaiFunction;
import space.meowcats.subayai.runtime.SubayaiNull;
import space.meowcats.subayai.runtime.SubayaiStrings;

/**
 * The node to normalize any value to an Subayai value. This is useful to reduce the number of values
 * expression nodes need to expect.
 */
@TypeSystemReference(SubayaiTypes.class)
@GenerateUncached
@GenerateInline
@GenerateCached(false)
public abstract class SubayaiToTruffleStringNode extends Node {

    static final int LIMIT = 5;

    private static final TruffleString TRUE = SubayaiStrings.constant("true");
    private static final TruffleString FALSE = SubayaiStrings.constant("false");
    private static final TruffleString FOREIGN_OBJECT = SubayaiStrings.constant("[foreign object]");

    public abstract TruffleString execute(Node node, Object value);

    @Specialization
    public static TruffleString fromNull(@SuppressWarnings("unused") SubayaiNull value) {
        return SubayaiStrings.NULL;
    }

    @Specialization
    protected static TruffleString fromString(String value,
                    // TruffleString nodes cannot be inlined yet
                    @Shared("fromJava") @Cached(inline = false) TruffleString.FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.execute(value, SubayaiLanguage.STRING_ENCODING);
    }

    @Specialization
    public static TruffleString fromTruffleString(TruffleString value) {
        return value;
    }

    @Specialization
    public static TruffleString fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Specialization
    @TruffleBoundary
    protected static TruffleString fromLong(long value,
                    @Shared("fromLong") @Cached(inline = false) TruffleString.FromLongNode fromLongNode) {
        return fromLongNode.execute(value, SubayaiLanguage.STRING_ENCODING, true);
    }

    @Specialization
    @TruffleBoundary
    protected static TruffleString fromBigNumber(SubayaiBigInteger value,
                                                 @Shared("fromJava") @Cached(inline = false) TruffleString.FromJavaStringNode fromJavaStringNode) {
        return fromJavaStringNode.execute(value.toString(), SubayaiLanguage.STRING_ENCODING);
    }

    @Specialization
    public static TruffleString fromFunction(SubayaiFunction value) {
        return value.getName();
    }

    @Specialization(limit = "LIMIT")
    public static TruffleString fromInterop(Object value,
                    @CachedLibrary("value") InteropLibrary interop,
                    @Shared("fromLong") @Cached(inline = false) TruffleString.FromLongNode fromLongNode,
                    @Shared("fromJava") @Cached(inline = false) TruffleString.FromJavaStringNode fromJavaStringNode) {
        try {
            if (interop.fitsInLong(value)) {
                return fromLongNode.execute(interop.asLong(value), SubayaiLanguage.STRING_ENCODING, true);
            } else if (interop.isString(value)) {
                return fromJavaStringNode.execute(interop.asString(value), SubayaiLanguage.STRING_ENCODING);
            } else if (interop.isNumber(value) && value instanceof SubayaiBigInteger) {
                return fromJavaStringNode.execute(bigNumberToString((SubayaiBigInteger) value), SubayaiLanguage.STRING_ENCODING);
            } else if (interop.isNull(value)) {
                return SubayaiStrings.NULL_LC;
            } else {
                return FOREIGN_OBJECT;
            }
        } catch (UnsupportedMessageException e) {
            throw shouldNotReachHere(e);
        }
    }

    @TruffleBoundary
    private static String bigNumberToString(SubayaiBigInteger value) {
        return value.toString();
    }
}
