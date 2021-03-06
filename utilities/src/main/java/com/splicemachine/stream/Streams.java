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

package com.splicemachine.stream;

import com.splicemachine.concurrent.traffic.TrafficController;
import com.splicemachine.metrics.Metrics;
import com.splicemachine.metrics.Stats;

import java.util.Iterator;

/**
 * Utility classes around Streams.
 *
 * @author Scott Fines
 * Date: 8/13/14
 */
public class Streams {

    @SafeVarargs public static <T> Stream<T> of(T...elements){ return new ArrayStream<>(elements); }
    public static <T> Stream<T> wrap(Iterator<T> iterator){ return new IteratorStream<>(iterator); }
    public static <T> Stream<T> wrap(Iterable<T> iterable){ return new IteratorStream<>(iterable.iterator()); }
    public static <T> PeekableStream<T> peekingStream(Stream<T> stream){ return new PeekingStream<>(stream); }
    public static <T> Stream<T> rateLimit(Stream<T> stream,TrafficController rateLimiter){
        return new RateLimitedStream<T>(stream,rateLimiter);
    }

    @SuppressWarnings("unchecked") public static <T> Stream<T> empty(){ return (Stream<T>)EMPTY; }

    @SuppressWarnings("unchecked") public static <T,V extends Stats> MeasuredStream<T,V> measuredEmpty(){ return (MeasuredStream<T,V>)MEASURED_EMPTY; }

    /******************************************************************************************************************/
    /*private helper methods and classes*/

    private static final AbstractStream EMPTY =  new AbstractStream() {
        @Override public Object next() throws StreamException { return null; }
        @Override public void close() throws StreamException {  }
    };

    private static final AbstractMeasuredStream MEASURED_EMPTY = new AbstractMeasuredStream() {
        @Override public Stats getStats() { return Metrics.noOpIOStats(); }
        @Override public Object next() throws StreamException { return null; }
        @Override public void close() throws StreamException {  }
    };

    private static class RateLimitedStream<T> extends AbstractStream<T>{
        private final Stream<T> delegate;
        private final TrafficController controller;

        public RateLimitedStream(Stream<T> delegate, TrafficController controller) {
            this.delegate = delegate;
            this.controller = controller;
        }

        @Override
        public T next() throws StreamException {
            try {
                controller.acquire(1);
            } catch (InterruptedException e) {
                throw new StreamException(e);
            }
            return delegate.next();
        }

        @Override public void close() throws StreamException { delegate.close(); }
    }
    static final class FilteredStream<T> extends ForwardingStream<T>{
        private final Predicate<T> predicate;

        public FilteredStream(Stream<T> delegate,Predicate<T> predicate) {
            super(delegate);
            this.predicate = predicate;
        }

        @Override
        public T next() throws StreamException {
            T n;
            while((n = delegate.next())!=null){
                if(predicate.apply(n)) return n;
            }
            return null;
        }
    }

    static final class LimitedStream<T> extends ForwardingStream<T> {
        private final long maxSize;
        private long numReturned;

        public LimitedStream(Stream<T> stream, long maxSize) {
            super(stream);
            this.maxSize = maxSize;
        }

        @Override
        public T next() throws StreamException {
            if(numReturned>maxSize) return null;
            T n = delegate.next();
            if(n==null)
                numReturned = maxSize+1; //prevent extraneous calls to the underlying stream
            return n;
        }
    }

    private static class IteratorStream<T> extends AbstractStream<T> {
        /*
         * Stream representation of an Iterator
         */
        private final Iterator<T> iterator;

        private IteratorStream(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public T next() throws StreamException {
            if(!iterator.hasNext()) return null;
            return iterator.next();
        }

        @Override public void close() throws StreamException { }//no-op
    }

    private static class ArrayStream<T> extends AbstractStream<T> {
        /*
         * Stream representation of a fixed array
         */
        private final T[] stream;
        private int position = 0;

        private ArrayStream(T[] stream) {
            this.stream = stream;
        }

        @Override
        public T next() throws StreamException {
            if(position>=stream.length) return null;
            T n =  stream[position];
            position++;
            return n;
        }

        @Override public void close() throws StreamException {  } //no-op
    }

    private static class PeekingStream<T> extends ForwardingStream<T> implements PeekableStream<T> {
        /*
         * Peekable version of any stream
         */
        private T n;

        public PeekingStream(Stream<T> stream) {
            super(stream);
        }

        @Override
        public T peek() throws StreamException {
            if(n!=null) return n;
            n = delegate.next();
            return n;
        }

        @Override
        public void take() {
            assert n!=null: "Called take without first calling peek!";
            n = null; //strip away n;
        }

        @Override
        public T next() throws StreamException {
            T next = peek();
            n = null; //strip n away
            return next;
        }
    }

}
