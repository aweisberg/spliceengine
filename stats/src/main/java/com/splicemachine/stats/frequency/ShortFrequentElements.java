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

package com.splicemachine.stats.frequency;

import org.sparkproject.guava.primitives.Longs;
import org.sparkproject.guava.primitives.Shorts;
import com.splicemachine.encoding.Encoder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @author Scott Fines
 *         Date: 12/5/14
 */
public abstract class ShortFrequentElements implements FrequentElements<Short> {
    private static final Comparator<ShortFrequencyEstimate> naturalComparator = new Comparator<ShortFrequencyEstimate>() {
        @Override
        public int compare(ShortFrequencyEstimate o1, ShortFrequencyEstimate o2) {
            int compare = Shorts.compare(o1.value(), o2.value());
            if(compare!=0) return compare;
            return Long.signum(o1.count()-o2.count()); //should never happen, but just in case
        }
    };

    private NavigableSet<ShortFrequencyEstimate> elements;
    private long totalCount;

    private ShortFrequentElements(long totalCount,Collection<ShortFrequencyEstimate> elements) {
        TreeSet<ShortFrequencyEstimate> elems = new TreeSet<>(naturalComparator);
        elems.addAll(elements);
        this.elements = elems;
        this.totalCount = totalCount;
    }

    public static ShortFrequentElements topK(int k, long totalCount,Collection<ShortFrequencyEstimate> elements){
        return new TopK(k,totalCount,elements);
    }

    public static ShortFrequentElements heavyHitters(float support, long totalCount,Collection<ShortFrequencyEstimate> elements){
        return new HeavyItems(support,totalCount,elements);
    }

    @Override
    public Set<? extends FrequencyEstimate<Short>> allFrequentElements() {
        return Collections.unmodifiableSet(elements);
    }

    public ShortFrequencyEstimate countEqual(short item){
        for(ShortFrequencyEstimate est:elements){
            if(est.value()==item) return est;
        }
        return new ZeroFreq(item);

    }

    public Set<ShortFrequencyEstimate> frequentBetween(short start, short stop, boolean includeStart, boolean includeStop){
        ShortFrequencyEstimate first = elements.first();
        if(first.value()>stop) return Collections.emptySet();
        else if(first.value()==stop){
            if(includeStop) return Collections.singleton(first);
            else return Collections.emptySet();
        }else if(first.value()>start || (includeStart && first.value()==start))
            return frequentBefore(stop,includeStop);

        ShortFrequencyEstimate last = elements.last();
        if(last.value()<start) return Collections.emptySet();
        else if(last.value()==start){
            if(includeStart) return Collections.singleton(last);
            else return Collections.emptySet();
        }else if(last.value()<stop || (includeStop && last.value()==stop)) return frequentAfter(start,includeStart);

        ZeroFreq est = new ZeroFreq(start);
        ShortFrequencyEstimate rangeStart = elements.ceiling(est);
        est.item = stop;
        ShortFrequencyEstimate rangeStop = elements.ceiling(est);
        return Collections.unmodifiableSet(elements.subSet(rangeStart,includeStart,rangeStop,includeStop));
    }

    public Set<ShortFrequencyEstimate> frequentAfter(short start, boolean includeStart){
        ShortFrequencyEstimate first = elements.first();
        if(first.value()>start) return elements;
        else if(first.value()==start){
            if(includeStart) return elements;
            else return elements.tailSet(first,false);
        }
        ShortFrequencyEstimate last = elements.last();
        if(last.value()<start) return Collections.emptySet();
        else if(last.value()==start){
            if(includeStart) return Collections.singleton(last);
            else return Collections.emptySet();
        }

        //find the starting value
        ShortFrequencyEstimate rangeStart = elements.ceiling(new ZeroFreq(start));
        return Collections.unmodifiableSet(elements.tailSet(rangeStart,includeStart));
    }

    public Set<ShortFrequencyEstimate> frequentBefore(short stop, boolean includeStop){
        ShortFrequencyEstimate first = elements.first();
        if(first.value()>stop)return Collections.emptySet();
        else if(first.value()==stop){
            if(!includeStop) return Collections.emptySet();
            else return Collections.singleton(first);
        }
        ShortFrequencyEstimate last = elements.last();
        if(last.value()<stop) return elements;
        else if(last.value()==stop){
            if(includeStop) return elements;
            else return elements.headSet(last);
        }

        ShortFrequencyEstimate rangeStop = elements.higher(new ZeroFreq(stop));
        return Collections.unmodifiableSet(elements.headSet(rangeStop,includeStop));
    }

