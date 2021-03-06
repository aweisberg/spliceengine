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

package com.splicemachine.derby.impl.sql.execute.tester;

import com.splicemachine.derby.test.framework.SpliceWatcher;
import com.splicemachine.derby.test.framework.SpliceSchemaWatcher;
import com.splicemachine.derby.test.framework.SpliceWatcher;
import com.splicemachine.homeless.TestUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NumericConstantsIT {

    private static final String CLASS_NAME = NumericConstantsIT.class.getSimpleName().toUpperCase();
    private static final SpliceWatcher spliceClassWatcher = new SpliceWatcher(CLASS_NAME);

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(spliceClassWatcher)
            .around(new SpliceSchemaWatcher(CLASS_NAME))
            .around(TestUtils.createFileDataWatcher(spliceClassWatcher, "test_data/NumericConstantsIT.sql", CLASS_NAME));

    @Rule
    public SpliceWatcher methodWatcher = new SpliceWatcher(CLASS_NAME);

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // smallint
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void smallInt_min() throws Exception {
        assertCount(1, "table_smallint", "a", "=", Short.MIN_VALUE);
        assertCount(4, "table_smallint", "a", "!=", Short.MIN_VALUE);
        assertCount(0, "table_smallint", "a", "<", Short.MIN_VALUE);
        assertCount(1, "table_smallint", "a", "<=", Short.MIN_VALUE);
        assertCount(4, "table_smallint", "a", ">", Short.MIN_VALUE);
        assertCount(5, "table_smallint", "a", ">=", Short.MIN_VALUE);
    }

    @Test
    public void smallInt_max() throws Exception {
        assertCount(1, "table_smallint", "a", "=", Short.MAX_VALUE);
        assertCount(4, "table_smallint", "a", "!=", Short.MAX_VALUE);
        assertCount(4, "table_smallint", "a", "<", Short.MAX_VALUE);
        assertCount(5, "table_smallint", "a", "<=", Short.MAX_VALUE);
        assertCount(0, "table_smallint", "a", ">", Short.MAX_VALUE);
        assertCount(1, "table_smallint", "a", ">=", Short.MAX_VALUE);
    }

    @Test
    public void smallInt_max_plusOne() throws Exception {
        String SHORT_MAX_PLUS_1 = new BigInteger(String.valueOf(Short.MAX_VALUE)).add(BigInteger.ONE).toString();

        assertCount(0, "table_smallint", "a", "=", SHORT_MAX_PLUS_1);
        assertCount(5, "table_smallint", "a", "!=", SHORT_MAX_PLUS_1);
        assertCount(5, "table_smallint", "a", "<", SHORT_MAX_PLUS_1);
        assertCount(5, "table_smallint", "a", "<=", SHORT_MAX_PLUS_1);
        assertCount(0, "table_smallint", "a", ">", SHORT_MAX_PLUS_1);
        assertCount(0, "table_smallint", "a", ">=", SHORT_MAX_PLUS_1);
    }

    @Test
    public void smallInt_min_minusOne() throws Exception {
        String SHORT_MIN_MINUS_1 = new BigInteger(String.valueOf(Short.MIN_VALUE)).subtract(BigInteger.ONE).toString();

        assertCount(0, "table_smallint", "a", "=", SHORT_MIN_MINUS_1);
        assertCount(5, "table_smallint", "a", "!=", SHORT_MIN_MINUS_1);
        assertCount(0, "table_smallint", "a", "<", SHORT_MIN_MINUS_1);
        assertCount(0, "table_smallint", "a", "<=", SHORT_MIN_MINUS_1);
        assertCount(5, "table_smallint", "a", ">", SHORT_MIN_MINUS_1);
        assertCount(5, "table_smallint", "a", ">=", SHORT_MIN_MINUS_1);
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // integer
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void integer_min() throws Exception {
        assertCount(1, "table_integer", "a", "=", Integer.MIN_VALUE);
        assertCount(4, "table_integer", "a", "!=", Integer.MIN_VALUE);
        assertCount(0, "table_integer", "a", "<", Integer.MIN_VALUE);
        assertCount(1, "table_integer", "a", "<=", Integer.MIN_VALUE);
        assertCount(4, "table_integer", "a", ">", Integer.MIN_VALUE);
        assertCount(5, "table_integer", "a", ">=", Integer.MIN_VALUE);
    }

    @Test
    public void integer_max() throws Exception {
        assertCount(1, "table_integer", "a", "=", Integer.MAX_VALUE);
        assertCount(4, "table_integer", "a", "!=", Integer.MAX_VALUE);
        assertCount(4, "table_integer", "a", "<", Integer.MAX_VALUE);
        assertCount(5, "table_integer", "a", "<=", Integer.MAX_VALUE);
        assertCount(0, "table_integer", "a", ">", Integer.MAX_VALUE);
        assertCount(1, "table_integer", "a", ">=", Integer.MAX_VALUE);
    }

    @Test
    public void integer_max_plusOne() throws Exception {
        String INT_MAX_PLUS_1 = new BigInteger(String.valueOf(Integer.MAX_VALUE)).add(BigInteger.ONE).toString();

        assertCount(0, "table_integer", "a", "=", INT_MAX_PLUS_1);
        assertCount(5, "table_integer", "a", "!=", INT_MAX_PLUS_1);
        assertCount(5, "table_integer", "a", "<", INT_MAX_PLUS_1);
        assertCount(5, "table_integer", "a", "<=", INT_MAX_PLUS_1);
        assertCount(0, "table_integer", "a", ">", INT_MAX_PLUS_1);
        assertCount(0, "table_integer", "a", ">=", INT_MAX_PLUS_1);
    }

    @Test
    public void integer_min_minusOne() throws Exception {
        String INT_MIN_MINUS_1 = new BigInteger(String.valueOf(Integer.MIN_VALUE)).subtract(BigInteger.ONE).toString();

        assertCount(0, "table_integer", "a", "=", INT_MIN_MINUS_1);
        assertCount(5, "table_integer", "a", "!=", INT_MIN_MINUS_1);
        assertCount(0, "table_integer", "a", "<", INT_MIN_MINUS_1);
        assertCount(0, "table_integer", "a", "<=", INT_MIN_MINUS_1);
        assertCount(5, "table_integer", "a", ">", INT_MIN_MINUS_1);
        assertCount(5, "table_integer", "a", ">=", INT_MIN_MINUS_1);
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // bigint
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void bigint_min() throws Exception {
        assertCount(1, "table_bigint", "a", "=", Long.MIN_VALUE);
        assertCount(4, "table_bigint", "a", "!=", Long.MIN_VALUE);
        assertCount(0, "table_bigint", "a", "<", Long.MIN_VALUE);
        assertCount(1, "table_bigint", "a", "<=", Long.MIN_VALUE);
        assertCount(4, "table_bigint", "a", ">", Long.MIN_VALUE);
        assertCount(5, "table_bigint", "a", ">=", Long.MIN_VALUE);
    }

    @Test
    public void bigint_max() throws Exception {
        assertCount(1, "table_bigint", "a", "=", Long.MAX_VALUE);
        assertCount(4, "table_bigint", "a", "!=", Long.MAX_VALUE);
        assertCount(4, "table_bigint", "a", "<", Long.MAX_VALUE);
        assertCount(5, "table_bigint", "a", "<=", Long.MAX_VALUE);
        assertCount(0, "table_bigint", "a", ">", Long.MAX_VALUE);
        assertCount(1, "table_bigint", "a", ">=", Long.MAX_VALUE);
    }

    @Test
    public void bigint_max_plusOne() throws Exception {
        String LONG_MAX_PLUS_1 = new BigInteger(String.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE).toString();

        assertCount(0, "table_bigint", "a", "=", LONG_MAX_PLUS_1);
        assertCount(5, "table_bigint", "a", "!=", LONG_MAX_PLUS_1);
        assertCount(5, "table_bigint", "a", "<", LONG_MAX_PLUS_1);
        assertCount(5, "table_bigint", "a", "<=", LONG_MAX_PLUS_1);
        assertCount(0, "table_bigint", "a", ">", LONG_MAX_PLUS_1);
        assertCount(0, "table_bigint", "a", ">=", LONG_MAX_PLUS_1);
    }

    @Test
    public void bigint_min_minusOne() throws Exception {
        String LONG_MIN_MINUS_1 = new BigInteger(String.valueOf(Long.MIN_VALUE)).subtract(BigInteger.ONE).toString();

        assertCount(0, "table_bigint", "a", "=", LONG_MIN_MINUS_1);
        assertCount(5, "table_bigint", "a", "!=", LONG_MIN_MINUS_1);
        assertCount(0, "table_bigint", "a", "<", LONG_MIN_MINUS_1);
        assertCount(0, "table_bigint", "a", "<=", LONG_MIN_MINUS_1);
        assertCount(5, "table_bigint", "a", ">", LONG_MIN_MINUS_1);
        assertCount(5, "table_bigint", "a", ">=", LONG_MIN_MINUS_1);
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // real
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void real_min() throws Exception {
        String REAL_MIN = "-3.402E+38";

        assertCount(1, "table_real", "a", "=", REAL_MIN);
        assertCount(4, "table_real", "a", "!=", REAL_MIN);
        assertCount(0, "table_real", "a", "<", REAL_MIN);
        assertCount(1, "table_real", "a", "<=", REAL_MIN);
        assertCount(4, "table_real", "a", ">", REAL_MIN);
        assertCount(5, "table_real", "a", ">=", REAL_MIN);
    }

    @Test
    public void real_max() throws Exception {
        String REAL_MAX = "3.402E+38";

        assertCount(1, "table_real", "a", "=", REAL_MAX);
        assertCount(4, "table_real", "a", "!=", REAL_MAX);
        assertCount(4, "table_real", "a", "<", REAL_MAX);
        assertCount(5, "table_real", "a", "<=", REAL_MAX);
        assertCount(0, "table_real", "a", ">", REAL_MAX);
        assertCount(1, "table_real", "a", ">=", REAL_MAX);
    }

    @Test
    public void real_minTimesTen() throws Exception {
        String REAL_MIN_TIMES_10 = "-3.402E+39";

        assertCount(0, "table_real", "a", "=", REAL_MIN_TIMES_10);
        assertCount(5, "table_real", "a", "!=", REAL_MIN_TIMES_10);
        assertCount(0, "table_real", "a", "<", REAL_MIN_TIMES_10);
        assertCount(0, "table_real", "a", "<=", REAL_MIN_TIMES_10);
        assertCount(5, "table_real", "a", ">", REAL_MIN_TIMES_10);
        assertCount(5, "table_real", "a", ">=", REAL_MIN_TIMES_10);
    }

    @Test
    public void real_maxTimesTen() throws Exception {
        String REAL_MAX_TIMES_10 = "3.402E+39";

        assertCount(0, "table_real", "a", "=", REAL_MAX_TIMES_10);
        assertCount(5, "table_real", "a", "!=", REAL_MAX_TIMES_10);
        assertCount(5, "table_real", "a", "<", REAL_MAX_TIMES_10);
        assertCount(5, "table_real", "a", "<=", REAL_MAX_TIMES_10);
        assertCount(0, "table_real", "a", ">", REAL_MAX_TIMES_10);
        assertCount(0, "table_real", "a", ">=", REAL_MAX_TIMES_10);
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // double
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void double_min() throws Exception {
        String DOUBLE_MIN = "-1.79769E+308";

        assertCount(1, "table_double", "a", "=", DOUBLE_MIN);
        assertCount(4, "table_double", "a", "!=", DOUBLE_MIN);
        assertCount(0, "table_double", "a", "<", DOUBLE_MIN);
        assertCount(1, "table_double", "a", "<=", DOUBLE_MIN);
        assertCount(4, "table_double", "a", ">", DOUBLE_MIN);
        assertCount(5, "table_double", "a", ">=", DOUBLE_MIN);
    }

    @Test
    public void double_max() throws Exception {
        String DOUBLE_MAX = "1.79769E+308";

        assertCount(1, "table_double", "a", "=", DOUBLE_MAX);
        assertCount(4, "table_double", "a", "!=", DOUBLE_MAX);
        assertCount(4, "table_double", "a", "<", DOUBLE_MAX);
        assertCount(5, "table_double", "a", "<=", DOUBLE_MAX);
        assertCount(0, "table_double", "a", ">", DOUBLE_MAX);
        assertCount(1, "table_double", "a", ">=", DOUBLE_MAX);
    }

    /* Assert that we have the same behavior as derby when using numeric constant greater than double */
    @Test
    public void double_minTimesTen() throws Exception {
        String DOUBLE_MIN_TIMES_10 = "-1.79769E+309";
        assertException("select * from table_double where a = " + DOUBLE_MIN_TIMES_10, SQLDataException.class,
                "The resulting value is outside the range for the data type DOUBLE.");
    }

    /* Assert that we have the same behavior as derby when using numeric constant greater than double */
    @Test
    public void double_maxTimesTen() throws Exception {
        String DOUBLE_MAX_TIMES_10 = "1.79769E+309";
        assertException("select * from table_double where a = " + DOUBLE_MAX_TIMES_10, SQLDataException.class,
                "The resulting value is outside the range for the data type DOUBLE.");
    }

    /* Assert that we have the same behavior as derby when using numeric constant greater than double */
    @Test
    public void double_maxTimesTen_LessThan() throws Exception {
        String DOUBLE_MAX_TIMES_10 = "1.79769E+309";
        assertException("select * from table_double where a < " + DOUBLE_MAX_TIMES_10, SQLDataException.class,
                "The resulting value is outside the range for the data type DOUBLE.");
    }

    /* Assert that we have the same behavior as derby when using numeric constant greater than double */
    @Test
    public void double_maxTimesTen_GreaterThan() throws Exception {
        String DOUBLE_MAX_TIMES_10 = "1.79769E+309";
        assertException("select * from table_double where a > " + DOUBLE_MAX_TIMES_10, SQLDataException.class,
                "The resulting value is outside the range for the data type DOUBLE.");
    }

    /* Assert that we have the same behavior as derby when using numeric constant greater than double */
    @Test
    public void double_maxTimesTen_GreaterThanConstantOnLeft() throws Exception {
        assertException("select * from table_double where 1.79769E+309 < a", SQLDataException.class,
                "The resulting value is outside the range for the data type DOUBLE.");
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // table_decimal_5_0
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void decimal_5_0_min() throws Exception {
        String DECIMAL_MIN = "-99999.00";

        assertCount(1, "table_decimal_5_0", "a", "=", DECIMAL_MIN);
        assertCount(4, "table_decimal_5_0", "a", "!=", DECIMAL_MIN);
        assertCount(0, "table_decimal_5_0", "a", "<", DECIMAL_MIN);
        assertCount(1, "table_decimal_5_0", "a", "<=", DECIMAL_MIN);
        assertCount(4, "table_decimal_5_0", "a", ">", DECIMAL_MIN);
        assertCount(5, "table_decimal_5_0", "a", ">=", DECIMAL_MIN);
    }

    @Test
    public void decimal_5_0_max() throws Exception {
        String DECIMAL_MAX = "99999.00";

        assertCount(1, "table_decimal_5_0", "a", "=", DECIMAL_MAX);
        assertCount(4, "table_decimal_5_0", "a", "!=", DECIMAL_MAX);
        assertCount(4, "table_decimal_5_0", "a", "<", DECIMAL_MAX);
        assertCount(5, "table_decimal_5_0", "a", "<=", DECIMAL_MAX);
        assertCount(0, "table_decimal_5_0", "a", ">", DECIMAL_MAX);
        assertCount(1, "table_decimal_5_0", "a", ">=", DECIMAL_MAX);
    }


    @Test
    public void decimal_5_0_minMinusOne() throws Exception {
        String DECIMAL_MIN_MINUS_1 = "-100000.00";

        assertCount(0, "table_decimal_5_0", "a", "=", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_5_0", "a", "!=", DECIMAL_MIN_MINUS_1);
        assertCount(0, "table_decimal_5_0", "a", "<", DECIMAL_MIN_MINUS_1);
        assertCount(0, "table_decimal_5_0", "a", "<=", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_5_0", "a", ">", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_5_0", "a", ">=", DECIMAL_MIN_MINUS_1);
    }

    @Test
    public void decimal_5_0_maxPlusOne() throws Exception {
        String DECIMAL_MAX_PLUS_1 = "100000.00";

        assertCount(0, "table_decimal_5_0", "a", "=", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_5_0", "a", "!=", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_5_0", "a", "<", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_5_0", "a", "<=", DECIMAL_MAX_PLUS_1);
        assertCount(0, "table_decimal_5_0", "a", ">", DECIMAL_MAX_PLUS_1);
        assertCount(0, "table_decimal_5_0", "a", ">=", DECIMAL_MAX_PLUS_1);
    }

    // - - - - - - - - - - - - - - - - - - - - - -
    //
    // table_decimal_11_2
    //
    // - - - - - - - - - - - - - - - - - - - - - -

    @Test
    public void decimal_11_2_min() throws Exception {
        String DECIMAL_MIN = "-999999999.99";

        assertCount(1, "table_decimal_11_2", "a", "=", DECIMAL_MIN);
        assertCount(4, "table_decimal_11_2", "a", "!=", DECIMAL_MIN);
        assertCount(0, "table_decimal_11_2", "a", "<", DECIMAL_MIN);
        assertCount(1, "table_decimal_11_2", "a", "<=", DECIMAL_MIN);
        assertCount(4, "table_decimal_11_2", "a", ">", DECIMAL_MIN);
        assertCount(5, "table_decimal_11_2", "a", ">=", DECIMAL_MIN);
    }

    @Test
    public void decimal_11_2_max() throws Exception {
        String DECIMAL_MAX = "999999999.99";

        assertCount(1, "table_decimal_11_2", "a", "=", DECIMAL_MAX);
        assertCount(4, "table_decimal_11_2", "a", "!=", DECIMAL_MAX);
        assertCount(4, "table_decimal_11_2", "a", "<", DECIMAL_MAX);
        assertCount(5, "table_decimal_11_2", "a", "<=", DECIMAL_MAX);
        assertCount(0, "table_decimal_11_2", "a", ">", DECIMAL_MAX);
        assertCount(1, "table_decimal_11_2", "a", ">=", DECIMAL_MAX);
    }


    @Test
    public void decimal_11_2_minMinusOne() throws Exception {
        String DECIMAL_MIN_MINUS_1 = "-10000000000.99";

        assertCount(0, "table_decimal_11_2", "a", "=", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_11_2", "a", "!=", DECIMAL_MIN_MINUS_1);
        assertCount(0, "table_decimal_11_2", "a", "<", DECIMAL_MIN_MINUS_1);
        assertCount(0, "table_decimal_11_2", "a", "<=", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_11_2", "a", ">", DECIMAL_MIN_MINUS_1);
        assertCount(5, "table_decimal_11_2", "a", ">=", DECIMAL_MIN_MINUS_1);
    }

    @Test
    public void decimal_11_2_maxPlusOne() throws Exception {
        String DECIMAL_MAX_PLUS_1 = "10000000000.99";

        assertCount(0, "table_decimal_11_2", "a", "=", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_11_2", "a", "!=", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_11_2", "a", "<", DECIMAL_MAX_PLUS_1);
        assertCount(5, "table_decimal_11_2", "a", "<=", DECIMAL_MAX_PLUS_1);
        assertCount(0, "table_decimal_11_2", "a", ">", DECIMAL_MAX_PLUS_1);
        assertCount(0, "table_decimal_11_2", "a", ">=", DECIMAL_MAX_PLUS_1);
    }


    /**
     * EXECUTES:
     *
     * SELECT * FROM [table] WHERE [operandOne] [operator] [operandTwo]
     * AND
     * SELECT * FROM [table] WHERE [operandTwo] [operator] [operandOnE]
     */
    private void assertCount(int expectedCount, String table, String operandOne, String operator, Object operandTwo) throws Exception {
        String SQL_TEMPLATE = "select * from %s where %s %s %s";
        assertCount(expectedCount, format(SQL_TEMPLATE, table, operandOne, operator, operandTwo));
        String operatorTwo = newOperator(operator);
        assertCount(expectedCount, format(SQL_TEMPLATE, table, operandTwo, operatorTwo, operandOne));
    }

    private void assertCount(int expectedCount, String sql) throws Exception {
        ResultSet rs = methodWatcher.executeQuery(sql);
        assertEquals(format("count mismatch for sql='%s'", sql), expectedCount, count(rs));
    }

    private void assertException(String sql, Class expectedException, String expectedMessage) throws Exception {
        try {
            methodWatcher.executeQuery(sql);
            fail();
        } catch (Exception e) {
            assertEquals(expectedException, e.getClass());
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    private static int count(ResultSet rs) throws SQLException {
        int count = 0;
        while (rs.next()) {
            count++;
        }
        return count;
    }

    private static String newOperator(String operator) {
        if ("<".equals(operator.trim())) {
            return ">";
        }
        if (">".equals(operator.trim())) {
            return "<";
        }
        if ("<=".equals(operator.trim())) {
            return ">=";
        }
        if (">=".equals(operator.trim())) {
            return "<=";
        }
        return operator;
    }

}
