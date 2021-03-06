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

package com.splicemachine.stats;

import com.splicemachine.encoding.Encoder;
import com.splicemachine.stats.cardinality.CardinalityEstimator;
import com.splicemachine.stats.cardinality.CardinalityEstimators;
import com.splicemachine.stats.estimate.Distribution;
import com.splicemachine.stats.estimate.DistributionFactory;
import com.splicemachine.stats.frequency.FrequencyCounters;
import com.splicemachine.stats.frequency.FrequentElements;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Scott Fines
 *         Date: 2/24/15
 */
public class ComparableColumnStatistics<T extends Comparable<T>> extends BaseColumnStatistics<T> {
    private CardinalityEstimator<T> cardinalityEstimator;
    private FrequentElements<T> frequentElements;
    private T min;
    private T max;
    private DistributionFactory<T> distributionFactory;


    public ComparableColumnStatistics(int columnId,
                                      CardinalityEstimator<T> cardinalityEstimator,
                                      FrequentElements<T> frequentElements,
                                      T min,
                                      T max,
                                      long totalBytes,
                                      long totalCount,
                                      long nullCount,
                                      long minCount,
                                      DistributionFactory<T> distributionFactory) {
        super(columnId, totalBytes, totalCount, nullCount,minCount);
        this.cardinalityEstimator = cardinalityEstimator;
        this.frequentElements = frequentElements;
        this.min = min;
        this.max = max;
        this.distributionFactory = distributionFactory;
    }

    @Override public long cardinality() { return cardinalityEstimator.getEstimate(); }
    @Override public FrequentElements<T> topK() { return frequentElements; }
    @Override public T minValue() { return min; }
    @Override public T maxValue() { return max; }

    @Override
    public String toString() {
        return String.format("ComparableColumnStatistics{ cardinalityEstimator=%s, frequentElements=%s, min=%s, max=%s, distributionFactory=%s}",
                cardinalityEstimator,frequentElements,min,max,distributionFactory);
    }

    @Override
    public Distribution<T> getDistribution() {
        return distributionFactory.newDistribution(this);
    }

    @Override
    public ColumnStatistics<T> getClone() {
        return new ComparableColumnStatistics<>(columnId,cardinalityEstimator.getClone(),
                frequentElements.getClone(),
                //TODO -sf- is this safe?
                min,
                max,
                totalBytes,totalCount,nullCount,minCount,distributionFactory);
    }

    @Override
    public CardinalityEstimator getCardinalityEstimator() {
        return cardinalityEstimator;
    }

    @Override
    public ColumnStatistics<T> merge(ColumnStatistics<T> other) {
        cardinalityEstimator = cardinalityEstimator.merge(other.getCardinalityEstimator());
        frequentElements = frequentElements.merge(other.topK());
        /*
         * We need to check for null here, because it's possible that an entire partition consists
         * of nothing but null values. It's unlikely, but hey! you never know, and when it happens,
         * we don't want a NullPointer to be thrown.
         */
        if(min==null || (other.minValue()!=null && other.minValue().compareTo(min)>0))
            min = other.minValue();
        if(max==null || (other.maxValue() !=null && other.maxValue().compareTo(max)<0))
            max = other.maxValue();
        totalBytes+=other.totalBytes();
        totalCount+=other.nullCount()+other.nonNullCount();
        nullCount+=other.nullCount();
        return this;
    }

    public static <T extends Comparable<T>> Encoder<ComparableColumnStatistics<T>> encoder(Encoder<T> typeEncoder,
                                                                                           DistributionFactory<T> distFactory){
        return new EncDec<>(typeEncoder,distFactory);
    }

    static class EncDec<T extends Comparable<T>> implements Encoder<ComparableColumnStatistics<T>> {
        private Encoder<T> valueEncoder;
        private DistributionFactory<T> distributionFactory;

        public EncDec(Encoder<T> valueEncoder,DistributionFactory<T> distributionFactory) {
            this.valueEncoder = valueEncoder;
            this.distributionFactory = distributionFactory;
        }

        @Override
        public void encode(ComparableColumnStatistics<T> item,DataOutput encoder) throws IOException {
            BaseColumnStatistics.write(item,encoder);
            valueEncoder.encode(item.min, encoder);
            valueEncoder.encode(item.max, encoder);
            CardinalityEstimators.<T>objectEncoder().encode(item.cardinalityEstimator, encoder);
            FrequencyCounters.objectEncoder(valueEncoder).encode(item.frequentElements,encoder);
        }

        @Override
        public ComparableColumnStatistics<T> decode(DataInput decoder) throws IOException {
            int columnId = decoder.readInt();
            long totalBytes = decoder.readLong();
            long totalCount = decoder.readLong();
            long nullCount = decoder.readLong();
            long minCount = decoder.readLong();
            T min = valueEncoder.decode(decoder);
            T max = valueEncoder.decode(decoder);
            CardinalityEstimator<T> cardinalityEstimator = CardinalityEstimators.<T>objectEncoder().decode(decoder);
            FrequentElements<T> frequentElements = FrequencyCounters.objectEncoder(valueEncoder).decode(decoder);
            return new ComparableColumnStatistics<>(columnId,
                    cardinalityEstimator,
                    frequentElements,
                    min,
                    max,
                    totalBytes,
                    totalCount,
                    nullCount,
                    minCount,
                    distributionFactory);
        }
    }

}
