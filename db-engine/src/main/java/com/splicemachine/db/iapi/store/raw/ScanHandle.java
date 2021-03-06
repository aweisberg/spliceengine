/*
 * Apache Derby is a subproject of the Apache DB project, and is licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use these files
 * except in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Splice Machine, Inc. has modified this file.
 *
 * All Splice Machine modifications are Copyright 2012 - 2016 Splice Machine, Inc.,
 * and are licensed to you under the License; you may not use this file except in
 * compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.splicemachine.db.iapi.store.raw;

import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.store.access.DatabaseInstant;
import java.io.InputStream;

/**
  Inteface for scanning the log from outside the RawStore.
  */
public interface ScanHandle
{
	/**
	  Position to the next log record. 
	  @return true if the log contains a next flushed log record and
	           false otherwise. If this returns false it is incorrect
			   to make any of the other calls on this interface.
	  @exception StandardException Oops
	  */
	public boolean next() throws StandardException;

	/**
	  Get the group for the current log record.
	  @exception StandardException Oops
	  */
	public int getGroup() throws StandardException;

	/**
	  Get the DatabaseInstant for the current log record.
	  @exception StandardException Oops
	  */
    public DatabaseInstant getInstant() throws StandardException;
	/**
	  Close this scan.
	  */
    public void close();
}
