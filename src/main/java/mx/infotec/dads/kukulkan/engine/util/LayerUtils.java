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
package mx.infotec.dads.kukulkan.engine.util;

import static mx.infotec.dads.kukulkan.metamodel.util.JavaFileNameParser.formatToImportStatement;
import static mx.infotec.dads.kukulkan.metamodel.util.Validator.requiredNotEmpty;

import java.util.HashMap;
import java.util.Map;

import mx.infotec.dads.kukulkan.engine.service.FileUtil;
import mx.infotec.dads.kukulkan.metamodel.context.GeneratorContext;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModel;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;
import mx.infotec.dads.kukulkan.metamodel.foundation.ProjectConfiguration;
import mx.infotec.dads.kukulkan.metamodel.util.NameConventions;

/**
 * LayerUtils.
 *
 * @author Daniel Cortes Pichardo
 */
public class LayerUtils {

    /** The Constant URL_NAME_PROPERTY. */
    public static final String URL_NAME_PROPERTY = "urlName";

    /** The Constant IMPORT_REPOSITORY_PROPERTY. */
    public static final String IMPORT_REPOSITORY_PROPERTY = "importRepository";

    /** The Constant IMPORT_SERVICE_PROPERTY. */
    public static final String IMPORT_SERVICE_PROPERTY = "importService";

    /** Authoring Properties. */
    public static final String YEAR_PROPERTY = "year";

    /** The Constant AUTHOR_PROPERTY. */
    public static final String AUTHOR_PROPERTY = "author";

    /** The Constant IS_MONGO_PROPERTY. */
    public static final String DATABASE_TYPE = "databaseType";

    /** The Constant PROJECT_NAME_PROPERTY. */
    public static final String PROJECT_NAME_PROPERTY = "projectName";

    /** The Constant DATA_MODEL_GROUP_PROPERTY. */
    public static final String DATA_MODEL_GROUP_PROPERTY = "dataModelGroup";

    /** Common Properties per Element. */
    public static final String HAS_BLOB_PROPERTIES_PROPERTY = "hasBlobProperties";

    /** The Constant HAS_LOCAL_DATE_PROPERTY. */
    public static final String HAS_LOCAL_DATE_PROPERTY = "hasLocalDate";

    /** The Constant HAS_TIME_PROPERTIES_PROPERTY. */
    public static final String HAS_TIME_PROPERTIES_PROPERTY = "hasTimeProperties";

    public static final String HAS_ONE_TO_ONE = "hasOneToOne";

    public static final String HAS_ONE_TO_MANY = "hasOneToMany";

    public static final String HAS_MANY_TO_ONE = "hasManyToOne";

    public static final String HAS_MANY_TO_MANY = "hasManyToMany";

    /** The Constant HAS_BIG_DECIMAL_PROPERTY. */
    public static final String HAS_BIG_DECIMAL_PROPERTY = "hasBigDecimal";

    /** The Constant HAS_ZONE_DATE_TIME_PROPERTY. */
    public static final String HAS_ZONE_DATE_TIME_PROPERTY = "hasZoneDateTime";

    /** The Constant HAS_INSTANT_PROPERTY. */
    public static final String HAS_INSTANT_PROPERTY = "hasInstant";

    /** The Constant HAS_CONSTRAINTS_PROPERTY. */
    public static final String HAS_CONSTRAINTS_PROPERTY = "hasConstraints";

    /** The Constant HAS_NOT_NULL. */
    public static final String HAS_NOT_NULL = "hasNotNull";

    /** The Constant TABLE_NAME_PROPERTY. */
    public static final String TABLE_NAME_PROPERTY = "tableName";

    /** The Constant TABLE_NAME_PROPERTY. */
    public static final String TABLE_NAME_PROPERTY_LOWER_CASE = "tableNameLowerCase";

    /** The Constant PACKAGE_PROPERTY. */
    public static final String PACKAGE_PROPERTY = "package";

    /** The Constant PACKAGE_SIMPLE_FORMAT_PROPERTY. */
    public static final String PACKAGE_SIMPLE_FORMAT_PROPERTY = "packageSimpleFormat";

