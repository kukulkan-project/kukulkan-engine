package mx.infotec.dads.kukulkan.engine.translator.dsl;

import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarMapping.resolveAssociationType;
import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarUtil.addContentType;
import static mx.infotec.dads.kukulkan.engine.translator.dsl.GrammarUtil.createJavaProperty;
import static mx.infotec.dads.kukulkan.engine.util.DataBaseMapping.createDefaultPrimaryKey;
import static mx.infotec.dads.kukulkan.metamodel.util.NameConventionFormatter.toDataBaseNameConvention;
import static mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser.parseToHyphens;
import static mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser.parseToLowerCaseFirstChar;

import java.util.ArrayList;
import java.util.Optional;

import mx.infotec.dads.kukulkan.dsl.kukulkan.AssociationField;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BlobFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BlobValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.BooleanFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.DateFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.NumericFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.NumericValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.PrimitiveField;
import mx.infotec.dads.kukulkan.dsl.kukulkan.StringFieldType;
import mx.infotec.dads.kukulkan.dsl.kukulkan.StringValidators;
import mx.infotec.dads.kukulkan.dsl.kukulkan.util.KukulkanSwitch;
import mx.infotec.dads.kukulkan.engine.language.JavaProperty;
import mx.infotec.dads.kukulkan.engine.service.InflectorService;
import mx.infotec.dads.kukulkan.engine.util.DataBaseMapping;
import mx.infotec.dads.kukulkan.metamodel.foundation.AssociationType;
import mx.infotec.dads.kukulkan.metamodel.foundation.Constraint;
import mx.infotec.dads.kukulkan.metamodel.foundation.DatabaseType;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;
import mx.infotec.dads.kukulkan.metamodel.foundation.EntityAssociation;
import mx.infotec.dads.kukulkan.metamodel.foundation.ProjectConfiguration;
import mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser;

public class XtextSemanticAnalyzer extends KukulkanSwitch<VisitorContext> {

    private static final String JAVA_UTIL_HASH_SET = "java.util.HashSet";
    private static final String JSON_IGNORE = "com.fasterxml.jackson.annotation.JsonIgnore";
    private static final String JAVA_UTIL_COLLECTION = "java.util.Set";

    /** The vctx. */
    private final VisitorContext vctx = new VisitorContext(new ArrayList<>());

    private EntityHolder entityHolder = new EntityHolder();

    /** The entity. */
    private Entity sourceEntity = null;

    private Entity targetEntity = null;

    private EntityAssociation entityAssociation = null;

    private PrimitiveField pfc = null;

    /** The property name. */
    private String propertyName = null;

    /** The java property. */
    private JavaProperty javaProperty = null;

    /** The constraint. */
    private Constraint constraint = null;

    private ProjectConfiguration pConf = null;

    private InflectorService inflectorService;

    public XtextSemanticAnalyzer(ProjectConfiguration pConf, InflectorService inflectorService) {
        this.pConf = pConf;
        this.inflectorService = inflectorService;
    }

    @Override
    public VisitorContext caseAssociationField(AssociationField object) {
        targetEntity = entityHolder.getEntity(object.getTargetEntity().getName(),
                pConf.getDatabase().getDatabaseType());
        entityAssociation = new EntityAssociation(sourceEntity, targetEntity);
        entityAssociation.setToTargetPropertyName(object.getId());
        if (object.getToSourcePropertyName() != null) {
            entityAssociation.setToSourcePropertyName(object.getToSourcePropertyName());
        }
        visitCardinality(object.getType());
        return super.caseAssociationField(object);
    }

    @Override
    public VisitorContext caseBlobFieldType(BlobFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(GrammarFieldTypeMapping.getMap().get(object.getName()));
        processFieldType(optional);
        return super.caseBlobFieldType(object);
    }

    @Override
    public VisitorContext caseBooleanFieldType(BooleanFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(GrammarFieldTypeMapping.getMap().get(object.getName()));
        processFieldType(optional);
        return super.caseBooleanFieldType(object);
    }

