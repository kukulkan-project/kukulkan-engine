/*
 *  
 * The MIT License (MIT)
 * Copyright (c) 2016 Daniel Cortes Pichardo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package mx.infotec.dads.kukulkan.engine.translator.dsl;

import static mx.infotec.dads.kukulkan.metamodel.foundation.SuperColumnType.BINARY_TYPE;
import static mx.infotec.dads.kukulkan.metamodel.foundation.SuperColumnType.BOOLEAN_TYPE;
import static mx.infotec.dads.kukulkan.metamodel.foundation.SuperColumnType.LITERAL_TYPE;
import static mx.infotec.dads.kukulkan.metamodel.foundation.SuperColumnType.NUMBER_TYPE;
import static mx.infotec.dads.kukulkan.metamodel.foundation.SuperColumnType.TIME_TYPE;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import mx.infotec.dads.kukulkan.engine.language.JavaProperty;
import mx.infotec.dads.kukulkan.metamodel.foundation.FieldType;
import mx.infotec.dads.kukulkan.metamodel.foundation.GrammarFieldType;
import mx.infotec.dads.kukulkan.metamodel.util.MetaModelException;

/**
 * GrammarPropertyMapping.
 *
 * @author Daniel Cortes Pichardo
 */
public class GrammarFieldTypeMap {

    /** The Constant map. */
    private static final Map<String, GrammarFieldType> GRAMMAR_MAP;
    static {
        GRAMMAR_MAP = new HashMap<>();
        /*
         * Literal
         */
        GRAMMAR_MAP.put(FieldType.STRING.text(), new GrammarFieldTypeImpl(FieldType.STRING, LITERAL_TYPE));
        GRAMMAR_MAP.put(FieldType.TEXT_BLOB.text(),
                new GrammarFieldTypeImpl(FieldType.TEXT_BLOB, LITERAL_TYPE, String.class, true));

        /*
         * Numbers
         */
        GRAMMAR_MAP.put(FieldType.INTEGER.text(),
                new GrammarFieldTypeImpl(FieldType.INTEGER, NUMBER_TYPE, Integer.class));
        GRAMMAR_MAP.put(FieldType.LONG.text(), new GrammarFieldTypeImpl(FieldType.LONG, NUMBER_TYPE, Long.class));
        GRAMMAR_MAP.put(FieldType.BIG_DECIMAL.text(),
                new GrammarFieldTypeImpl(FieldType.BIG_DECIMAL, NUMBER_TYPE, BigDecimal.class));
        GRAMMAR_MAP.put(FieldType.FLOAT.text(), new GrammarFieldTypeImpl(FieldType.FLOAT, NUMBER_TYPE, Float.class));
        GRAMMAR_MAP.put(FieldType.DOUBLE.text(), new GrammarFieldTypeImpl(FieldType.DOUBLE, NUMBER_TYPE, Double.class));

        /*
         * Time based
         */
        GRAMMAR_MAP.put(FieldType.LOCAL_DATE.text(),
                new GrammarFieldTypeImpl(FieldType.LOCAL_DATE, TIME_TYPE, LocalDate.class));
        GRAMMAR_MAP.put(FieldType.ZONED_DATETIME.text(),
                new GrammarFieldTypeImpl(FieldType.ZONED_DATETIME, TIME_TYPE, ZonedDateTime.class));
        GRAMMAR_MAP.put(FieldType.INSTANT.text(),
                new GrammarFieldTypeImpl(FieldType.INSTANT, TIME_TYPE, Instant.class));

        /*
         * Booleans
         */
        GRAMMAR_MAP.put(FieldType.BOOLEAN_TYPE.text(),
                new GrammarFieldTypeImpl(FieldType.BOOLEAN_TYPE, BOOLEAN_TYPE, boolean.class));

        /*
         * Blobs
         */
        GRAMMAR_MAP.put(FieldType.BLOB.text(),
                new GrammarFieldTypeImpl(FieldType.BLOB, BINARY_TYPE, byte[].class, true));
        GRAMMAR_MAP.put(FieldType.ANY_BLOB.text(),
                new GrammarFieldTypeImpl(FieldType.ANY_BLOB, BINARY_TYPE, byte[].class, true));
        GRAMMAR_MAP.put(FieldType.IMAGE_BLOB.text(),
                new GrammarFieldTypeImpl(FieldType.IMAGE_BLOB, BINARY_TYPE, byte[].class, true));

    }

    /**
     * Instantiates a new grammar property mapping.
     */
    private GrammarFieldTypeMap() {
    }

    /**
     * Gets the property type.
     *
     * @param property
     *            the property
     * @return the property type
     */
    public static GrammarFieldType getPropertyType(String property) {
        return GRAMMAR_MAP.get(property);
    }

    public static void configurateGrammarFieldType(GrammarFieldType propertyType, JavaProperty javaProperty) {
        switch (propertyType.getFieldType()) {
        case LOCAL_DATE:
            javaProperty.setLocalDate(true);
            break;
        case ZONED_DATETIME:
            javaProperty.setZoneDateTime(true);
            break;
        case INSTANT:
            javaProperty.setInstant(true);
            break;
        case BIG_DECIMAL:
            javaProperty.setBigDecimal(true);
            break;
        case LONG:
            javaProperty.setLong(true);
            break;
        case INTEGER:
            javaProperty.setInteger(true);
            break;
        case DOUBLE:
            javaProperty.setDouble(true);
            break;
        case FLOAT:
            javaProperty.setFloat(true);
            break;
        case ANY_BLOB:
            javaProperty.setBlob(true);
            javaProperty.setAnyBlob(true);
            break;
        case IMAGE_BLOB:
            javaProperty.setBlob(true);
            javaProperty.setImageBlob(true);
            break;
        case BLOB:
            javaProperty.setBlob(true);
            break;
        case TEXT_BLOB:
            javaProperty.setClob(true);
            javaProperty.setTextBlob(true);
            break;
        case STRING:
            javaProperty.setString(true);
            break;
        case BOOLEAN_TYPE:
            javaProperty.setBoolean(true);
            break;
        default:
            throw new MetaModelException("Property not found : " + propertyType.getFieldType());
        }
    }

    public static GrammarFieldType fieldTypeFrom(String from) {
        return GRAMMAR_MAP.get(from);
    }
}
