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

package com.splicemachine.db.iapi.jdbc;

import java.sql.*;
import java.net.URL;

/**
	JDBC 3 implementation of PreparedStatement.
*/
public class BrokeredPreparedStatement30 extends BrokeredPreparedStatement {

	private final Object generatedKeys;
	public BrokeredPreparedStatement30(BrokeredStatementControl control, String sql, Object generatedKeys) throws SQLException {
		super(control,sql);
		this.generatedKeys = generatedKeys;
	}

	public final void setURL(int i, URL x)
        throws SQLException
    {
        getPreparedStatement().setURL( i, x);
    }
    public final ParameterMetaData getParameterMetaData()
        throws SQLException
    {
        return getPreparedStatement().getParameterMetaData();
    }
	/**
		Create a duplicate PreparedStatement to this, including state, from the passed in Connection.
	*/
	public PreparedStatement createDuplicateStatement(Connection conn, PreparedStatement oldStatement) throws SQLException {

		PreparedStatement newStatement;

		if (generatedKeys == null)
			newStatement = conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		else {
			// The prepareStatement() calls that take a generated key value do not take resultSet* type
			// parameters, but since they don't return ResultSets that is OK. There are only for INSERT statements.
			if (generatedKeys instanceof Integer)
				newStatement = conn.prepareStatement(sql, ((Integer) generatedKeys).intValue());
			else if (generatedKeys instanceof int[])
				newStatement = conn.prepareStatement(sql, (int[]) generatedKeys);
			else
				newStatement = conn.prepareStatement(sql, (String[]) generatedKeys);
		}


		setStatementState(oldStatement, newStatement);

		return newStatement;
	}
}