    @Override
    public VisitorContext caseDateFieldType(DateFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(GrammarFieldTypeMapping.getMap().get(object.getType()));
        processFieldType(optional);
        return super.caseDateFieldType(object);
    }

    @Override
    public VisitorContext caseEntity(mx.infotec.dads.kukulkan.dsl.kukulkan.Entity object) {
        sourceEntity = entityHolder.getEntity(object.getName(), pConf.getDatabase().getDatabaseType());
        addMetaData(object, sourceEntity, pConf.getDatabase().getDatabaseType());
        getVctx().getElements().add(sourceEntity);
        return super.caseEntity(object);
    }

    @Override
    public VisitorContext caseNumericFieldType(NumericFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(GrammarFieldTypeMapping.getMap().get(object.getName()));
        processFieldType(optional);
        return super.caseNumericFieldType(object);
    }

    @Override
    public VisitorContext casePrimitiveField(PrimitiveField object) {
        pfc = object;
        propertyName = object.getId();
        constraint = new Constraint();
        super.doSwitch(object);
        javaProperty.setConstraint(constraint);
        return vctx;
    }

    @Override
    public VisitorContext caseStringFieldType(StringFieldType object) {
        Optional<GrammarFieldType> optional = Optional.of(GrammarFieldTypeMapping.getMap().get(object.getName()));
        processFieldType(optional);
        return super.caseStringFieldType(object);
    }

