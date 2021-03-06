/*
 * Copyright 2012 - 2016 Splice Machine, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.splicemachine.derby.vti.iapi;

import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.sql.execute.ExecRow;
import com.splicemachine.derby.iapi.sql.execute.SpliceOperation;
import com.splicemachine.derby.impl.sql.execute.operations.LocatedRow;
import com.splicemachine.derby.stream.iapi.DataSet;
import com.splicemachine.derby.stream.iapi.DataSetProcessor;
import com.splicemachine.derby.stream.iapi.OperationContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by jleach on 10/7/15.
 */
public interface DatasetProvider {
    /**
     *
     * Processing pipeline supporting in memory and spark datasets.
     *
     * @param dsp
     * @param execRow
     * @return
     * @throws StandardException
     */
     DataSet<LocatedRow> getDataSet(SpliceOperation op, DataSetProcessor dsp,ExecRow execRow) throws StandardException;

    /**
     *
     * Dynamic MetaData used to dynamically bind a function.
     *
     * @return
     */
    ResultSetMetaData getMetaData() throws SQLException;

    OperationContext getOperationContext();

}