    @Override
    public FrequencyEstimate<? extends Short> equal(Short item) {
        assert item!=null: "Cannot estimate null matches!";
        return countEqual(item);
    }

    @Override
    public Set<? extends FrequencyEstimate<Short>> frequentElementsBetween(Short start, Short stop, boolean includeMin, boolean includeStop) {
        if(start==null){
            if(stop==null){
                return elements;
            }else
                return frequentBefore(stop,includeStop);
        }else if(stop==null){
            return frequentAfter(start,includeMin);
        }else{
            if(start.equals(stop)){
                if(includeMin||includeStop) return Collections.singleton(countEqual(start));
                else return Collections.emptySet();
            }else
                return frequentBetween(start,stop,includeMin,includeStop);
        }
    }

    @Override
    public FrequentElements<Short> merge(FrequentElements<Short> other) {
        assert other instanceof ShortFrequentElements: "Cannot merge instance of type "+ other.getClass();
        return merge((ShortFrequentElements)other);
    }

    public ShortFrequentElements merge(ShortFrequentElements other) {
        NavigableSet<ShortFrequencyEstimate> merged = this.elements;
        NavigableSet<ShortFrequencyEstimate> otherEstimates = other.elements;
        /*
         * We need to pick out *maxSize* most common elements from both sets. Since this
         * is relatively small data, we will maintain a set in occurrance order, and add everything
         * from both sets, merging as we go. In order to effectively do that, we use a simple array,
         * add everything in, then add back in the number of elements that we need.
         */
        int totalSize = merged.size()+otherEstimates.size();
        ShortFrequencyEstimate[] topK = new ShortFrequencyEstimate[totalSize]; //assume no intersection
        int size =0;
        for(ShortFrequencyEstimate estimate:merged){
            topK[size] = estimate;
            size++;
        }
        for(ShortFrequencyEstimate otherEst:otherEstimates){
            boolean found = false;
            for(int i=0;i<size;i++){
                ShortFrequencyEstimate existingEst = topK[i];
                if(existingEst.equals(otherEst)){
                    topK[i] = (ShortFrequencyEstimate)existingEst.merge(otherEst);
                    found=true;
                    break;
                }
            }
            if(!found) {
                topK[size] = new ShortValueEstimate(otherEst.value(),otherEst.count(),otherEst.error());
                size++;
            }
        }
        this.totalCount+=other.totalCount;
        this.elements = rebuild(totalCount,topK,size);

        return this;
    }


    public Encoder<ShortFrequentElements> encoder(){ return EncoderDecoder.INSTANCE; }

    @Override public FrequentElements<Short> getClone() { return newCopy(); }

    public ShortFrequentElements newCopy(){
        Collection<ShortFrequencyEstimate> ests = new TreeSet<>(naturalComparator);
        for(ShortFrequencyEstimate est:elements){
            ests.add(new ShortValueEstimate(est.value(),est.count(),est.error()));
        }
        return getNew(totalCount,ests);
    }

    @Override
    public long totalFrequentElements() {
        long total = 0l;
        for(FrequencyEstimate<Short> est:elements){
            total+=est.count();
        }
        return total;
    }

    protected abstract NavigableSet<ShortFrequencyEstimate> rebuild(long mergedCount,ShortFrequencyEstimate[] topK,int size);

    protected abstract ShortFrequentElements getNew(long totalCount, Collection<ShortFrequencyEstimate> ests);

    /* ****************************************************************************************************************/
    /*private helper methods*/
    private static class TopK extends ShortFrequentElements{
        private final int k;
        private static final Comparator<? super ShortFrequencyEstimate> frequencyComparator = new Comparator<ShortFrequencyEstimate>() {
            @Override
            public int compare(ShortFrequencyEstimate o1, ShortFrequencyEstimate o2) {
                int compare = Longs.compare(o1.count(), o2.count());
                if(compare!=0)
                    return compare;
                return o1.compareTo(o2);
            }
        };

        private TopK(int k,long totalCount, Collection<ShortFrequencyEstimate> elements) {
            super(totalCount, elements);
            this.k = k;
        }

