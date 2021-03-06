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

package com.splicemachine.si.jmx;

import javax.management.MXBean;

/**
 * Monitoring Hook for JMX.
 *
 * @author Scott Fines
 * Created on: 6/3/13
 */
@MXBean
@SuppressWarnings("unused")
public interface TransactorStatus {

    /**
     * @return the total number of child transactions created by this node.
     */
    long getTotalChildTransactions();


    //TODO -sf- support these last two methods
    /**
     * @return the total number of non-child transactions committed by this node.
     */
//    long getTotalCommittedChildTransactions();

    /**
     * @return the total number of non-child transactions rolled back by this node.
     */
//    long getTotalRolledBackChildTransactions();

    /**
     * @return the total number of non-child transactions created by this node.
     */
    long getTotalTransactions();

    /**
     * @return the total number of non-child transactions committed by this node.
     */
    long getTotalCommittedTransactions();

    /**
     * @return the total number of non-child transactions rolled back by this node.
     */
    long getTotalRolledBackTransactions();

    /**
     * @return the total number of failed transactions on this node.
     */
    long getTotalFailedTransactions();

    /**
     * @return the total number of Transactions which were loaded by the store
     */
    long getNumLoadedTxns();

    /**
     * @return the total number of Transaction updates which were written
     */
    long getNumTxnUpdatesWritten();
}
