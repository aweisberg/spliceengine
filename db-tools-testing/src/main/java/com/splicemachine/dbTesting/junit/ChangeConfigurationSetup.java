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
package com.splicemachine.dbTesting.junit;

import junit.extensions.TestSetup;
import junit.framework.Test;

abstract class ChangeConfigurationSetup extends TestSetup {
    
    private TestConfiguration old;
    
    ChangeConfigurationSetup(Test test)
    {
        super(test);
    }
    
    protected final void setUp()
    {
        old = TestConfiguration.getCurrent();
        TestConfiguration.setCurrent(getNewConfiguration(old));
    }
    
    protected final void tearDown()
    {
        TestConfiguration.setCurrent(old);
    }
    
    /**
     * Return the new configuration to use at setUp time.
     * Most likely based upon the old configuration passed in. 
     * @param old The current configuration.
     * @return new configuration
     */
    abstract TestConfiguration getNewConfiguration(TestConfiguration old);
}
