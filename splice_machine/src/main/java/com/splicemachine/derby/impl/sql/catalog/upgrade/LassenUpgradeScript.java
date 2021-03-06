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

package com.splicemachine.derby.impl.sql.catalog.upgrade;

import com.splicemachine.db.catalog.UUID;
import com.splicemachine.db.catalog.types.DefaultInfoImpl;
import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.sql.dictionary.ColumnDescriptor;
import com.splicemachine.db.iapi.sql.dictionary.ColumnDescriptorList;
import com.splicemachine.db.iapi.sql.dictionary.DataDictionary;
import com.splicemachine.db.iapi.sql.dictionary.TableDescriptor;
import com.splicemachine.db.iapi.store.access.TransactionController;
import com.splicemachine.db.iapi.types.DataTypeDescriptor;
import com.splicemachine.db.iapi.types.SQLBoolean;
import com.splicemachine.derby.impl.sql.catalog.SpliceDataDictionary;
import com.splicemachine.pipeline.ErrorState;

import java.sql.Types;

/**
 * @author Scott Fines
 *         Date: 2/25/15
 */
public class LassenUpgradeScript extends UpgradeScriptBase {
    public LassenUpgradeScript(SpliceDataDictionary sdd, TransactionController tc) {
        super(sdd, tc);
    }

    @Override
    protected void upgradeSystemTables() throws StandardException {
        super.upgradeSystemTables();
        addStatsColumnToSysColumns(tc);

        sdd.createStatisticsTables(tc);
        sdd.createLassenTables(tc);
//        SpliceUtilities.createRestoreTableIfNecessary();
    }


    /* ****************************************************************************************************************/
    /*private helper methods*/
    private void addStatsColumnToSysColumns(TransactionController tc) throws StandardException {
        //add the syscolumns descriptor
        SQLBoolean template_column = new SQLBoolean();
        DataTypeDescriptor dtd = DataTypeDescriptor.getBuiltInDataTypeDescriptor(Types.BOOLEAN);
        tc.addColumnToConglomerate(sdd.getSYSCOLUMNSHeapConglomerateNumber(),9, template_column,dtd.getCollationType());

        TableDescriptor sysColumns = sdd.getTableDescriptor("SYSCOLUMNS",sdd.getSystemSchemaDescriptor(),tc);
        UUID defaultUuid = sdd.getUUIDFactory().createUUID();

        addCollectStatsColumn(tc,template_column,dtd,sysColumns,defaultUuid);
    }

    private void addCollectStatsColumn(TransactionController tc,
                                       SQLBoolean template_column,
                                       DataTypeDescriptor dtd,
                                       TableDescriptor sysColumns,
                                       UUID defaultUuid) throws StandardException{
        ColumnDescriptorList columnDescriptorList=sysColumns.getColumnDescriptorList();
        for(ColumnDescriptor cd:columnDescriptorList){
            /*
             * Quick check: If we can already see the column, no ned to add it in. This may happen
             * if another region server is starting simultaneously to us, and managed to get here first
             */
            if("COLLECTSTATS".equalsIgnoreCase(cd.getColumnName())){
                return; //no need to add the collect stats column
            }
        }

        ColumnDescriptor cd = new ColumnDescriptor("COLLECTSTATS",10,10,
                dtd,
                template_column,
                new DefaultInfoImpl(false,null,null),
                sysColumns,
                defaultUuid,
                0,
                0,10);

        try{
            /*
             * There is a cluster race condition here; when multiple RegionServers are starting up, one
             * may get here before the other, resulting in both attempting to create the same column. Our transaction
             * system (and the internal DD consistency controls) should prevent that, causing this to throw a
             * "LANG_OBJECT_ALREADY_EXISTS_IN_OBJECT" error back at us. This really is just saying that we
             * tried to create the same column twice. Since the column already exists and that's what we care about,
             * we are just as happy to get the error and return here as we would be if the call succeeded.
             *
             * Of course, if we get a goofy error that doesn't match up with what we expect, we should report that.
             */
            sdd.addDescriptor(cd,sysColumns,DataDictionary.SYSCOLUMNS_CATALOG_NUM,false,tc);
        }catch(StandardException se){
            if(ErrorState.LANG_OBJECT_ALREADY_EXISTS_IN_OBJECT.getSqlState().equals(se.getSQLState())){
                /*
                 * This occurred because another region server already added it in, no need to worry about it then.
                 * Just add it in to the CDL to make sure that it's present.
                 */
                columnDescriptorList.add(cd);
                return;
            }else throw se;
        }

        columnDescriptorList.add(cd);
        sdd.updateSYSCOLPERMSforAddColumnToUserTable(sysColumns.getUUID(), tc);
    }
}
