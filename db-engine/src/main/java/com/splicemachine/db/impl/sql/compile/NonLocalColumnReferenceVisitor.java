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

package com.splicemachine.db.impl.sql.compile;

import com.splicemachine.db.iapi.error.StandardException;
import com.splicemachine.db.iapi.sql.compile.Visitable;
import com.splicemachine.db.iapi.sql.compile.Visitor;
import com.splicemachine.db.iapi.sql.dictionary.ColumnDescriptor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * This Visitor finds all tables in a given tree and all columns that don't reference
 * tables found in the tree, i.e. the column may reference a table in a different tree,
 * such as the other side of the join.
 *
 */
public class NonLocalColumnReferenceVisitor implements Visitor {

    private Set tableUUIDs;
    private List colRefs;

    public NonLocalColumnReferenceVisitor(){
        tableUUIDs = new HashSet();
        colRefs = new LinkedList();
    }


    public Visitable visit(Visitable node, QueryTreeNode parent) throws StandardException {

        if(node instanceof FromBaseTable){

            FromBaseTable tableNode = (FromBaseTable) node;
            tableUUIDs.add(tableNode.getTableDescriptor().getUUID());

        }else if(node instanceof ColumnReference){

            ColumnReference cr = (ColumnReference) node;

            if(cr.getSource() != null){
                colRefs.add(cr);
            }
        }

        return node;
    }


    public boolean visitChildrenFirst(Visitable node) {
        return false;
    }


    public boolean stopTraversal() {
        return false;
    }


    public boolean skipChildren(Visitable node) throws StandardException {
        return false;
    }

    /**
     * The visitor collects tables found in a branch of the tree, this method
     * return true if the ResultColumn's source table has been found by the Visitor.
     * It will return false if the source table has not been found (i.e. it exists in another
     * area of the plan, such as the opposite side of the join.
     *
     * @param rc
     * @return
     */
    private boolean isTableLocal(ResultColumn rc){

        boolean result = true;

        ColumnDescriptor colDescriptor = rc.getTableColumnDescriptor();

        if(colDescriptor != null){
            result = tableUUIDs.contains(rc.getTableColumnDescriptor().getReferencingUUID());
        }

        return result;
    }

    /**
     * Return a list of ColumnReference objects that point to a table not
     * found in this tree.
     *
     * @return list of ColumnReference objects
     */
    public List getNonLocalColumnRefs(){

        Iterator it = colRefs.iterator();

        while(it.hasNext()){
            ColumnReference cr = (ColumnReference) it.next();

            ResultColumn rc = cr.getSource();

            if(isTableLocal(rc)){
                it.remove();
            }

        }

        return colRefs;
    }
}
