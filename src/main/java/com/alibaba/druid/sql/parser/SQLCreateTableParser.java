package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;

public class SQLCreateTableParser extends SQLDDLParser {
    public SQLCreateTableParser(String sql) {
        super(sql);
    }

    public SQLCreateTableParser(Lexer lexer) {
        super(lexer);
    }

    public SQLCreateTableStatement parseCrateTable() {
        return parseCrateTable(true);
    }

    public SQLCreateTableStatement parseCrateTable(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }

        SQLCreateTableStatement createTable = new SQLCreateTableStatement();

        if (lexer.token() == Token.IDENTIFIER && lexer.stringVal().equalsIgnoreCase("GLOBAL")) {
            lexer.nextToken();

            if (lexer.token() == Token.IDENTIFIER && lexer.stringVal().equalsIgnoreCase("TEMPORAY")) {
                lexer.nextToken();
                createTable.setType(SQLCreateTableStatement.Type.GLOBAL_TEMPORARY);
            } else {
                throw new ParserException("syntax error");
            }
        } else if (lexer.token() == Token.IDENTIFIER && lexer.stringVal().equalsIgnoreCase("LOCAL")) {
            lexer.nextToken();
            if (lexer.token() == Token.IDENTIFIER && lexer.stringVal().equalsIgnoreCase("TEMPORAY")) {
                lexer.nextToken();
                createTable.setType(SQLCreateTableStatement.Type.LOCAL_TEMPORARY);
            } else {
                throw new ParserException("syntax error");
            }
        }

        accept(Token.TABLE);

        createTable.setName(this.exprParser.name());

        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();

            while (lexer.token() == Token.IDENTIFIER) {
                SQLColumnDefinition column = parseColumn();
                createTable.getTableElementList().add(column);

                // parseConstaint(column.getConstaints());

                if (lexer.token() != Token.COMMA) {
                    break;
                }

                lexer.nextToken();
            }

            // while
            // (this.tokenList.current().equals(OracleToken.ConstraintToken)) {
            // parseConstaint(table.getConstaints());
            //
            // if (this.tokenList.current().equals(OracleToken.CommaToken))
            // ;
            // lexer.nextToken();
            // }

            accept(Token.RPAREN);

        }

        if (lexer.token() == Token.ON) {
            throw new ParserException("TODO");
        }

        return createTable;
    }
}