package mx.infotec.dads.kukulkan.engine.translator.dsl;

import static mx.infotec.dads.kukulkan.engine.language.JavaPropertyUtil.createJavaProperty;
import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarFieldTypeMap.fieldTypeFrom;
import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarMapping.resolveAssociationType;
import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarUtil.addContentType;
import static mx.infotec.dads.kukulkan.engine.util.CoreEntitesUtil.CORE_USER;
import static mx.infotec.dads.kukulkan.engine.util.CoreEntitesUtil.ENTITY_USER;
import static mx.infotec.dads.kukulkan.engine.util.CoreEntitesUtil.determineUserCorePhysicalName;
import static mx.infotec.dads.kukulkan.engine.util.DataBaseMapping.createDefaultPrimaryKey;
import static mx.infotec.dads.kukulkan.engine.util.DataBaseMapping.createIdJavaProperty;
import static mx.infotec.dads.kukulkan.metamodel.util.NameConventionFormatter.toDataBaseNameConvention;
import static mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser.parseToHyphens;
import static mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser.parseToLowerCaseFirstChar;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.util.StringUtils;

import mx.infotec.dads.kukulkan.dsl.kukulkan.AssociationField;
import mx.infotec.dads.kukulkan.dsl.kukulkan.AuditableSection;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BlobFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BlobValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BooleanFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.CoreEntityAssociationField;
import mx.infotec.dads.kukulkan.dsl.kukulkan.DateFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.NumericFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.NumericValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.PrimitiveField;
import mx.infotec.dads.kukulkan.dsl.kukulkan.StringFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.StringValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.ViewDeclaration;
import mx.infotec.dads.kukulkan.dsl.kukulkan.util.KukulkanSwitch;
import mx.infotec.dads.kukulkan.engine.language.JavaProperty;
import mx.infotec.dads.kukulkan.engine.model.EntityHolder;
import mx.infotec.dads.kukulkan.engine.model.PhysicalNameConvention;
import mx.infotec.dads.kukulkan.engine.service.InflectorService;
import mx.infotec.dads.kukulkan.engine.util.DataBaseMapping;
import mx.infotec.dads.kukulkan.metamodel.foundation.Constraint;
import mx.infotec.dads.kukulkan.metamodel.foundation.DatabaseType;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;
import mx.infotec.dads.kukulkan.metamodel.foundation.EntityAssociation;
import mx.infotec.dads.kukulkan.metamodel.foundation.GrammarFieldType;
import mx.infotec.dads.kukulkan.metamodel.foundation.ProjectConfiguration;
import mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser;

public class GrammarSemanticAnalyzer extends KukulkanSwitch<VisitorContext> {

    private static final String JAVA_UTIL_HASH_SET = "java.util.HashSet";
    private static final String JSON_IGNORE = "com.fasterxml.jackson.annotation.JsonIgnore";
    private static final String JAVA_UTIL_COLLECTION = "java.util.Set";
    private static final String SHEET_VIEW = "Sheet";

    /** The vctx. */
    private final VisitorContext vctx = new VisitorContext(new ArrayList<>());

    private EntityHolder entityHolder = new EntityHolder();

    /** The entity. */
    private Entity sourceEntity = null;

    private Entity targetEntity = null;

    private EntityAssociation entityAssociation = null;

    /** The property name. */
    private String propertyName = null;

    /** The java property. */
    private JavaProperty javaProperty = null;

    /** The constraint. */
    private Constraint constraint = null;

    private ProjectConfiguration pConf = null;

    private InflectorService inflectorService;

    private PhysicalNameConvention nameConvention;

    private boolean isDisplayField = false;

    public GrammarSemanticAnalyzer(ProjectConfiguration pConf, InflectorService inflectorService,
            PhysicalNameConvention physicalNameConvention) {
        this.pConf = pConf;
        this.inflectorService = inflectorService;
        this.nameConvention = physicalNameConvention;
    }

