package com.splicemachine.derby.test.framework;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.runner.Description;

public class SpliceUnitTest {
	public String getSchemaName() {
		Class<?> enclosingClass = getClass().getEnclosingClass();
		if (enclosingClass != null)
		    return enclosingClass.getSimpleName().toUpperCase();
		else
		    return getClass().getSimpleName().toUpperCase();
	}
	
	public String getTableReference(String tableName) {
		return getSchemaName() + "." + tableName;
	}

	public String getPaddedTableReference(String tableName) {
		return " " + getSchemaName() + "." + tableName.toUpperCase()+ " ";
	}

	
	public static int resultSetSize(ResultSet rs) throws Exception {
		int i = 0;
		while (rs.next()) {
			i++;
		}
		return i;
	}

    public static int columnWidth(ResultSet rs ) throws SQLException {
        return rs.getMetaData().getColumnCount();
    }

	public static String format(String format, Object...args) {
		return String.format(format, args);
	}
	public static String getBaseDirectory() {
		String userDir = System.getProperty("user.dir");
	    if(!userDir.endsWith("splice_machine_test"))
	    	userDir = userDir+"/splice_machine_test/";
	    return userDir;
	}

    public static String getResourceDirectory() {
		return getBaseDirectory()+"/src/test/test-data/";
	}


    public static class MyWatcher extends SpliceTableWatcher {

        public MyWatcher(String tableName, String schemaName, String createString) {
            super(tableName, schemaName, createString);
        }

        public void create(Description desc) {
            super.starting(desc);
        }
    }


}