    @Override
    public VisitorContext caseStringValidators(StringValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getPattern() != null) {
            constraint.setPattern(object.getPattern().substring(1, object.getPattern().length() - 1));
            sourceEntity.setHasConstraints(true);
            javaProperty.setHasConstraints(true);
            javaProperty.setHasConstraints(true);
        }
        if (object.getMinLenght() != null) {
            handleMinLength(object.getMinLenght());
        }
        if (object.getMaxLenght() != null) {
            handleMaxLenght(object.getMaxLenght());
        }
        return super.caseStringValidators(object);
    }

    @Override
    public VisitorContext caseBlobValidators(BlobValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getMinBytesValue() != null) {
            handleMinLength(object.getMinBytesValue());
        }
        if (object.getMaxBytesValue() != null) {
            handleMaxLenght(object.getMaxBytesValue());
        }
        return super.caseBlobValidators(object);
    }

    @Override
    public VisitorContext caseNumericValidators(NumericValidators object) {
        if (object.getRequired() != null) {
            handleRequiredField();
        }
        if (object.getMinValue() != null) {
            handleMinLength(object.getMinValue());
        }
        if (object.getMaxValue() != null) {
            handleMaxLenght(object.getMaxValue());
        }
        return super.caseNumericValidators(object);
    }

    private void handleRequiredField() {
        constraint.setNullable(true);
        sourceEntity.setHasNotNullElements(true);
        sourceEntity.setHasConstraints(true);
        javaProperty.setHasConstraints(true);
    }

    private void handleMinLength(String minLength) {
        constraint.setMin(minLength);
        sourceEntity.setHasConstraints(true);
        javaProperty.setHasConstraints(true);
        javaProperty.setSizeValidation(true);
    }

    private void handleMaxLenght(String maxLength) {
        constraint.setMax(maxLength);
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

    private void visitCardinality(String cardinality) {
        entityAssociation.setType(resolveAssociationType(sourceEntity, cardinality));
        if ((entityAssociation.getType().equals(AssociationType.ONE_TO_MANY)
                || entityAssociation.getType().equals(AssociationType.MANY_TO_MANY))
                && entityAssociation.getToSourcePropertyName() == null) {
            entityAssociation
                    .setToSourcePropertyName(parseToLowerCaseFirstChar(entityAssociation.getSource().getName()));
        }
        entityAssociation.setToTargetPropertyNamePlural(pluralize(entityAssociation.getToTargetPropertyName()));
        entityAssociation.setToSourcePropertyNamePlural(pluralize(entityAssociation.getToSourcePropertyName()));

        entityAssociation.setToTargetPropertyNameUnderscore(
                SchemaPropertiesParser.parseToUnderscore(entityAssociation.getToTargetPropertyName()));
        entityAssociation.setToSourcePropertyNameUnderscore(
                SchemaPropertiesParser.parseToUnderscore(entityAssociation.getToSourcePropertyName()));

        entityAssociation.setToTargetPropertyNameUnderscorePlural(
                SchemaPropertiesParser.parseToUnderscore(pluralize(entityAssociation.getToTargetPropertyName())));
        entityAssociation.setToSourcePropertyNameUnderscorePlural(
                SchemaPropertiesParser.parseToUnderscore(pluralize(entityAssociation.getToSourcePropertyName())));

        assignAssociation(sourceEntity, targetEntity, entityAssociation);
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
            javaProperty = createJavaProperty(pfc, propertyName, grammarPropertyType,
                    pConf.getDatabase().getDatabaseType());
            sourceEntity.addProperty(javaProperty);
            addContentType(sourceEntity, propertyName, pConf.getDatabase().getDatabaseType(), grammarPropertyType);
            GrammarMapping.addImports(sourceEntity.getImports(), javaProperty);
            DataBaseMapping.fillModelMetaData(sourceEntity, javaProperty);
        }
    }

    public void addMetaData(mx.infotec.dads.kukulkan.dsl.kukulkan.Entity entityContext, Entity entity, DatabaseType dbType) {
        String singularName = singularize(entityContext.getName());
        if (singularName == null) {
            singularName = entityContext.getName();
        }
        entity.setTableName(toDataBaseNameConvention(dbType, pluralize(entityContext.getName())));
        entity.setName(entityContext.getName());
        entity.setCamelCaseFormat(SchemaPropertiesParser.parseToPropertyName(singularName));
        entity.setCamelCasePluralFormat(pluralize(entity.getCamelCaseFormat()));
        entity.setHyphensFormat(parseToHyphens(entity.getCamelCaseFormat()));
        entity.setHyphensPluralFormat(parseToHyphens(entity.getCamelCasePluralFormat()));
        entity.setPrimaryKey(createDefaultPrimaryKey(dbType));
    }

    private void assignAssociation(Entity sourceEntity, Entity targetEntity, EntityAssociation entityAssociation) {
        sourceEntity.addAssociation(entityAssociation);
        // if association is a cycle, then sourceEntity already have the
        // association so it is not necessary added to it
        if (!entityAssociation.isCycle()) {
            targetEntity.addAssociation(entityAssociation);
        }
    }

    public String singularize(String word) {
        if (word == null) {
            return null;
        }
        String singularize = inflectorService.singularize(word);
        if (singularize == null) {
            return word;
        } else {
            return singularize;
        }
    }

    public String pluralize(String word) {
        if (word == null) {
            return null;
        }
        String pluralize = inflectorService.pluralize(word);
        if (pluralize == null) {
            return word;
        } else {
            return pluralize;
        }
    }

    private void resolveImports(Entity sourceEntity, Entity targetEntity, EntityAssociation entityAssociation) {
        switch (entityAssociation.getType()) {
        case ONE_TO_ONE:
            break;
        case ONE_TO_MANY:
            sourceEntity.getImports().add(JAVA_UTIL_COLLECTION);
            sourceEntity.getImports().add(JAVA_UTIL_HASH_SET);
            sourceEntity.getImports().add(JSON_IGNORE);
            break;
        case MANY_TO_ONE:
            break;
        case MANY_TO_MANY:
            sourceEntity.getImports().add(JAVA_UTIL_COLLECTION);
            sourceEntity.getImports().add(JAVA_UTIL_HASH_SET);
            targetEntity.getImports().add(JAVA_UTIL_COLLECTION);
            targetEntity.getImports().add(JAVA_UTIL_HASH_SET);
            targetEntity.getImports().add(JSON_IGNORE);
            break;
        default:
            break;
        }
    }

}