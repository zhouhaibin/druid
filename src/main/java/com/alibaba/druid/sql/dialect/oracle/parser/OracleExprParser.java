package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.OracleOrderBy;
import com.alibaba.druid.sql.dialect.oracle.ast.expr.OracleAggregateExpr;
import com.alibaba.druid.sql.dialect.oracle.ast.expr.OracleAnalytic;
import com.alibaba.druid.sql.dialect.oracle.ast.expr.OracleAnalyticWindowing;
import com.alibaba.druid.sql.dialect.oracle.ast.expr.OracleIntervalType;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectOrderByItem;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;

public class OracleExprParser extends SQLExprParser {

    public boolean allowStringAdditive = false;

    private static final String[] _aggregateFunctions = { "AVG", "CORR", "COVAR_POP", "COVAR_SAMP", "COUNT",
            "CUME_DIST", "DENSE_RANK", "FIRST", "FIRST_VALUE", "LAG", "LAST", "LAST_VALUE", "LEAD", "MAX", "MIN",
            "NTILE", "PERCENT_RANK", "PERCENTILE_CONT", "PERCENTILE_DISC", "RANK", "RATIO_TO_REPORT", "REGR_SLOPE",
            "REGR_INTERCEPT", "REGR_COUNT", "REGR_R2", "REGR_AVGX", "REGR_AVGY", "REGR_SXX", "REGR_SYY", "REGR_SXY",
            "ROW_NUMBER", "STDDEV", "STDDEV_POP", "STDDEV_SAMP", "SUM", "VAR_POP", "VAR_SAMP", "VARIANCE" };

    public OracleExprParser(Lexer lexer) {
        super(lexer);
    }

    public OracleExprParser(String text) {
        super(text);
    }

    public boolean isAggreateFunction(String word) {
        for (int i = 0; i < _aggregateFunctions.length; ++i) {
            if (_aggregateFunctions[i].compareToIgnoreCase(word) == 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public OracleOrderBy parseOrderBy() {
        if (lexer.token() == (Token.ORDER)) {
            OracleOrderBy orderBy = new OracleOrderBy();

            lexer.nextToken();

            if (identifierEquals("SIBINGS")) {
                lexer.nextToken();
                orderBy.setSibings(true);
            }

            accept(Token.BY);

            orderBy.getItems().add(parseSelectOrderByItem());

            while (lexer.token() == (Token.COMMA)) {
                lexer.nextToken();
                orderBy.getItems().add(parseSelectOrderByItem());
            }

            return orderBy;
        }

        return null;
    }

    protected OracleAggregateExpr parseAggregateExpr(String method_name) {
        OracleAggregateExpr aggregateExpr;
        if (lexer.token() == (Token.ALL)) {
            aggregateExpr = new OracleAggregateExpr(method_name, 1);
            lexer.nextToken();
        } else if (lexer.token() == (Token.DISTINCT)) {
            aggregateExpr = new OracleAggregateExpr(method_name, 0);
            lexer.nextToken();
        } else {
            aggregateExpr = new OracleAggregateExpr(method_name, 1);
        }
        exprList(aggregateExpr.getArguments());

        if (lexer.stringVal().equalsIgnoreCase("IGNORE")) {
            lexer.nextToken();
            identifierEquals("NULLS");
            aggregateExpr.setIgnoreNulls(true);
        }

        accept(Token.RPAREN);

        if (identifierEquals("OVER")) {
            OracleAnalytic over = new OracleAnalytic();

            lexer.nextToken();
            accept(Token.LPAREN);

            if (identifierEquals("PARTITION")) {
                lexer.nextToken();
                accept(Token.BY);

                if (lexer.token() == (Token.LPAREN)) {
                    lexer.nextToken();
                    exprList(over.getPartitionBy());
                    accept(Token.RPAREN);
                } else {
                    over.getPartitionBy().add(expr());
                }
            }

            over.setOrderBy(parseOrderBy());
            if (over.getOrderBy() != null) {
                OracleAnalyticWindowing windowing = null;
                if (lexer.stringVal().equalsIgnoreCase("ROWS")) {
                    lexer.nextToken();
                    windowing = new OracleAnalyticWindowing();
                    windowing.setType(OracleAnalyticWindowing.Type.ROWS);
                } else if (lexer.stringVal().equalsIgnoreCase("RANGE")) {
                    lexer.nextToken();
                    windowing = new OracleAnalyticWindowing();
                    windowing.setType(OracleAnalyticWindowing.Type.RANGE);
                }

                if (windowing != null) {
                    if (lexer.stringVal().equalsIgnoreCase("CURRENT")) {
                        lexer.nextToken();
                        if (lexer.stringVal().equalsIgnoreCase("ROW")) {
                            lexer.nextToken();
                            windowing.setExpr(new SQLIdentifierExpr("CURRENT ROW"));
                            over.setWindowing(windowing);
                        }
                        throw new ParserException("syntax error");
                    }
                    if (lexer.stringVal().equalsIgnoreCase("UNBOUNDED")) {
                        lexer.nextToken();
                        if (lexer.stringVal().equalsIgnoreCase("PRECEDING")) {
                            lexer.nextToken();
                            windowing.setExpr(new SQLIdentifierExpr("UNBOUNDED PRECEDING"));
                            over.setWindowing(windowing);
                        }
                        throw new ParserException("syntax error");
                    }

                    throw new ParserException("TODO");
                }
            }

            accept(Token.RPAREN);

            aggregateExpr.setOver(over);
        }
        return aggregateExpr;
    }

    private OracleIntervalType parseIntervalType() {
        String currentTokenUpperValue = lexer.stringVal();
        lexer.nextToken();

        if (currentTokenUpperValue.equals("YEAR")) return OracleIntervalType.YEAR;
        if (currentTokenUpperValue.equals("MONTH")) return OracleIntervalType.MONTH;
        if (currentTokenUpperValue.equals("HOUR")) return OracleIntervalType.HOUR;
        if (currentTokenUpperValue.equals("MINUTE")) return OracleIntervalType.MINUTE;
        if (currentTokenUpperValue.equals("SECOND")) {
            return OracleIntervalType.SECOND;
        }
        throw new ParserException("syntax error");
    }

    @Override
    protected OracleSelectParser createSelectParser() {
        return new OracleSelectParser(this.lexer);
    }

    @Override
    protected OracleSelectOrderByItem parseSelectOrderByItem() {
        OracleSelectOrderByItem item = new OracleSelectOrderByItem();

        item.setExpr(expr());

        if (lexer.token() == (Token.ASC)) {
            lexer.nextToken();
            item.setType(SQLOrderingSpecification.ASC);
        } else if (lexer.token() == (Token.DESC)) {
            lexer.nextToken();
            item.setType(SQLOrderingSpecification.DESC);
        }

        if (identifierEquals("NULLS")) {
            if (lexer.stringVal().equalsIgnoreCase("FIRST")) {
                lexer.nextToken();
                item.setNullsOrderType(OracleSelectOrderByItem.NullsOrderType.NullsFirst);
            } else if (lexer.stringVal().equalsIgnoreCase("LAST")) {
                lexer.nextToken();
                item.setNullsOrderType(OracleSelectOrderByItem.NullsOrderType.NullsLast);
            } else {
                throw new ParserException("TODO");
            }
        }

        return item;
    }
}