    @Override
    public VisitorContext caseAssociationField(AssociationField associationField) {
        targetEntity = entityHolder.getEntity(associationField.getTargetEntity().getName());
        entityAssociation = new EntityAssociation(sourceEntity, targetEntity);
        entityAssociation.setToTargetPropertyName(associationField.getId());
        if (!StringUtils.isEmpty(associationField.getToSourcePropertyName())) {
            entityAssociation.setBidirectional(true);
            entityAssociation.setToSourcePropertyName(associationField.getToSourcePropertyName());
        } else {
            entityAssociation.setBidirectional(false);
            entityAssociation
                    .setToSourcePropertyName(parseToLowerCaseFirstChar(entityAssociation.getSource().getName()));
        }
        genericVisitCardinality(associationField.getType());
        return super.caseAssociationField(associationField);
    }

    @Override
    public VisitorContext caseBlobFieldType(BlobFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(fieldTypeFrom(object.getName()));
        processFieldType(optional);
        return super.caseBlobFieldType(object);
    }

    @Override
    public VisitorContext caseBooleanFieldType(BooleanFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(fieldTypeFrom(object.getName()));
        processFieldType(optional);
        return super.caseBooleanFieldType(object);
    }

    @Override
    public VisitorContext caseCoreEntityAssociationField(CoreEntityAssociationField coreEntityAssociationField) {
        String associableEntity = coreEntityAssociationField.getTargetEntity();
        if (CORE_USER.equals(associableEntity)) {
            targetEntity = Entity.createDomainModelElement();
            addMetaData(ENTITY_USER, determineUserCorePhysicalName(pConf), targetEntity,
                    pConf.getTargetDatabase().getDatabaseType());
            entityAssociation = new EntityAssociation(sourceEntity, targetEntity);
            entityAssociation.setToTargetPropertyName(coreEntityAssociationField.getId());
        }
        genericVisitCardinality(coreEntityAssociationField.getType());
        return super.caseCoreEntityAssociationField(coreEntityAssociationField);
    }

    @Override
    public VisitorContext caseDateFieldType(DateFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(fieldTypeFrom(object.getType()));
        processFieldType(optional);
        return super.caseDateFieldType(object);
    }

    @Override
    public VisitorContext caseEntity(mx.infotec.dads.kukulkan.dsl.kukulkan.Entity object) {
        String entityName = object.getName();
        sourceEntity = entityHolder.getEntity(object.getName());
        String tableName = !StringUtils.isEmpty(object.getTableName()) ? object.getTableName() : null;
        addMetaData(entityName, tableName, sourceEntity, pConf.getTargetDatabase().getDatabaseType());
        getVctx().getElements().add(sourceEntity);
        // Re-process associations
        sourceEntity.getNotOwnerAssociations().stream().forEach(association -> {
            association.setToSourceReferencePhysicalName(
                    nameConvention.getPhysicalReferenceNameStrategy().getPhysicalReferenceName(association, true));
            association.setToTargetReferencePhysicalName(
                    nameConvention.getPhysicalReferenceNameStrategy().getPhysicalReferenceName(association, false));
        });
        return super.caseEntity(object);
    }

    @Override
    public VisitorContext caseNumericFieldType(NumericFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(fieldTypeFrom(object.getName()));
        processFieldType(optional);
        return super.caseNumericFieldType(object);
    }

    @Override
    public VisitorContext casePrimitiveField(PrimitiveField primitiveField) {
        propertyName = primitiveField.getId();
        constraint = new Constraint();
        if (primitiveField.getDisplayFieldMarker() != null) {
            isDisplayField = true;
        }
        return vctx;
    }

    @Override
    public VisitorContext caseStringFieldType(StringFieldType stringFieldType) {
        Optional<GrammarFieldType> optional = Optional.of(fieldTypeFrom(stringFieldType.getName()));
        processFieldType(optional);
        return super.caseStringFieldType(stringFieldType);
    }

