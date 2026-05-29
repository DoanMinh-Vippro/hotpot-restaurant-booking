package com.example.hotpotrestaurantbooking_backend.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class DatabaseNamingStrategy extends PhysicalNamingStrategyStandardImpl implements PhysicalNamingStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (name == null) {
            return null;
        }
        String text = stripQuotes(name.getText());
        return Identifier.toIdentifier(toSnakeCase(text), name.isQuoted());
    }

    private Identifier apply(Identifier name) {
        if (name == null) {
            return null;
        }
        String text = stripQuotes(name.getText());
        return Identifier.toIdentifier(text, name.isQuoted());
    }

    private String stripQuotes(String text) {
        if (text == null) {
            return null;
        }
        if (text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private String toSnakeCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder(text.length() + 5);
        char previousChar = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && previousChar != '_' && !Character.isUpperCase(previousChar)) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
            previousChar = c;
        }
        return result.toString();
    }
}
