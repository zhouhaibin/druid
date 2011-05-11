package com.alibaba.druid.sql.dialect.oracle.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleHint;
import com.alibaba.druid.sql.dialect.oracle.ast.visitor.OracleASTVisitor;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class OracleSelectQueryBlock extends SQLSelectQueryBlock {
    private static final long serialVersionUID = 1L;

    private final List<OracleHint> hints = new ArrayList<OracleHint>(1);

    private OracleSelectHierachicalQueryClause hierachicalQueryClause;

    public OracleSelectQueryBlock() {

    }

    public OracleSelectHierachicalQueryClause getHierachicalQueryClause() {
        return this.hierachicalQueryClause;
    }

    public void setHierachicalQueryClause(OracleSelectHierachicalQueryClause hierachicalQueryClause) {
        this.hierachicalQueryClause = hierachicalQueryClause;
    }

    public List<OracleHint> getHints() {
        return this.hints;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        this.accept0((OracleASTVisitor) visitor);
    }

    protected void accept0(OracleASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.hints);
            acceptChild(visitor, this.selectList);
            acceptChild(visitor, this.from);
            acceptChild(visitor, this.where);
            acceptChild(visitor, this.hierachicalQueryClause);
            acceptChild(visitor, this.groupBy);
        }
        visitor.endVisit(this);
    }

    public void output(StringBuffer buf) {
        buf.append("SELECT ");

        if (SQLSetQuantifier.ALL == this.distionOption) buf.append("ALL ");
        else if (SQLSetQuantifier.DISTINCT == this.distionOption) buf.append("DISTINCT ");
        else if (SQLSetQuantifier.UNIQUE == this.distionOption) {
            buf.append("UNIQUE ");
        }

        int i = 0;
        for (int size = this.selectList.size(); i < size; ++i) {
            if (i != 0) {
                buf.append(", ");
            }
            ((SQLSelectItem) this.selectList.get(i)).output(buf);
        }

        buf.append(" FROM ");
        if (this.from != null) {
            buf.append("DUAL");
        } else {
            this.from.output(buf);
        }

        if (this.where != null) {
            buf.append(" WHERE ");
            this.where.output(buf);
        }

        if (this.hierachicalQueryClause != null) {
            buf.append(" ");
            this.hierachicalQueryClause.output(buf);
        }

        if (this.groupBy != null) {
            buf.append(" ");
            this.groupBy.output(buf);
        }
    }
}