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

package com.splicemachine.mrio.api.core;

import com.splicemachine.derby.test.framework.SpliceDataWatcher;
import com.splicemachine.derby.test.framework.SpliceSchemaWatcher;
import com.splicemachine.derby.test.framework.SpliceTableWatcher;
import com.splicemachine.derby.test.framework.SpliceWatcher;
import com.splicemachine.mrio.MRConstants;
import com.splicemachine.si.constants.SIConstants;
import org.apache.hadoop.hbase.client.*;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Ignore
public class SpliceMemstoreKeyValueScannerIT extends BaseMRIOTest{
    private static final Logger LOG = Logger.getLogger(SpliceMemstoreKeyValueScannerIT.class);
    protected static String SCHEMA_NAME=SpliceMemstoreKeyValueScannerIT.class.getSimpleName();
	protected static SpliceWatcher spliceClassWatcher = new SpliceWatcher();
	protected static SpliceSchemaWatcher spliceSchemaWatcher = new SpliceSchemaWatcher(SCHEMA_NAME);
	protected static SpliceTableWatcher spliceTableWatcherA = new SpliceTableWatcher("A",SCHEMA_NAME,"(col1 int, col2 varchar(56), primary key (col1))");
	
	@ClassRule 
	public static TestRule chain = RuleChain.outerRule(spliceClassWatcher)
		.around(spliceSchemaWatcher)
		.around(spliceTableWatcherA)
		.around(new SpliceDataWatcher(){
			@Override
			protected void starting(Description description) {
				try {
					spliceClassWatcher.setAutoCommit(false);
					for (int i = 0; i< 500; i++) {
						if (i%10 != 0)
							continue;							
						PreparedStatement psA = spliceClassWatcher.prepareStatement("insert into "+ SCHEMA_NAME+ ".A (col1,col2) values (?,?)");
						psA.setInt(1,i);
						psA.setString(2, "dataset"+i);
						psA.executeUpdate();
						if (i==250)
							flushTable(SCHEMA_NAME+".A");
					}
					
					for (int i = 0; i< 500; i++) {
						if (i%10 == 0) {
							PreparedStatement psA = spliceClassWatcher.prepareStatement("update "+ SCHEMA_NAME+ ".A set col2 = ? where col1 = ?");
							psA.setString(1, "datasetupdate"+i);
							psA.setInt(2,i);
							psA.executeUpdate();
						} else {
							PreparedStatement psA = spliceClassWatcher.prepareStatement("insert into "+ SCHEMA_NAME+ ".A (col1,col2) values (?,?)");
							psA.setInt(1,i);
							psA.setString(2, "dataset"+i);
							psA.executeUpdate();
						}
					}
					spliceClassWatcher.commit();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				finally {
					spliceClassWatcher.closeAll();
				}
			}
			
		});
	
	@Rule public SpliceWatcher methodWatcher = new SpliceWatcher();

	@Test
	@Ignore
	public void testFlushHandling() throws SQLException, IOException, InterruptedException {
    	int i = 0;
    	HBaseAdmin admin = null;
    	ResultScanner rs = null;
    	HTable htable = null;
		try {
			String tableName = sqlUtil.getConglomID(SCHEMA_NAME+".A");
	    	htable = new HTable(config,tableName);
	    	Scan scan = new Scan();
	    	scan.setCaching(50);
	    	scan.setBatch(50);
	    	scan.setMaxVersions();
	    	scan.setAttribute(MRConstants.SPLICE_SCAN_MEMSTORE_ONLY, SIConstants.EMPTY_BYTE_ARRAY);
	    	rs = htable.getScanner(scan);
	    	Result result;
	    	boolean flush = false;
	    	while (  ((result = rs.next()) != null) && !result.isEmpty() && !flush) {
	    		i++;
//	    		if (i ==1)
//	    			Assert.assertTrue("First Row Beginning Marker Missing",impl.getDataLib().singleMatchingFamily(impl.getDataLib().getDataFromResult(result)[0], MRConstants.HOLD));
//	    		System.out.println(i + " --> " + result);
//	    		flush = impl.getDataLib().singleMatchingFamily(impl.getDataLib().getDataFromResult(result)[0], MRConstants.FLUSH);
	    		if (i == 201)
	    			Assert.assertTrue("201 Should be a Flush...", flush);
		    	if (i==200)
		    		flushTable(SCHEMA_NAME+".A");
	    	}	    	
		}
    	finally {
			if (htable != null)
    			htable.close();
    		if (rs != null)
   			rs.close();
    	}
	}
	
}
