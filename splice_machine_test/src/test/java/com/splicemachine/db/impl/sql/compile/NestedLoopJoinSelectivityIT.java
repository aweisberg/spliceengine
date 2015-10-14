package com.splicemachine.db.impl.sql.compile;

import com.splicemachine.derby.test.framework.SpliceSchemaWatcher;
import com.splicemachine.derby.test.framework.SpliceWatcher;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 *
 *
 *
 */
public class NestedLoopJoinSelectivityIT extends BaseJoinSelectivityIT {
    public static final String CLASS_NAME = NestedLoopJoinSelectivityIT.class.getSimpleName().toUpperCase();
    protected static SpliceWatcher spliceClassWatcher = new SpliceWatcher(CLASS_NAME);
    protected static SpliceSchemaWatcher spliceSchemaWatcher = new SpliceSchemaWatcher(CLASS_NAME);

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(spliceClassWatcher)
            .around(spliceSchemaWatcher);
    @Rule
    public SpliceWatcher methodWatcher = new SpliceWatcher(CLASS_NAME);

    @BeforeClass
    public static void createDataSet() throws Exception {
        createJoinDataSet(spliceClassWatcher, spliceSchemaWatcher.toString());
    }
    @Test
    public void innerJoin() throws Exception {
        rowContainsQuery(
                new int[] {1,3},
                "explain select * from --splice-properties joinOrder=fixed\n ts_10_spk, ts_5_spk --splice-properties joinStrategy=NESTEDLOOP\n where ts_10_spk.c1 = ts_5_spk.c1",methodWatcher,
                "rows=10","NestedLoopJoin");
    }

    @Test
    public void antiJoin() throws Exception {
        rowContainsQuery(
                new int[] {1,5},
                "explain select * from --splice-properties joinOrder=fixed\n ts_10_spk where not exists (select * from  ts_5_spk --splice-properties joinStrategy=NESTEDLOOP\n where ts_10_spk.c1 = ts_5_spk.c1)",methodWatcher,
                "rows=8","MergeSortLeftOuterJoin");
    }

    @Test
    public void leftOuterJoin() throws Exception {
        rowContainsQuery(
                new int[] {1,3},
                "explain select * from --splice-properties joinOrder=fixed\n ts_10_spk left outer join ts_5_spk --splice-properties joinStrategy=NESTEDLOOP\n on ts_10_spk.c1 = ts_5_spk.c1",methodWatcher,
                "rows=10","NestedLoopLeftOuterJoin");
    }

    @Test
    public void rightOuterJoin() throws Exception {
        rowContainsQuery(
                new int[] {1,3},
                "explain select * from ts_10_spk --splice-properties joinStrategy=NESTEDLOOP\n right outer join ts_5_spk on ts_10_spk.c1 = ts_5_spk.c1",methodWatcher,
                "rows=5","NestedLoopRightOuterJoin");
    }

}