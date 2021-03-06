/*
 * Apache Derby is a subproject of the Apache DB project, and is licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use these files
 * except in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Splice Machine, Inc. has modified this file.
 *
 * All Splice Machine modifications are Copyright 2012 - 2016 Splice Machine, Inc.,
 * and are licensed to you under the License; you may not use this file except in
 * compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.splicemachine.db.iapi.services.cache;

/**
	A factory for handing out caches.
*/
public interface CacheFactory {
	
	/**
		Create a cache that uses the class represented by holderClass as
		the holder class. This holderClass must implement Cacheable.

		@param holderFactory The factory for the objects that are to be cached.
		@param name			The name of the cache
		@param initialSize	The number of initial holder objects that will be created
		@param maximumSize  The maximum number of objects the cache will hold
 
	*/
	
	public CacheManager newCacheManager(CacheableFactory holderFactory, String name,
										int initialSize, int maximumSize);
}