    @Override
    public VisitorContext caseStringValidators(StringValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getPattern() != null) {
            constraint.setPattern(
                    object.getPattern().getPattern().substring(1, object.getPattern().getPattern().length() - 1));
            sourceEntity.setHasConstraints(true);
            javaProperty.setHasConstraints(true);
            javaProperty.setHasConstraints(true);
        }
        if (object.getMinLenght() != null) {
            handleMinLength(object.getMinLenght().getValue());
        }
        if (object.getMaxLenght() != null) {
            handleMaxLenght(object.getMaxLenght().getValue());
        }
        return super.caseStringValidators(object);
    }

    @Override
    public VisitorContext caseBlobValidators(BlobValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getMinBytesValue() != null) {
            handleMinLength(object.getMinBytesValue().getValue());
        }
        if (object.getMaxBytesValue() != null) {
            handleMaxLenght(object.getMaxBytesValue().getValue());
        }
        return super.caseBlobValidators(object);
    }

    @Override
    public VisitorContext caseNumericValidators(NumericValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getMinValue() != null) {
            handleMinLength(object.getMinValue().getValue());
        }
        if (object.getMaxValue() != null) {
            handleMaxLenght(object.getMaxValue().getValue());
        }
        return super.caseNumericValidators(object);
    }

    @Override
    public VisitorContext caseViewDeclaration(ViewDeclaration object) {
        if (SHEET_VIEW.equals(object.getViewType())) {
            entityHolder.getEntity(object.getEntity().getName()).getFeatures().setSheetable(true);
        }
        return super.caseViewDeclaration(object);
    }

    @Override
    public VisitorContext caseAuditableSection(AuditableSection object) {
        for (mx.infotec.dads.kukulkan.dsl.kukulkan.Entity entity : object.getAuditableEntities()) {
            entityHolder.getEntity(entity.getName()).getFeatures().setAuditable(true);
        }
        return super.caseAuditableSection(object);
    }

    private void handleRequiredField() {
        constraint.setNullable(false);
        sourceEntity.setHasNotNullElements(true);
        sourceEntity.setHasConstraints(true);
        javaProperty.setHasConstraints(true);
    }

    private void handleMinLength(int minLength) {
        constraint.setMin(Integer.toString(minLength));
        sourceEntity.setHasConstraints(true);
        javaProperty.setHasConstraints(true);
        javaProperty.setSizeValidation(true);
    }

    private void handleMaxLenght(int maxLength) {
        constraint.setMax(Integer.toString(maxLength));
        sourceEntity.setHasConstraints(true);
        javaProperty.setHasConstraints(true);
        javaProperty.setSizeValidation(true);
    }

    /**
     * Gets the vctx.
     *
     * @return the vctx
     */
    public VisitorContext getVctx() {
        return vctx;
    }

    private void genericVisitCardinality(String type) {
        entityAssociation.setType(resolveAssociationType(sourceEntity, type));
        entityAssociation
                .setToTargetPropertyNamePlural(inflectorService.pluralize(entityAssociation.getToTargetPropertyName()));
        if (entityAssociation.isBidirectional()) {
            entityAssociation.setToSourcePropertyNamePlural(
                    inflectorService.pluralize(entityAssociation.getToSourcePropertyName()));
        }
        entityAssociation.setToTargetPropertyNameUnderscore(
                SchemaPropertiesParser.parseToUnderscore(entityAssociation.getToTargetPropertyName()));
        entityAssociation.setToSourcePropertyNameUnderscore(
                SchemaPropertiesParser.parseToUnderscore(entityAssociation.getToSourcePropertyName()));

        entityAssociation.setToTargetPropertyNameUnderscorePlural(SchemaPropertiesParser
                .parseToUnderscore(inflectorService.pluralize(entityAssociation.getToTargetPropertyName())));
        entityAssociation.setToSourcePropertyNameUnderscorePlural(SchemaPropertiesParser
                .parseToUnderscore(inflectorService.pluralize(entityAssociation.getToSourcePropertyName())));

        assignAssociation(sourceEntity, targetEntity, entityAssociation);
        // For references
        // entityAssociation.setToSourceReferencePhysicalName(nameConvention.getPhysicalReferenceNameStrategy()
        // .getPhysicalReferenceName(entityAssociation.getToSourcePropertyNameUnderscorePlural()));
        // entityAssociation.setToTargetReferencePhysicalName(nameConvention.getPhysicalReferenceNameStrategy()
        // .getPhysicalReferenceName(entityAssociation.getToTargetPropertyNameUnderscorePlural()));
        entityAssociation.setToSourceReferencePhysicalName(
                nameConvention.getPhysicalReferenceNameStrategy().getPhysicalReferenceName(entityAssociation, true));
        entityAssociation.setToTargetReferencePhysicalName(
                nameConvention.getPhysicalReferenceNameStrategy().getPhysicalReferenceName(entityAssociation, false));
        resolveImports(sourceEntity, targetEntity, entityAssociation);
    }

    /**
     * Process field type.
     *
     * @param optional
     *            the optional
     */
    public void processFieldType(Optional<GrammarFieldType> optional) {
        if (optional.isPresent()) {
            GrammarFieldType grammarPropertyType = optional.get();
            javaProperty = createJavaProperty(propertyName, grammarPropertyType,
                    pConf.getTargetDatabase().getDatabaseType());

            javaProperty.setConstraint(constraint);
            setPropertyToShow();

            sourceEntity.addProperty(javaProperty);
            addContentType(sourceEntity, propertyName, pConf.getTargetDatabase().getDatabaseType(),
                    grammarPropertyType);
            GrammarMapping.addImports(sourceEntity.getImports(), javaProperty);
            DataBaseMapping.fillModelMetaData(sourceEntity, javaProperty);
        }
    }

    public void addMetaData(String entityName, String physicalName, Entity entity, DatabaseType dbType) {
        String singularName = inflectorService.singularize(entityName);
        if (singularName == null) {
            singularName = entityName;
        }
        if (StringUtils.isEmpty(physicalName)) {
            entity.setTableName(toDataBaseNameConvention(dbType, inflectorService.pluralize(entityName)));
        } else {
            entity.setTableName(physicalName);
        }
        entity.setUnderscoreName(SchemaPropertiesParser.parsePascalCaseToUnderscore(entity.getName()));
        entity.setName(entityName);
        entity.setCamelCaseFormat(SchemaPropertiesParser.parseToPropertyName(singularName));
        entity.setCamelCasePluralFormat(inflectorService.pluralize(entity.getCamelCaseFormat()));
        entity.setHyphensFormat(parseToHyphens(entity.getCamelCaseFormat()));
        entity.setHyphensPluralFormat(parseToHyphens(entity.getCamelCasePluralFormat()));
        entity.setPrimaryKey(createDefaultPrimaryKey(dbType, "id",
                nameConvention.getPrimaryKeyNameStrategy().getPrimaryKeyPhysicalName(entity)));
        entity.setReferencePhysicalName(entity.getPrimaryKey().getPhysicalName().getSnakeCase());
        entity.setDisplayField(createIdJavaProperty("id"));

    }

    private void assignAssociation(Entity sourceEntity, Entity targetEntity, EntityAssociation entityAssociation) {
        sourceEntity.addAssociation(entityAssociation);
        // if association is a cycle, then sourceEntity already have the
        // association so it is not necessary added to it
        if (!entityAssociation.isCycle()) {
            targetEntity.addAssociation(entityAssociation);
        }
    }

    private void setPropertyToShow() {
        if (isDisplayField) {
            sourceEntity.setDisplayField(javaProperty);
            isDisplayField = false;
        }
    }

    private void resolveImports(Entity sourceEntity, Entity targetEntity, EntityAssociation entityAssociation) {
        switch (entityAssociation.getType()) {
        case ONE_TO_ONE:
            if (entityAssociation.isBidirectional()) {
                targetEntity.getImports().add(JSON_IGNORE);
            }
            break;
        case ONE_TO_MANY:
            sourceEntity.getImports().add(JAVA_UTIL_COLLECTION);
            sourceEntity.getImports().add(JAVA_UTIL_HASH_SET);
            sourceEntity.getImports().add(JSON_IGNORE);
            break;
        case MANY_TO_ONE:
            if (entityAssociation.isBidirectional()) {
                targetEntity.getImports().add(JAVA_UTIL_COLLECTION);
                targetEntity.getImports().add(JAVA_UTIL_HASH_SET);
            }
            break;
        case MANY_TO_MANY:
            sourceEntity.getImports().add(JAVA_UTIL_COLLECTION);
            sourceEntity.getImports().add(JAVA_UTIL_HASH_SET);
            if (entityAssociation.isBidirectional()) {
                targetEntity.getImports().add(JAVA_UTIL_COLLECTION);
                targetEntity.getImports().add(JAVA_UTIL_HASH_SET);
                targetEntity.getImports().add(JSON_IGNORE);
            }
            break;
        default:
            break;
        }
    }

}
