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

package io.questdb.cairo;

import io.questdb.cairo.vm.Vm;
import io.questdb.cairo.vm.api.MemoryMR;
import io.questdb.std.*;
import io.questdb.std.str.Path;

import java.io.Closeable;

import static io.questdb.cairo.TableUtils.*;

public class WalReaderEvents implements Closeable {
    private final FilesFacade ff;
    private final MemoryMR eventMem;
    private final WalEventCursor eventCursor;

    public WalReaderEvents(FilesFacade ff) {
        this.ff = ff;
        eventMem = Vm.getMRInstance();
        eventCursor = new WalEventCursor(eventMem);
    }

    @Override
    public void close() {
        // WalReaderEvents is re-usable after close, don't assign nulls
        Misc.free(eventMem);
    }

    public WalEventCursor of(Path path, long segmentId, int expectedVersion) {
        final int pathLen = path.length();
        try {
            path.slash().put(segmentId).concat(TableUtils.EVENT_FILE_NAME).$();
            eventMem.smallFile(ff, path, MemoryTag.MMAP_TABLE_WAL_READER);

            // minimum we need is WAL_FORMAT_VERSION (int) and END_OF_EVENTS (long)
            checkMemSize(eventMem, Integer.BYTES + Long.BYTES);
            validateMetaVersion(eventMem, 0, expectedVersion);
            eventCursor.reset();
        } catch (Throwable e) {
            close();
            throw e;
        } finally {
            path.trimTo(pathLen);
        }
        return eventCursor;
    }
}