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

package com.splicemachine.derby.stream.control;

import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.sql.Activation;
import com.splicemachine.derby.iapi.sql.execute.SpliceOperation;
import com.splicemachine.derby.iapi.sql.execute.SpliceOperationContext;
import com.splicemachine.derby.jdbc.SpliceTransactionResourceImpl;
import com.splicemachine.derby.stream.ActivationHolder;import com.splicemachine.derby.stream.iapi.OperationContext;
import com.splicemachine.si.api.txn.TxnView;
import com.splicemachine.si.impl.driver.SIDriver;
import com.splicemachine.utils.SpliceLogUtils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jleach on 4/17/15.
 */
public class ControlOperationContext<Op extends SpliceOperation> implements OperationContext<Op> {
    private static final String LINE_SEP = System.lineSeparator();
    private static Logger LOG = Logger.getLogger(ControlOperationContext.class);

    long rowsRead;
        long rowsFiltered;
        long rowsWritten;
        long rowsRetried;
        long tooBusy;
        long rowsJoinedLeft;
        long rowsJoinedRight;
        long rowsProduced;
        List<String> badRecords;
        public ActivationHolder activationHolder;
        public SpliceTransactionResourceImpl impl;
        public Activation activation;
        public SpliceOperationContext context;
        public Op op;
        public TxnView txn;
        private int failBadRecordCount = -1;
        private boolean permissive;
        private BadRecordsRecorder badRecordsRecorder;
        private boolean failed;
        private int numberBadRecords = 0;

    public ControlOperationContext() {
        }

        protected ControlOperationContext(Op spliceOperation) {
            this.op = spliceOperation;
            if (op !=null) {
                this.activation = op.getActivation();
                try {
                    this.txn = spliceOperation.getCurrentTransaction();
                } catch (StandardException se) {
                    throw new RuntimeException(se);
                }
            }
            rowsRead = 0;
            rowsFiltered=0;
            rowsWritten = 0;
            badRecords =new ArrayList<>();
        }

        public void readExternalInContext(ObjectInput in) throws IOException, ClassNotFoundException
        {}

    @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            if(activationHolder==null)
                activationHolder = new ActivationHolder(activation, op);
            out.writeObject(activationHolder);
            out.writeObject(op);
            out.writeObject(badRecordsRecorder);
            SIDriver.driver().getOperationFactory().writeTxn(txn, out);
       }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            activationHolder = (ActivationHolder)in.readObject();
            op = (Op) in.readObject();
            badRecordsRecorder = (BadRecordsRecorder) in.readObject();
            txn = SIDriver.driver().getOperationFactory().readTxn(in);
            boolean prepared = false;
            try {
                impl = new SpliceTransactionResourceImpl();

                prepared = impl.marshallTransaction(txn);
                activation = activationHolder.getActivation();
                context = SpliceOperationContext.newContext(activation);
                op.init(context);
                readExternalInContext(in);
            } catch (Exception e) {
                SpliceLogUtils.logAndThrowRuntime(LOG, e);
            } finally {
                if (prepared) {
                    impl.close();
                }
            }
        }

    @Override
    public void prepare() {

    }

    @Override
    public void reset() {

    }

    @Override
    public Op getOperation() {
        return op;
    }

    @Override
    public Activation getActivation() {
        return op.getActivation();
    }


    @Override
    public void recordRead() {
        rowsRead++;
    }

    @Override
    public void recordRetry(long w) {
        rowsRetried+=w;
    }

    @Override
    public void recordRegionTooBusy(long w) {
        tooBusy+=w;
    }

    @Override
    public void recordFilter() {
        rowsFiltered++;
    }

    @Override
    public void recordWrite() {
        rowsWritten++;
    }

    @Override
    public void recordPipelineWrites(long w) {
        rowsWritten+=w;
    }

    @Override
    public void recordThrownErrorRows(long w) {

    }

    @Override
    public void recordRetriedRows(long w) {

    }

    @Override
    public void recordPartialRows(long w) {

    }

    @Override
    public void recordPartialThrownErrorRows(long w) {

    }

    @Override
    public void recordPartialRetriedRows(long w) {

    }

    @Override
    public void recordPartialIgnoredRows(long w) {

    }

    @Override
    public void recordPartialWrite(long w) {

    }

    @Override
    public void recordIgnoredRows(long w) {

    }

    @Override
    public void recordCatchThrownRows(long w) {

    }

    @Override
    public void recordCatchRetriedRows(long w) {

    }

    @Override
    public void recordJoinedLeft() {
        rowsJoinedLeft++;
    }

    @Override
    public void recordJoinedRight() {
        rowsJoinedRight++;
    }

    @Override
    public void recordProduced() {
        rowsProduced++;
    }

    @Override
    public long getRecordsRead() {
        return rowsRead;
    }

    @Override
    public long getRecordsFiltered() {
        return rowsFiltered;
    }

    @Override
    public long getRecordsWritten() {
        return rowsWritten;
    }

    @Override
    public long getRetryAttempts() {
        return rowsRetried;
    }

    @Override
    public long getRegionTooBusyExceptions() {
        return tooBusy;
    }

    @Override
    public void pushScope(String displayName) {
        // no op
    }

    @Override
    public void pushScope() {
        // no op
    }

    @Override
    public void pushScopeForOp(Scope step) {
        // no op
    }

    @Override
    public void pushScopeForOp(String step) {
        // no op
    }

    @Override
    public void popScope() {
        // no op
    }

    @Override
    public TxnView getTxn(){
        return txn;
    }

    @Override
    public void recordBadRecord(String badRecord, Exception e) {
        if (! failed) {
            String errorState = "";
            if (e != null) {
                if (e instanceof SQLException) {
                    errorState = ((SQLException)e).getSQLState();
                } else if (e instanceof StandardException) {
                    errorState = ((StandardException)e).getSQLState();
                }
            }
            failed = badRecordsRecorder.recordBadRecord(errorState + " " + badRecord+LINE_SEP);
        }
    }

    @Override
    public long getBadRecords() {
        return (badRecordsRecorder != null ? badRecordsRecorder.getNumberOfBadRecords() : 0);
    }

    @Override
    public String getBadRecordFileName() {
        return (badRecordsRecorder != null ? badRecordsRecorder.getBadRecordFileName() : "");
    }

    @Override
    public BadRecordsRecorder getBadRecordsRecorder() {
        return badRecordsRecorder;
    }

    @Override
    public boolean isPermissive() {
        return permissive;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public void setPermissive(String statusDirectory, String importFileName, long badRecordThreshold) {
        this.permissive=true;
        this.badRecordsRecorder = new BadRecordsRecorder(statusDirectory, importFileName, badRecordThreshold);
    }
}