        @Override
        protected NavigableSet<ShortFrequencyEstimate> rebuild(long mergedCount, ShortFrequencyEstimate[] topK,int size) {
            Arrays.sort(topK,0,size,frequencyComparator);
            int k = Math.min(this.k,size);
            NavigableSet<ShortFrequencyEstimate> newElements = new TreeSet<>(naturalComparator);
            //noinspection ManualArrayToCollectionCopy
            for(int i=0;i<k;i++){
                newElements.add(topK[i]);
            }
            return newElements;
        }

        @Override
        protected ShortFrequentElements getNew(long totalCount, Collection<ShortFrequencyEstimate> ests) {
            return new TopK(k,totalCount,ests);
        }
    }

    private static class HeavyItems extends ShortFrequentElements{
        private final float support;
        private HeavyItems(float support,long totalCount, Collection<ShortFrequencyEstimate> elements) {
            super(totalCount, elements);
            this.support = support;
        }

        @Override
        protected NavigableSet<ShortFrequencyEstimate> rebuild(long mergedCount, ShortFrequencyEstimate[] topK,int size) {
            NavigableSet<ShortFrequencyEstimate> result = new TreeSet<>(naturalComparator);
            long threshold = (long)(mergedCount*support);
            //noinspection ForLoopReplaceableByForEach
            for(int i=0;i< size;i++){
                ShortFrequencyEstimate est = topK[i];
                if(est.count()>threshold)
                    result.add(est);
            }
            return result;
        }

        @Override
        protected ShortFrequentElements getNew(long totalCount, Collection<ShortFrequencyEstimate> ests) {
            return new HeavyItems(support,totalCount,ests);
        }
    }
    private class ZeroFreq implements ShortFrequencyEstimate {
        private short item;

        public ZeroFreq(short item) {
            this.item = item;
        }

        @Override public short value() { return item; }
        @SuppressWarnings("NullableProblems")
        @Override public int compareTo(ShortFrequencyEstimate o) { return Shorts.compare(item,o.value()); }
        @Override public Short getValue() { return item; }
        @Override public long count() { return 0; }
        @Override public long error() { return 0; }
        @Override public FrequencyEstimate<Short> merge(FrequencyEstimate<Short> other) { return other; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShortFrequencyEstimate)) return false;

            ShortFrequencyEstimate zeroFreq = (ShortFrequencyEstimate) o;

            return item == zeroFreq.value();
        }

        @Override public int hashCode() { return item; }
    }

    static class EncoderDecoder implements Encoder<ShortFrequentElements> {
        public static final EncoderDecoder INSTANCE = new EncoderDecoder(); //singleton pattern

        @Override
        public void encode(ShortFrequentElements item, DataOutput dataInput) throws IOException {
            dataInput.writeLong(item.totalCount);
            encodeSet(item.elements,dataInput);
            if(item instanceof TopK) {
                dataInput.writeBoolean(true);
                dataInput.writeInt(((TopK)item).k);
            }else {
                dataInput.writeBoolean(false);
                dataInput.writeFloat(((HeavyItems)item).support);
            }

        }

        @Override
        public ShortFrequentElements decode(DataInput input) throws IOException {
            long totalCount = input.readLong();
            Set<ShortFrequencyEstimate> estimates = decodeSet(input);
            if(input.readBoolean()){
                int k = input.readInt();
                return new TopK(k,totalCount,estimates);
            }else{
                float support = input.readFloat();
                return new HeavyItems(support,totalCount,estimates);
            }
        }

        private void encodeSet(NavigableSet<ShortFrequencyEstimate> elements, DataOutput dataInput) throws IOException {
            dataInput.writeShort(elements.size());
            for(ShortFrequencyEstimate element:elements){
                dataInput.writeShort(element.value());
                dataInput.writeLong(element.count());
                dataInput.writeLong(element.error());
            }
        }

        private Set<ShortFrequencyEstimate> decodeSet(DataInput input) throws IOException{
            int size = input.readShort();
            Set<ShortFrequencyEstimate> set = new TreeSet<>(naturalComparator);
            for(int i=0;i<size;i++){
                short v = input.readShort();
                long c = input.readLong();
                long eps = input.readLong();
                set.add(new ShortValueEstimate(v,c,eps));
            }
            return set;
        }
    }
}
