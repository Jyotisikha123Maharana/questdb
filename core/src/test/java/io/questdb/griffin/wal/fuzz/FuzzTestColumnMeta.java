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

package io.questdb.griffin.wal.fuzz;

import io.questdb.cairo.GenericRecordMetadata;
import io.questdb.cairo.TableColumnMetadata;

public class FuzzTestColumnMeta extends GenericRecordMetadata {
    int liveColumnCount = 0;

    @Override
    public GenericRecordMetadata add(TableColumnMetadata meta) {
        columnNameIndexMap.put(meta.getName(), columnCount);
        columnMetadata.extendAndSet(columnCount, meta);
        columnCount++;
        if (meta.getType() > 0) {
            liveColumnCount++;
        }
        return this;
    }

    public int getLiveColumnCount() {
        return liveColumnCount;
    }

    public void rename(int columnIndex, String name, String newName) {
        columnMetadata.get(columnIndex).setName(newName);
        columnNameIndexMap.remove(name);
        columnNameIndexMap.put(newName, columnIndex);
    }
}