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

package com.splicemachine.db.iapi.services.io;

import java.io.InputStream;

/**
 * This is a simple interface that is used by streams that can clone themselves.
 * <p>
 * The purpose is for the implementation of BLOB/CLOB (and potentially other
 * types whose value is represented by a stream), for which their size makes it
 * impossible or very expensive to materialize the value.
 */
public interface CloneableStream {

    /**
     * Clone the stream.
     * <p>
     * To be used when a "deep" clone of a stream is required rather than
     * multiple references to the same stream.
     * <p>
     * The resulting clone should support reads, resets, closes which 
     * do not affect the original stream source of the clone.
     *
     * @return The cloned stream.
     */
    public InputStream cloneStream() ;
}
