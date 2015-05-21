/*

   Derby - Class com.splicemachine.db.iapi.db.TriggerExecutionContext

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.splicemachine.db.iapi.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.splicemachine.db.catalog.UUID;

/**
 * A trigger execution context holds information that is available from the context of a trigger invocation.
 */
public interface TriggerExecutionContext {
    /**
     * Get the target table name upon which the trigger event is declared.
     *
     * @return the target table
     */
    String getTargetTableName();

    /**
     * Get the target table UUID upon which the trigger event is declared.
     *
     * @return the uuid of the target table
     */
    UUID getTargetTableId();

    /**
     * Get the text of the statement that caused the trigger to fire.
     *
     * @return the statement text.
     */
    String getEventStatementText();

    /**
     * Get the columns that have been modified by the statement
     * that caused this trigger to fire.  If all columns are
     * modified, will return null (e.g. for INSERT or DELETE
     * return null).
     *
     * @return an array of Strings
     */
    String[] getModifiedColumns();

    /**
     * Find out if a column was changed, by column name.
     * Note that this will always return true for INSERT and DELETE regardless of the column name passed in.
     *
     * @param columnName the column to check
     * @return true if the column was modified by this statement.
     */
    boolean wasColumnModified(String columnName);

    /**
     * Find out if a column was changed, by column number
     * Note that this will always return true for INSERT and DELETE regardless of the column name passed in.
     *
     * @param columnNumber the column to check
     * @return true if the column was modified by this statement.
     */
    boolean wasColumnModified(int columnNumber);

    /**
     * Returns a result set of the old (before) images of the changed rows.
     * For a row trigger, this result set will have a single row.  For
     * a statement trigger, this result set has every row that has
     * changed or will change.  If a statement trigger does not affect
     * a row, then the result set will be empty (i.e. ResultSet.next()
     * will return false).
     * <p/>
     * Will return null if the call is inapplicable for the trigger
     * that is currently executing.  For example, will return null if called
     * during a the firing of an INSERT trigger.
     *
     * @return the ResultSet containing before images of the rows
     * changed by the triggering event.  May return null.
     * @throws SQLException if called after the triggering event has completed
     */
    ResultSet getOldRowSet() throws SQLException;

    /**
     * Returns a result set of the new (after) images of the changed rows.
     * For a row trigger, this result set will have a single row.  For
     * a statement trigger, this result set has every row that has
     * changed or will change.  If a statement trigger does not affect
     * a row, then the result set will be empty (i.e. ResultSet.next()
     * will return false).
     * <p/>
     * Will return null if the call is inapplicable for the trigger
     * that is currently executing.  For example, will return null if
     * called during the firing of a DELETE trigger.
     *
     * @return the ResultSet containing after images of the rows
     * changed by the triggering event.  May return null.
     * @throws SQLException if called after the triggering event has completed
     */
    ResultSet getNewRowSet() throws SQLException;

    /**
     * Like getOldRowSet(), but returns a result set positioned
     * on the first row of the before (old) result set.  Used as a convenience
     * to get a column for a row trigger.  Equivalent to getOldRowSet()
     * followed by next().
     * <p/>
     * Will return null if the call is inapplicable for the trigger
     * that is currently executing.  For example, will return null if called
     * during a the firing of an INSERT trigger.
     *
     * @return the ResultSet positioned on the old row image.  May return null.
     * @throws SQLException if called after the triggering event has completed
     */
    ResultSet getOldRow() throws SQLException;

    /**
     * Like getNewRowSet(), but returns a result set positioned
     * on the first row of the after (new) result set.  Used as a convenience
     * to get a column for a row trigger.  Equivalent to getNewRowSet()
     * followed by next().
     * <p/>
     * Will return null if the call is inapplicable for the trigger
     * that is currently executing.  For example, will return null if
     * called during the firing of a DELETE trigger.
     *
     * @return the ResultSet positioned on the new row image.  May return null.
     * @throws SQLException if called after the triggering event has completed
     */
    ResultSet getNewRow() throws SQLException;
}
