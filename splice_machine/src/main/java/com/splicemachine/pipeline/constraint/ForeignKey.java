package com.splicemachine.pipeline.constraint;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectOpenHashSet;
import com.splicemachine.hbase.KVPair;
import com.splicemachine.si.api.TxnView;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Representation of a ForeignKey Constraint.
 *
 * @author Scott Fines
 * Created on: 2/28/13
 */
public class ForeignKey implements Constraint{
    public static final byte[] FOREIGN_KEY_FAMILY = "fk".getBytes();
    public static final byte[] FOREIGN_KEY_COLUMN = "fk".getBytes();
    /*
     * The columns in the foreign key table to get values for
     */
    private final BitSet fkCols;

    /*
     * The table holding the primary key that this Foreign Key is referencing
     */
    private final String refTableName;
    //for performance efficiency
    private final byte[] refTableBytes;

    private final byte[] mainTableBytes;

    public ForeignKey(String refTableName,String mainTable,BitSet fkCols)  {
        this.fkCols = fkCols;
        this.refTableName = refTableName;
        this.refTableBytes = Bytes.toBytes(refTableName);
        this.mainTableBytes = Bytes.toBytes(mainTable);
    }

//    @Override
    public boolean validate(Put put,RegionCoprocessorEnvironment rce) throws IOException{
//        Get get = SpliceUtils.createGet(... Constraints.getReferencedRowKey(put, fkCols));
//        get.addFamily(SpliceConstants.DEFAULT_FAMILY_BYTES);
        return true;// TODO -sf- implement
    }

//    @Override
    public boolean validate(Delete delete,RegionCoprocessorEnvironment rce) throws IOException{
       //foreign keys are validated on the PK side of deletes, so nothing to validate
        return true;
    }

    public void updateForeignKey(Put put) throws IOException{
//        byte[] referencedRowKey = Constraints.getReferencedRowKey(put, fkCols);
//        if(referencedRowKey==null)
//            throw new DoNotRetryIOException("Foreign Key Constraint Violation");

//        tableSource.getTable(refTableBytes).incrementColumnValue(referencedRowKey,
//                FOREIGN_KEY_FAMILY,FOREIGN_KEY_COLUMN,1l);
    }

    public void updateForeignKey(Delete delete) throws IOException{
//        Get get = SpliceUtils.createGet(...);
//        for(int fk = fkCols.nextSetBit(0);fk!=-1;fk=fkCols.nextSetBit(fk+1)){
//            get.addColumn(SpliceConstants.DEFAULT_FAMILY_BYTES,Integer.toString(fk).getByteCopy());
//        }
//        HTableInterface table = tableSource.getTable(mainTableBytes);
//        Result result = table.get(get);
//        if(result==null){
            //don't know why this would be, we're about to delete it!
            //oh well, guess we don't have to do anything
//            return;
//        }
//        byte[] referencedRowKey = Constraints.getReferencedRowKey(
//                result.getFamilyMap(SpliceConstants.DEFAULT_FAMILY_BYTES), fkCols);
//        if(referencedRowKey==null) return; //nothing to update!
//        table.incrementColumnValue(FOREIGN_KEY_FAMILY,FOREIGN_KEY_COLUMN,referencedRowKey,-1l);
    }

		@Override
		public BatchConstraintChecker asChecker() {
				throw new UnsupportedOperationException();
		}

		@Override
    public Type getType() {
        return Type.FOREIGN_KEY;
    }

    @Override
    public ValidationType validate(KVPair mutation, TxnView txn,RegionCoprocessorEnvironment rce,ObjectOpenHashSet<KVPair> priors) throws IOException {
        throw new UnsupportedOperationException("Foreign Keys are not yet implemented");
    }

    @Override
    public Collection<KVPair> validate(Collection<KVPair> mutations, TxnView txn,RegionCoprocessorEnvironment rce,ObjectOpenHashSet<KVPair> priors) throws IOException {
        return Collections.emptyList();
    }

    @Override
    public ConstraintContext getConstraintContext() {
        return null;
    }
}