package com.splicemachine.hbase.writer;

import com.google.common.collect.Lists;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

/**
 * @author Scott Fines
 *         Created on: 8/8/13
 */
public class WriteUtils {


    private static Comparator<BulkWrite> writeComparator = new Comparator<BulkWrite>() {
        @Override
        public int compare(BulkWrite o1, BulkWrite o2) {
            if(o1==null) {
                if(o2==null) return 1;
                else return -1;
            }else if(o2==null)
                return 1;

            else return Bytes.compareTo(o1.getRegionKey(),o2.getRegionKey());
        }
    };

    public static List<BulkWrite> bucketWrites(List<KVPair> buffer,String txnId,Set<HRegionInfo> regions) throws Exception{
        List<BulkWrite> buckets = Lists.newArrayListWithCapacity(regions.size());
        for(HRegionInfo info:regions){

            buckets.add(new BulkWrite(txnId,info.getStartKey()));
        }
        //make sure regions are in sorted order
        Collections.sort(buckets, writeComparator);

        for(KVPair kv:buffer){
            byte[] row = kv.getRow();
            boolean less;
            Iterator<BulkWrite> bucketList = buckets.listIterator();
            BulkWrite bucket = null;
            //we know this iterator has at least one region, otherwise we would have exploded
            do{
                BulkWrite next = bucketList.next();
                int compare = Bytes.compareTo(next.getRegionKey(),row);
                less = compare<0;
                if(compare==0||less){
                    bucket = next;
                }
            }while(bucketList.hasNext() && less);

            bucket.addWrite(kv);
        }

        return buckets;
    }

    static long getWaitTime(int tryNum,long pause) {
        long retryWait;
        if(tryNum>= HConstants.RETRY_BACKOFF.length)
            retryWait = HConstants.RETRY_BACKOFF[HConstants.RETRY_BACKOFF.length-1];
        else
            retryWait = HConstants.RETRY_BACKOFF[tryNum];
        return retryWait*pause;
    }
}
