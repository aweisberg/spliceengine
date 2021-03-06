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

package com.splicemachine.utils;

import java.util.HashSet;

/**
 * Utilities for dealing with primitive integer arrays.
 *
 * @author Scott Fines
 * Date: 11/15/13
 */
public class IntArrays {

		private IntArrays(){} //don't instantiate utility classes!

		public static int[] complement(int[] map,int size){
				int[] complement = count(size);
				for(int pos:map){
						complement[pos] = -1;
				}
				return complement;
		}

    /**
     * This method creates an array of indexes but filters indexes contained in <code>filterMap</code>.<br/>
     * The indexes in the resulting list are sequential but have gaps at locations where filtered indexes
     * were specified.
     * <p/>
     * Examples:<br/>
     * <pre>
     FilterMap: [0, 1, 2, 3, 4, 5] Size: 6  // filter everything
     Result: []

     FilterMap: [] Size: 6                  // filter nothing
     Result: [0, 1, 2, 3, 4, 5]

     FilterMap: [0] Size: 5                 // filter index 0
     Result: [1, 2, 3, 4]

     FilterMap: [1, 2] Size: 6              // filter indexes 1 and 2
     Result: [0, 3, 4, 5]

     FilterMap: [3, 4] Size: 6              // filter indexes 3 and 4
     Result: [0, 1, 2, 5]
     * </pre>
     *
     * @param filterMap indexes that you <b>DO NOT</b> want to see in the result.
     * @param size the size of the original list of indexes (which we don't see)
     *             from which to remove the given indexes.
     * @return an array of indexes with those in the <code>filterMap</code> skipped.
     */
    public static int[] complementMap(int[] filterMap, int size) {
        // track columns we need to filter
        HashSet<Integer> columnsToFilter = new HashSet<Integer>(filterMap.length);
        // determine the number of columns to filter
        int numMissingFields = 0;
        for (int i=0; i<filterMap.length; i++) {
            if (filterMap[i] >= 0) numMissingFields++;
            columnsToFilter.add(filterMap[i]);
        }

        int mapSize = size - numMissingFields;
        assert mapSize >= 0 : "Cannot construct a complement with more missing fields than present!";
        if (mapSize == 0) return new int[]{};

        int[] finalData = new int[mapSize];
        int index = 0;
        int filter = 0;
        while (index < mapSize) {
            if (! columnsToFilter.contains(filter)) {
                // add index only if it's not filtered
                finalData[index++] = filter;
            }
            filter++;
        }
        return finalData;
    }

		public static int[] intersect(int[] map,int size){
			int[] intersect = negativeInitialize(size);
			for(int pos:map){
				intersect[pos] = pos;
			}
			return intersect;
		}
		
		private static int max(int[] map) {
				int max = Integer.MIN_VALUE;
				for (int aMap : map) {
						if (aMap > max)
								max = aMap;
				}
				return max;
		}

		public static int[] count(int size){
				int[] newInts = new int[size];
				for(int i=0;i<size;i++){
						newInts[i] = i;
				}
				return newInts;
		}
		public static int[] negativeInitialize(int size){
			int[] newInts = new int[size];
			for(int i=0;i<size;i++){
					newInts[i] = -1;
			}
			return newInts;
	}
}
