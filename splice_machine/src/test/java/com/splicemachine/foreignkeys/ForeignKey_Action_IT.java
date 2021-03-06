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

package com.splicemachine.foreignkeys;

import com.splicemachine.derby.test.framework.SpliceSchemaWatcher;
import com.splicemachine.derby.test.framework.SpliceWatcher;
import com.splicemachine.derby.test.framework.TestConnection;
import com.splicemachine.test_dao.TableDAO;
import com.splicemachine.util.StatementUtils;
import org.junit.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Foreign key tests for referential actions:
 *
 * ON DELETE NO ACTION
 * ON DELETE CASCADE
 * ON DELETE SET NULL
 * ON UPDATE NO ACTION
 */
public class ForeignKey_Action_IT {

    private static final String SCHEMA = ForeignKey_Action_IT.class.getSimpleName();

    @ClassRule
    public static SpliceSchemaWatcher spliceSchemaWatcher = new SpliceSchemaWatcher(SCHEMA);

    @Rule
    public SpliceWatcher methodWatcher = new SpliceWatcher(SCHEMA);

    private TestConnection conn;
    @Before
    public void deleteTables() throws Exception {
        conn = methodWatcher.getOrCreateConnection();
        conn.setAutoCommit(false);
        new TableDAO(conn).drop(SCHEMA, "C", "P");
    }

    @After
    public void tearDown() throws Exception{
        conn.rollback();
        conn.reset();
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //
    // fk references unique index
    //
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void onDeleteNoAction() throws Exception {
        try(Statement s = conn.createStatement()){
            s.executeUpdate("create table P (a int unique, b int)");
            s.executeUpdate("create table C (a int, b int, CONSTRAINT FK_1 FOREIGN KEY (a) REFERENCES P(a) ON DELETE NO ACTION)");
            s.executeUpdate("insert into P values(1,10),(2,20),(3,30)");
            s.executeUpdate("insert into C values(1,10),(1,15),(2,20),(2,20),(3,30),(3,35)");

            assertQueryFail(s,"delete from P where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
        }
    }

    @Test
    public void onDeleteNoActionImplicit() throws Exception {
        try(Statement s = conn.createStatement()){
            s.executeUpdate("create table P (a int unique, b int)");
            s.executeUpdate("create table C (a int, b int, CONSTRAINT FK_1 FOREIGN KEY (a) REFERENCES P(a))");
            s.executeUpdate("insert into P values(1,10),(2,20),(3,30)");
            s.executeUpdate("insert into C values(1,10),(1,15),(2,20),(2,20),(3,30),(3,35)");

            assertQueryFail(s,"delete from P where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //
    // fk references primary key
    //
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void onDeleteNoAction_primaryKey() throws Exception {
        try(Statement s = conn.createStatement()){
            s.executeUpdate("create table P (a int primary key, b int)");
            s.executeUpdate("create table C (a int, b int, CONSTRAINT FK_1 FOREIGN KEY (a) REFERENCES P(a) ON DELETE NO ACTION)");
            s.executeUpdate("insert into P values(1,10),(2,20),(3,30)");
            s.executeUpdate("insert into C values(1,10),(1,15),(2,20),(2,20),(3,30),(3,35)");

            assertQueryFail(s,"delete from P where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
        }
    }

    /* Make sure FKs still work when we create the parent, write to it first, then create the child that actually has the FK */
    @Test
    public void onDeleteNoAction_primaryKey_initializeWriteContextOfParentFirst() throws Exception {
        try(Statement s = conn.createStatement()){
            s.executeUpdate("create table P (a int primary key, b int unique)");
            s.executeUpdate("insert into P values(1,10),(2,20),(3,30),(4,40)");

            s.executeUpdate("create table C1 (a int, b int, CONSTRAINT FK_1 FOREIGN KEY (a) REFERENCES P(a))");
            s.executeUpdate("insert into C1 values(1,10),(1,15),(2,20),(2,20),(3,30),(3,35)");

            assertQueryFail(s,"delete from P where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");

            s.executeUpdate("create table C2 (a int, b int, CONSTRAINT FK_2 FOREIGN KEY (b) REFERENCES P(b))");
            s.executeUpdate("insert into C2 values(4,40)");

            // verify NEW FK constraint works
            assertQueryFail(s,"delete from P where a = 4","Operation on table 'P' caused a violation of foreign key constraint 'FK_2' for key (B).  The statement has been rolled back.");
            assertQueryFail(s,"update P set b=-1 where a = 4","Operation on table 'P' caused a violation of foreign key constraint 'FK_2' for key (B).  The statement has been rolled back.");

            // verify FIRST FK constraint STILL works
            assertQueryFail(s,"delete from P where a = 1","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 1","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
        }
    }

    @Test
    public void onDeleteNoAction_primaryKey_successAfterDeleteReference() throws Exception {
        try(Statement s = conn.createStatement()){
            s.executeUpdate("create table P (a int primary key, b int)");
            s.executeUpdate("create table C (a int, b int, CONSTRAINT FK_1 FOREIGN KEY (a) REFERENCES P(a) ON DELETE NO ACTION)");
            s.executeUpdate("insert into P values(1,10),(2,20),(3,30)");
            s.executeUpdate("insert into C values(1,10),(1,15),(2,20),(2,20),(3,30),(3,35)");

            assertQueryFail(s,"delete from P where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");
            assertQueryFail(s,"update P set a=-1 where a = 2","Operation on table 'P' caused a violation of foreign key constraint 'FK_1' for key (A).  The statement has been rolled back.");

            // delete references
            s.executeUpdate("delete from C where a=2");

            // now delete from parent should succeed
            assertEquals(4L,StatementUtils.onlyLong(s,"select count(*) from C"));
            assertEquals(1L,s.executeUpdate("delete from P where a = 2"));
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //
    // helper methods
    //
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private void assertQueryFail(Statement s,String sql, String expectedExceptionMessage) {
        try{
            s.executeUpdate(sql);
            fail("expected query to fail: " + sql);
        } catch (Exception e) {
            assertEquals(expectedExceptionMessage, e.getMessage());
            assertEquals(SQLIntegrityConstraintViolationException.class, e.getClass());
        }
    }

}