/*
 * Copyright 2011 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.ast.expr;

import java.util.BitSet;

import com.alibaba.druid.sql.visitor.SQLASTVisitor;

/**
 * SQL-92
 * <p>
 * &ltbit string literal> ::= B &ltquote> [ &ltbit> ... ] &ltquote> [ {
 * &ltseparator> ... &ltquote> [ &ltbit> ... ] &ltquote> }... ]
 * </p>
 * 
 * @author WENSHAO
 */
public class SQLBitStringLiteralExpr extends SQLLiteralExpr {
    private static final long serialVersionUID = 1L;

    private BitSet value;

    public SQLBitStringLiteralExpr() {

    }

    public BitSet getValue() {
        return value;
    }

    public void setValue(BitSet value) {
        this.value = value;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        visitor.visit(this);

        visitor.endVisit(this);
    }

    public void output(StringBuffer buf) {
        buf.append("b'");
        for (int i = 0; i < value.length(); ++i) {
            buf.append(value);
        }
        buf.append("'");
    }
}