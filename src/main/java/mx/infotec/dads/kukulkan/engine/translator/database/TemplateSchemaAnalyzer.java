package mx.infotec.dads.kukulkan.engine.translator.database;

import static mx.infotec.dads.kukulkan.engine.util.DataBaseMapping.createIdJavaProperty;
import static mx.infotec.dads.kukulkan.metamodel.util.NameConventionFormatter.toDataBaseNameConvention;
import static mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser.parseToHyphens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.metamodel.ConnectionException;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.UnsupportedDataContextPropertiesException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.springframework.beans.factory.annotation.Autowired;

import mx.infotec.dads.kukulkan.engine.model.EntityHolder;
import mx.infotec.dads.kukulkan.engine.service.GeneratorPrintProvider;
import mx.infotec.dads.kukulkan.engine.service.InflectorService;
import mx.infotec.dads.kukulkan.metamodel.foundation.DatabaseType;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModel;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModelGroup;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;
import mx.infotec.dads.kukulkan.metamodel.foundation.EntityAssociation;
import mx.infotec.dads.kukulkan.metamodel.foundation.JavaDomainModel;
import mx.infotec.dads.kukulkan.metamodel.foundation.Property;
import mx.infotec.dads.kukulkan.metamodel.util.SchemaPropertiesParser;

/**
 * SchemaAnalyzers
 * 
 * @author Daniel Cortes Pichardo
 *
 */
public abstract class TemplateSchemaAnalyzer implements SchemaAnalyzer {

    @Autowired
    private InflectorService inflectorService;

    @Autowired
    private GeneratorPrintProvider printer;

    @Override
    public DomainModel analyse(SchemaAnalyzerContext context) {
        Schema schema = getDefaultSchema(context);
        EntityHolder entityHolder = new EntityHolder();
        for (Table table : schema.getTables()) {
            String entityName = SchemaPropertiesParser.parseToClassName(table.getName());
            Entity entity = entityHolder.getEntity(entityName);
            processTable(context, entity, table);
            List<Column> foreignKeys = table.getForeignKeys();
            for (Column column : table.getColumns()) {
                boolean isForeignKey = foreignKeys.contains(column);
                if (column.isPrimaryKey()) {
                    processPrimaryKey(context, entity, column);
                } else if (column.getType().isBinary()) {
                    processBinaryColumn(context, entity, column, isForeignKey);
                } else if (column.getType().isBoolean()) {
                    processBooleanColumn(context, entity, column, isForeignKey);
                } else if (column.getType().isLargeObject()) {
                    processLargeObjectColumn(context, entity, column, isForeignKey);
                } else if (column.getType().isLiteral()) {
                    processLiteralColumn(context, entity, column, isForeignKey);
                } else if (column.getType().isNumber()) {
                    processNumberColumn(context, entity, column, isForeignKey);
                } else if (column.getType().isTimeBased()) {
                    processTimeBasedColumn(context, entity, column, isForeignKey);
                } else {
                    printer.error("Column type not supported :: " + column.getType().getName() + " for column "
                            + column.getName() + " from table " + table.getName() + " with native column type: "
                            + column.getNativeType());
                }
                entityHolder.update(entity);
            }
            Optional<Property<?>> propertyDisplayField = resolveDisplayField(entity.getProperties());
            propertyDisplayField.ifPresent(entity::setDisplayField);
        }
        processRelationships(context, entityHolder, schema.getRelationships());
        context.getElements().addAll(entityHolder.getEntitiesAsList());
        return getDomainModel(context);
    }

    public abstract Optional<Property<?>> resolveDisplayField(Collection<Property> properties);

    private Schema getDefaultSchema(SchemaAnalyzerContext context) {
        try {
            return DataContextFactoryRegistryImpl.getDefaultInstance()
                    .createDataContext(context.getDataContextProperties()).getDefaultSchema();
        } catch (ConnectionException | UnsupportedDataContextPropertiesException e) {
            throw new SchemaAnalyzerException(e.getMessage(), e);
        } catch (MetaModelException e) {
            throw new SchemaAnalyzerException("Default Schema cannot be returned: ", e);
        }
    }

    public DomainModel getDomainModel(SchemaAnalyzerContext context) {
        DomainModel domainModel = new JavaDomainModel();
        List<DomainModelGroup> domainModelGroupList = new ArrayList<>();
        DomainModelGroup dmg = new DomainModelGroup();
        dmg.setName("");
        dmg.setDescription("Default package");
        dmg.setBriefDescription("Default package");
        dmg.setEntities(context.getElements());
        domainModelGroupList.add(dmg);
        domainModel.setDomainModelGroup(domainModelGroupList);
        return domainModel;
    }

    public void processTable(final SchemaAnalyzerContext context, final Entity entity, final Table table) {
        DatabaseType databaseType = context.getProjectConfiguration().getTargetDatabase().getDatabaseType();
        String singularName = inflectorService.singularize(entity.getName());
        if (singularName == null) {
            singularName = entity.getName();
        }
        if (table.getName() == null || "".equals(table.getName())) {
            entity.setTableName(toDataBaseNameConvention(databaseType, inflectorService.pluralize(entity.getName())));
        } else {
            entity.setTableName(table.getName());
        }
        entity.setUnderscoreName(SchemaPropertiesParser.parsePascalCaseToUnderscore(entity.getName()));
        entity.setCamelCaseFormat(SchemaPropertiesParser.parseToPropertyName(singularName));
        entity.setCamelCasePluralFormat(inflectorService.pluralize(entity.getCamelCaseFormat()));
        entity.setHyphensFormat(parseToHyphens(entity.getCamelCaseFormat()));
        entity.setHyphensPluralFormat(parseToHyphens(entity.getCamelCasePluralFormat()));
        entity.setReferencePhysicalName(context.getPhysicalNameConvention().getPhysicalReferenceNameStrategy()
                .getPhysicalReferenceName(new EntityAssociation(entity, null), true));
        // entity.setReferencePhysicalName(entity.getPrimaryKey().getPhysicalName().getSnakeCase());
        // Refactor in order to have a strategy from differents sources
        entity.setDisplayField(createIdJavaProperty("id"));
    }

    public abstract void processPrimaryKey(SchemaAnalyzerContext context, final Entity entity, Column column);

    public abstract void processTimeBasedColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processNumberColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processLiteralColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processLargeObjectColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processBooleanColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processBinaryColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column, boolean isForeignKey);

    public abstract void processRelationships(final SchemaAnalyzerContext context, final EntityHolder entityHolder,
            final Collection<Relationship> relationships);
}