    /** The Constant PACKAGE_IMPL_PROPERTY. */
    public static final String PACKAGE_IMPL_PROPERTY = "packageImpl";

    /** The Constant IMPORTS_PROPERTY. */
    public static final String IMPORTS_PROPERTY = "imports";

    /** The Constant IMPORT_PRIMARY_KEY_PROPERTY. */
    public static final String IMPORT_PRIMARY_KEY_PROPERTY = "importPrimaryKey";

    /** The Constant IMPORT_MODEL_PROPERTY. */
    public static final String IMPORT_MODEL_PROPERTY = "importModel";

    /** The Constant ENTITY_PROPERTY. */
    public static final String ENTITY_PROPERTY = "entity";

    /** The Constant ENTITY_CAMEL_CASE_PROPERTY. */
    public static final String ENTITY_CAMEL_CASE_PROPERTY = "entityCamelCase";

    /** The Constant ENTITY_CAMEL_CASE_PLURAL_PROPERTY. */
    public static final String ENTITY_CAMEL_CASE_PLURAL_PROPERTY = "entityCamelCasePlural";

    /** The Constant ENTITY_HYPHEN_NOTATION_PLURAL_PROPERTY. */
    public static final String ENTITY_HYPHEN_NOTATION_PLURAL_PROPERTY = "entityHyphenNotationPlural";

    /** The Constant ENTITY_HYPHEN_NOTATION_PROPERTY. */
    public static final String ENTITY_HYPHEN_NOTATION_PROPERTY = "entityHyphenNotation";

    /** The Constant ID_PROPERTY. */
    public static final String ID_PROPERTY = "id";

    /** The Constant PRIMARY_KEY_PROPERTY. */
    public static final String PRIMARY_KEY_PROPERTY = "primaryKey";

    /** The Constant PROPERTIES_PROPERTY. */
    public static final String PROPERTIES_PROPERTY = "properties";

    /** The Constant PROJECT_CONFIGURATION_PROPERTY. */
    public static final String PROJECT_CONFIGURATION_PROPERTY = "project";

    public static final String TIME_STAMP = "timestamp";

    public static final String OWNER_ASSOCIATIONS = "ownerAssociations";

    public static final String NOT_OWNER_ASSOCIATIONS = "notOwnerAssociations";

    /**
     * Instantiates a new layer utils.
     */
    private LayerUtils() {

    }

    /**
     * createGeneralDescription for the template engine. It adds common
     * properties needed to identify meta info.
     *
     * @param context
     *            the context
     * @return Map<String, Object>
     */
    public static Map<String, Object> addAuthoringData(GeneratorContext context) {
        ProjectConfiguration pConf = requiredNotEmpty(context.get(ProjectConfiguration.class));
        DomainModel domainModel = requiredNotEmpty(context.get(DomainModel.class));
        Map<String, Object> model = new HashMap<>();
        model.put(YEAR_PROPERTY, pConf.getYear());
        model.put(AUTHOR_PROPERTY, pConf.getAuthor());
        model.put(DATABASE_TYPE, pConf.getTargetDatabase().getDatabaseType());
        model.put(PROJECT_NAME_PROPERTY, pConf.getId());
        model.put(DATA_MODEL_GROUP_PROPERTY, domainModel.getDomainModelGroup());
        model.put(TIME_STAMP, FileUtil.formatToDateTimeJournal(pConf.getTimestamp()));
        return model;
    }

    /**
     * createGeneralDescription for the template engine. It adds common
     * properties needed to identify meta info.
     *
     * @param context
     *            the context
     * @return Map<String, Object>
     */
    public static Map<String, Object> addProjectData(GeneratorContext context) {
        Map<String, Object> model = new HashMap<>();
        model.put(PROJECT_CONFIGURATION_PROPERTY, requiredNotEmpty(context.get(ProjectConfiguration.class)));
        return model;
    }

