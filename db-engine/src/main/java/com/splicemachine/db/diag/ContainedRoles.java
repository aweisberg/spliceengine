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

package com.splicemachine.db.diag;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import com.splicemachine.db.iapi.sql.conn.ConnectionUtil;
import com.splicemachine.db.iapi.sql.conn.LanguageConnectionContext;
import com.splicemachine.db.iapi.sql.dictionary.DataDictionary;
import com.splicemachine.db.iapi.sql.dictionary.RoleClosureIterator;
import com.splicemachine.db.iapi.sql.dictionary.RoleGrantDescriptor;
import com.splicemachine.db.iapi.sql.ResultColumnDescriptor;
import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.reference.Limits;
import com.splicemachine.db.iapi.error.PublicAPI;
import com.splicemachine.db.iapi.util.IdUtil;
import com.splicemachine.db.vti.VTITemplate;

import com.splicemachine.db.impl.jdbc.EmbedResultSetMetaData;


/**
 * Contained roles shows all roles contained in the given identifier, or if the
 * second argument, if given, is not 0, the inverse relation; all roles who
 * contain the given role identifier.
 *
 * <p>To use it, query it as follows:
 * </p>
 * <pre> SELECT * FROM TABLE(SUSCS_DIAG.CONTAINED_ROLES('FOO')) t; </pre>
 * <pre> SELECT * FROM TABLE(CONTAINED_ROLES('FOO', 1)) t; </pre>
 *
 * <p>The following columns will be returned:
 *    <ul><li>ROLEID -- VARCHAR(128) NOT NULL
 *    </ul>
 * </p>
 */
public class ContainedRoles extends VTITemplate {

    RoleClosureIterator rci;
    String nextRole;
    boolean initialized;
    String role;
    boolean inverse;

    /**
     * Constructor.
     *
     * @param roleid The role identifier for which we want to find the set of
     *               contained roles (inclusive). The identifier is expected to
     *               be in SQL form (not case normal form).
     * @param inverse If != 0, use the inverse relation: find those roles which
     *                all contain roleid (inclusive).
     * @throws SQLException This is a public API, so the internal exception is
     *                      wrapped in SQLException.
     */
    public ContainedRoles(String roleid, int inverse) throws SQLException {
        try {
            if (roleid != null) {
                role = IdUtil.parseSQLIdentifier(roleid);
            }

            this.inverse = (inverse != 0);
        } catch (StandardException e) {
            throw PublicAPI.wrapStandardException(e);
        }
    }

    /**
     * Constructor.
     *
     * @param roleid The role identifier for which we want to find the set of
     *               contained roles (inclusive). The identifier is expected to
     *               be in SQL form (not case normal form).
     * @throws SQLException This is a public API, so the internal exception is
     *                      wrapped in SQLException.
     */
    public ContainedRoles(String roleid)  throws SQLException {
        this(roleid, 0);
    }

    /**
     * @see java.sql.ResultSet#next
     */
    public boolean next() throws SQLException {
        try {
            // Need to defer initialization here to make sure we have an
            // activation.
            if (!initialized) {
                initialized = true;
                LanguageConnectionContext lcc = ConnectionUtil.getCurrentLCC();
                DataDictionary dd = lcc.getDataDictionary();
                RoleGrantDescriptor rdDef =
                    dd.getRoleDefinitionDescriptor(role);

                if (rdDef != null) {
                    lcc.beginNestedTransaction(true);
                    try {
                        try {
                            rci = dd.createRoleClosureIterator
                                (lcc.getLastActivation().
                                     getTransactionController(),
                                 role, !inverse);
                        } finally {
                        }
                    } finally {
                        // make sure we commit; otherwise, we will end up with
                        // mismatch nested level in the language connection
                        // context.
                        lcc.commitNestedTransaction();
                    }
                }
            }

            return rci != null && ((nextRole = rci.next()) != null);

        } catch (StandardException e) {
            throw PublicAPI.wrapStandardException(e);
        }
    }


    /**
     * @see java.sql.ResultSet#close
     */
    public void close() {
    }


    /**
     * @see java.sql.ResultSet#getMetaData
     */
    public ResultSetMetaData getMetaData() {
        return metadata;
    }

    /**
     * @see java.sql.ResultSet#getString
     */
    public String getString(int columnIndex) throws SQLException {
        return nextRole;
    }

    /*
     * Metadata
     */
    private static final ResultColumnDescriptor[] columnInfo = {
        EmbedResultSetMetaData.getResultColumnDescriptor
        ("ROLEID", Types.VARCHAR, false, Limits.MAX_IDENTIFIER_LENGTH)
    };

    private static final ResultSetMetaData metadata =
        new EmbedResultSetMetaData(columnInfo);

}
