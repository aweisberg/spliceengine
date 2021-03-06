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

package com.splicemachine.derby.impl.sql.depend;

import com.splicemachine.derby.impl.store.access.BaseSpliceTransaction;
import com.splicemachine.derby.impl.store.access.SpliceTransaction;
import com.splicemachine.derby.impl.store.access.SpliceTransactionManager;
import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.services.context.ContextManager;
import com.splicemachine.db.iapi.sql.conn.LanguageConnectionContext;
import com.splicemachine.db.iapi.sql.depend.Dependency;
import com.splicemachine.db.iapi.sql.depend.Dependent;
import com.splicemachine.db.iapi.sql.depend.Provider;
import com.splicemachine.db.iapi.sql.depend.ProviderInfo;
import com.splicemachine.db.iapi.sql.depend.ProviderList;
import com.splicemachine.db.iapi.sql.dictionary.DataDictionary;
import com.splicemachine.db.iapi.store.access.TransactionController;
import com.splicemachine.db.impl.sql.depend.BasicDependencyManager;
import com.splicemachine.primitives.Bytes;
import org.apache.log4j.Logger;
import com.splicemachine.utils.SpliceLogUtils;

public class SpliceDependencyManager extends BasicDependencyManager {
    private static final Logger LOG = Logger.getLogger(SpliceDependencyManager.class);
    
	public SpliceDependencyManager(DataDictionary dd) {
		super(dd);
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "initialize dependencyManager");
	}

	@Override
	public void addDependency(Dependent d, Provider p, ContextManager cm) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "addDependency dependent=%s,provider=%s,contextManager=%s",d,p,cm);
		super.addDependency(d, p, cm);
	}

	@Override
	public void invalidateFor(Provider p, int action, LanguageConnectionContext lcc) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "invalidateFor provider=%s,action=%d,lcc=%s",p,action,lcc);
		super.invalidateFor(p, action, lcc);
	}

	@Override
	public void clearDependencies(LanguageConnectionContext lcc, Dependent d) throws StandardException {
        // Passed on to next method...
		super.clearDependencies(lcc, d);
	}

	@Override
	public void clearDependencies(LanguageConnectionContext lcc,Dependent d,TransactionController tc) throws StandardException{
		super.clearDependencies(lcc,d,tc);
	}

	@Override
	public synchronized void clearInMemoryDependency(Dependency dy) {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "clearInMemoryDependency dependency=%s",dy);
		super.clearInMemoryDependency(dy);
	}

	@Override
	public ProviderInfo[] getPersistentProviderInfos(Dependent dependent) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "getPersistentProviderInfos dependent=%s",dependent);
		return super.getPersistentProviderInfos(dependent);
	}

	@Override
	public ProviderInfo[] getPersistentProviderInfos(ProviderList pl) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "getPersistentProviderInfos providerList=%s",pl);
		return super.getPersistentProviderInfos(pl);
	}

	@Override
	public void clearColumnInfoInProviders(ProviderList pl) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "clearColumnInfoInProviders providerList=%s",pl);
		super.clearColumnInfoInProviders(pl);
	}

	@Override
	public void copyDependencies(Dependent copyFrom, Dependent copyTo, boolean persistentOnly, ContextManager cm) throws StandardException {
		if (LOG.isTraceEnabled())
			SpliceLogUtils.trace(LOG, "copyDependencies copyFrom=%s,copyTo=%s,persistentOnly=%s, contextManager=%s",copyFrom,copyTo,persistentOnly,cm);
		super.copyDependencies(copyFrom, copyTo, persistentOnly, cm);
	}

	@Override
  public void copyDependencies(Dependent copyFrom, Dependent copyTo, boolean persistentOnly, ContextManager cm, TransactionController tc) throws StandardException {
      if (LOG.isTraceEnabled())
          SpliceLogUtils.trace(LOG, "copyDependencies copyFrom=%s,copyTo=%s,persistentOnly=%s, contextManager=%s, transactionController=%s",copyFrom,copyTo,persistentOnly,cm,tc);
      LanguageConnectionContext lcc = getLanguageConnectionContext(cm);
      // tc == null means do it in the user transaction
      TransactionController tcToUse = (tc == null) ? lcc.getTransactionExecute() : tc;
      BaseSpliceTransaction usrTxn = ((SpliceTransactionManager)tcToUse).getRawTransaction();
      assert usrTxn instanceof SpliceTransaction: "Programmer error: cannot elevate a non-SpliceTransaction";
      SpliceTransaction txn = (SpliceTransaction)usrTxn;
      if(!txn.allowsWrites())
          txn.elevate(Bytes.toBytes(copyTo.getObjectName()));
      super.copyDependencies(copyFrom, copyTo, persistentOnly, cm, tc);
  }

    @Override
    public String getActionString(int action) {
        if (LOG.isTraceEnabled())
            SpliceLogUtils.trace(LOG, "getActionString %d",action);
        return super.getActionString(action);
    }

    @Override
    public int countDependencies() throws StandardException {
        if (LOG.isTraceEnabled())
            SpliceLogUtils.trace(LOG, "countDependencies");
        return super.countDependencies();
    }

}
