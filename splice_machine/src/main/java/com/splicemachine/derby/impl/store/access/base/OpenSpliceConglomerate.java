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

package com.splicemachine.derby.impl.store.access.base;

import com.splicemachine.derby.impl.store.access.BaseSpliceTransaction;
import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.store.access.DynamicCompiledOpenConglomInfo;
import com.splicemachine.db.iapi.store.access.RowUtil;
import com.splicemachine.db.iapi.store.access.StaticCompiledOpenConglomInfo;
import com.splicemachine.db.iapi.store.access.conglomerate.Conglomerate;
import com.splicemachine.db.iapi.store.access.conglomerate.TransactionManager;
import com.splicemachine.db.iapi.store.raw.Transaction;
import com.splicemachine.db.iapi.types.DataValueDescriptor;
import com.splicemachine.db.iapi.types.RowLocation;
import com.splicemachine.derby.impl.store.access.btree.IndexConglomerate;
import com.splicemachine.derby.impl.store.access.hbase.HBaseRowLocation;
import java.util.Arrays;

/**
*
* This class maintains the key session items for a conglomerate.  This is usually passed into the Controllor (inserts/updates/deletes)
* or Scan Manager (Result Sets).
*
**/

public class OpenSpliceConglomerate  {
	protected SpliceConglomerate conglomerate;
	protected TransactionManager transactionManager;
	protected Transaction transaction;
	protected StaticCompiledOpenConglomInfo staticCompiledOpenConglomInfo;
	protected DynamicCompiledOpenConglomInfo dynamicCompiledOpenConglomInfo;
	protected boolean hold;	
	protected DataValueDescriptor[] rowTemplate = null;
	
	public OpenSpliceConglomerate(TransactionManager transactionManager,
                                  Transaction transaction,
                                  boolean hold,
                                  StaticCompiledOpenConglomInfo staticCompiledOpenConglomInfo,
                                  DynamicCompiledOpenConglomInfo dynamicCompiledOpenConglomInfo,
                                  SpliceConglomerate conglomerate) {
		this.transactionManager = transactionManager;
		this.transaction = transaction;
		try {
			((BaseSpliceTransaction)transaction).setActiveState(false, false, null);
		} catch (Exception e) {
        throw new RuntimeException(e);
		}
		this.hold = hold;
		this.staticCompiledOpenConglomInfo = staticCompiledOpenConglomInfo;
		this.dynamicCompiledOpenConglomInfo = dynamicCompiledOpenConglomInfo;
		this.conglomerate = conglomerate;
	}
    
    public int[] getFormatIds() {
        return conglomerate.getFormat_ids();
    }

    public int[] getColumnOrdering() {
        return conglomerate.getColumnOrdering();
    }


    public int[] getCollationIds() {
        return conglomerate.getCollation_ids();
    }

    public boolean[] getAscDescInfo() {
    	return conglomerate.getAscDescInfo();
    }
    
    public long getContainerID() {
    	return conglomerate.getContainerid();
    }
    
    
    /**
     * Return an "empty" row location object of the correct type.
     * <p>
     *
	 * @return The empty Rowlocation.
     *
	 * @exception  StandardException  Standard exception policy.
     **/
	public RowLocation newRowLocationTemplate() throws StandardException {
		return new HBaseRowLocation();
	}
	public Conglomerate getConglomerate() {
		return this.conglomerate;
	}
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public StaticCompiledOpenConglomInfo getStaticCompiledOpenConglomInfo() {
		return staticCompiledOpenConglomInfo;
	}

	public DynamicCompiledOpenConglomInfo getDynamicCompiledOpenConglomInfo() {
		return dynamicCompiledOpenConglomInfo;
	}

	public boolean isHold() {
		return hold;
	}

	public DataValueDescriptor[] cloneRowTemplate() throws StandardException {
		if (rowTemplate == null)
			rowTemplate = RowUtil.newTemplate(getTransaction().getDataValueFactory(), null, getFormatIds(), getCollationIds());
		return(RowUtil.newRowFromTemplate(rowTemplate));
	}
	
	public long getIndexConglomerate() {
		return ((IndexConglomerate)this.conglomerate).baseConglomerateId;
	}

	@Override
	public String toString() {
		try {
			return String.format("OpenSpliceConglomerate {conglomerate=%s, rowTemplate=%s}",conglomerate,Arrays.toString(cloneRowTemplate()));
		} catch (StandardException e) {
			e.printStackTrace();
			return String.format("OpenSpliceConglomerate {conglomerate=%s}",conglomerate);
		}
	}
	
	
	
}
