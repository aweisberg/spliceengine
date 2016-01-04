package com.splicemachine.storage;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;

import java.io.IOException;
import java.util.Map;

/**
 * @author Scott Fines
 *         Date: 12/17/15
 */
public class HGet implements DataGet{
    private Get get;

    public HGet(byte[] key){
        this.get = new Get(key);
    }

    @Override
    public void setTimeRange(int low,long high){
        assert low <=high :"high < low!";
        try{
            get.setTimeRange(low,high);
        }catch(IOException e){
            //will never happen--the assert protects us
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addColumn(byte[] family,byte[] qualifier){
        get.addColumn(family,qualifier);
    }

    @Override
    public void returnAllVersions(){
        get.setMaxVersions();
    }

    @Override
    public void setFilter(DataFilter txnFilter){
        assert txnFilter instanceof HFilterWrapper;
        Filter toAdd;
        Filter existingFilter=get.getFilter();
        if(existingFilter!=null){
            FilterList fl = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            fl.addFilter(existingFilter);
            fl.addFilter(((HFilterWrapper)txnFilter).unwrapDelegate());
            toAdd = fl;
        }else{
            toAdd = ((HFilterWrapper)txnFilter).unwrapDelegate();
        }
        get.setFilter(toAdd);
    }

    @Override
    public byte[] key(){
        return get.getRow();
    }

    @Override
    public DataFilter filter(){
        return new HFilterWrapper(get.getFilter());
    }

    @Override
    public long highTimestamp(){
        return get.getTimeRange().getMax();
    }

    @Override
    public long lowTimestamp(){
        return get.getTimeRange().getMin();
    }

    @Override
    public void addAttribute(String key,byte[] value){
        get.setAttribute(key,value);
    }

    @Override
    public byte[] getAttribute(String key){
        return get.getAttribute(key);
    }

    @Override
    public Map<String, byte[]> allAttributes(){
        return get.getAttributesMap();
    }

    @Override
    public void setAllAttributes(Map<String, byte[]> attrMap){
        for(Map.Entry<String,byte[]> me:attrMap.entrySet()){
            get.setAttribute(me.getKey(),me.getValue());
        }
    }

    public Get unwrapDelegate(){
        return get;
    }

    public void reset(byte[] rowKey){
        get = new Get(rowKey);
    }
}