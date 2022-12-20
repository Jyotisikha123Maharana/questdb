/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2022 QuestDB
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

package io.questdb.griffin.engine.functions.cast;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlException;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.NumericFunction;
import io.questdb.griffin.engine.functions.UnaryFunction;
import io.questdb.std.*;
import io.questdb.std.str.CharSink;

public class CastStrToNumericFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "cast(Sx)";
    }

    @Override
    public Function newInstance(int position, ObjList<Function> args, IntList argPositions, CairoConfiguration configuration, SqlExecutionContext sqlExecutionContext) throws SqlException {
        return new Func(args.getQuick(0));
    }

    private static class Func extends NumericFunction implements UnaryFunction {
        private final Function arg;
        private final Long256Impl long256a = new Long256Impl();
        private final Long256Impl long256b = new Long256Impl();
        private final Long256Impl long256builder = new Long256Impl();

        public Func(Function arg) {
            this.arg = arg;
        }

        @Override
        public Function getArg() {
            return arg;
        }

        @Override
        public void getLong256(Record rec, CharSink sink) {
            final CharSequence value = arg.getStr(rec);
            if (value != null) {
                Numbers.parseLong256(value, value.length(), long256builder);
            }
        }

        @Override
        public Long256 getLong256A(Record rec) {
            final CharSequence value = arg.getStr(rec);
            if (value == null) {
                return Long256Impl.NULL_LONG256;
            }
            return Numbers.parseLong256(value, value.length(), long256a);
        }

        @Override
        public Long256 getLong256B(Record rec) {
            final CharSequence value = arg.getStr(rec);
            if (value == null) {
                return Long256Impl.NULL_LONG256;
            }
            return Numbers.parseLong256(value, value.length(), long256b);
        }
    }
}
    