    /**
     * addCommonDataModelElements, add needed model properties for the template
     * engine.
     *
     * @param conf
     *            the conf
     * @param map
     *            the map
     * @param bPackage
     *            the b package
     * @param dme
     *            the dme
     */
    public static void addCommonDataModelElements(ProjectConfiguration conf, Map<String, Object> map, String bPackage,
            Entity dme) {
        map.put(HAS_TIME_PROPERTIES_PROPERTY, dme.isHasTimeProperties());
        map.put(HAS_LOCAL_DATE_PROPERTY, dme.isHasLocalDate());
        map.put(HAS_BLOB_PROPERTIES_PROPERTY, dme.isHasBlobProperties());
        map.put(HAS_CONSTRAINTS_PROPERTY, dme.isHasConstraints());
        map.put(HAS_INSTANT_PROPERTY, dme.isHasInstant());
        map.put(HAS_ZONE_DATE_TIME_PROPERTY, dme.isHasZoneDateTime());
        map.put(HAS_BIG_DECIMAL_PROPERTY, dme.isHasBigDecimal());
        map.put(HAS_NOT_NULL, dme.isHasNotNullElements());
        map.put(URL_NAME_PROPERTY, dme.getCamelCaseFormat());
        map.put(TABLE_NAME_PROPERTY, dme.getTableName());
        map.put(TABLE_NAME_PROPERTY_LOWER_CASE, dme.getTableName().toLowerCase());
        map.put(PACKAGE_PROPERTY, null);// must be provided in individual files
        map.put(IMPORTS_PROPERTY, dme.getImports());
        map.put(IMPORT_SERVICE_PROPERTY, formatToImportStatement(bPackage, NameConventions.SERVICE_LAYER_NAME,
                dme.getName() + NameConventions.SERVICE));
        map.put(IMPORT_REPOSITORY_PROPERTY,
                formatToImportStatement(bPackage, NameConventions.DAO_LAYER_NAME, dme.getName() + NameConventions.DAO));
        map.put(IMPORT_MODEL_PROPERTY,
                formatToImportStatement(bPackage, NameConventions.DOMAIN_LAYER_NAME, dme.getName()));
        importPrimaryKeyIfComposed(conf, map, bPackage, dme);
        map.put(ENTITY_CAMEL_CASE_PROPERTY, dme.getCamelCaseFormat());
        map.put(ENTITY_PROPERTY, dme);
        map.put(ENTITY_HYPHEN_NOTATION_PROPERTY, dme.getHyphensFormat());
        map.put(ENTITY_HYPHEN_NOTATION_PLURAL_PROPERTY, dme.getHyphensPluralFormat());
        map.put(ENTITY_CAMEL_CASE_PLURAL_PROPERTY, dme.getCamelCasePluralFormat());
        map.put(ID_PROPERTY, dme.getPrimaryKey().getType());
        dme.getPrimaryKey().setGenerationType(conf.getTargetDatabase().getGlobalGenerationType());
        map.put(PRIMARY_KEY_PROPERTY, dme.getPrimaryKey());
        map.put(PROPERTIES_PROPERTY, dme.getProperties());
        map.put(OWNER_ASSOCIATIONS, dme.getOwnerAssociations());
        map.put(NOT_OWNER_ASSOCIATIONS, dme.getNotOwnerAssociations());
        map.put(HAS_ONE_TO_ONE, dme.isHasOneToOne());
        map.put(HAS_ONE_TO_MANY, dme.isHasOneToMany());
        map.put(HAS_MANY_TO_ONE, dme.isHasManyToOne());
        map.put(HAS_MANY_TO_MANY, dme.isHasManyToMany());
    }

    /**
     * Import primary key if composed.
     *
     * @param conf
     *            the conf
     * @param model
     *            the model
     * @param bPackage
     *            the b package
     * @param dme
     *            the dme
     */
    public static void importPrimaryKeyIfComposed(ProjectConfiguration conf, Map<String, Object> model, String bPackage,
            Entity dme) {
        if (dme.getPrimaryKey().isComposed()) {
            model.put(IMPORT_PRIMARY_KEY_PROPERTY,
                    formatToImportStatement(bPackage, NameConventions.DAO_LAYER_NAME, dme.getPrimaryKey().getType()));
        }
    }
}
