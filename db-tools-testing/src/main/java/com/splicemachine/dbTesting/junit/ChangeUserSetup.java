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

import junit.framework.Test;

/**
 * A decorator that changes the default user and password
 * for the current configuration. Its tearDown method restores
 * the previous configuration.
 * Optionally the passwordToken can be changed as well. 
 * 
 */
final class ChangeUserSetup extends ChangeConfigurationSetup {
    
    private final String user;
    private final String password;
    private final String passwordToken;

    ChangeUserSetup(Test test, String user, String password, String passwordToken)
    {
        super(test);
        this.user = user;
        this.password = password;
        this.passwordToken = passwordToken;
    }
    
    ChangeUserSetup(Test test, String user, String password)
    {
        this(test, user, password, null);
    }
    
    TestConfiguration getNewConfiguration(TestConfiguration old)
    {
        return new TestConfiguration(old, user, password, passwordToken);
    }
}
