package mx.infotec.dads.kukulkan.engine.translator.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.ConnectionException;
import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.factory.DataContextFactoryRegistryImpl;
import org.apache.metamodel.factory.UnsupportedDataContextPropertiesException;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

import mx.infotec.dads.kukulkan.engine.translator.dsl.EntityHolder;
import mx.infotec.dads.kukulkan.metamodel.foundation.DatabaseType;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModel;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModelGroup;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;
import mx.infotec.dads.kukulkan.metamodel.foundation.JavaDomainModel;

/**
 * SchemaAnalyzers
 * 
 * @author Daniel Cortes Pichardo
 *
 */
public abstract class TemplateSchemaAnalyzer implements SchemaAnalyzer {

    @Override
    public DomainModel analyse(SchemaAnalyzerContext context) {
        Schema schema = getDefaultSchema(context);
        EntityHolder entityHolder = new EntityHolder();
        for (Table table : schema.getTables()) {
            DatabaseType databaseType = context.getProjectConfiguration().getDatabase().getDatabaseType();
            Entity entity = entityHolder.getEntity(table.getName(), databaseType);
            processTable(context, entity, table);
            for (Column column : table.getColumns()) {
                if (column.isPrimaryKey()) {
                    processPrimaryKey(context, entity, column);
                } else if (column.getType().isBinary()) {
                    processBinaryColumn(context, entity, column);
                } else if (column.getType().isBoolean()) {
                    processBooleanColumn(context, entity, column);
                } else if (column.getType().isLargeObject()) {
                    processLargeObjectColumn(context, entity, column);
                } else if (column.getType().isLiteral()) {
                    processLiteralColumn(context, entity, column);
                } else if (column.getType().isNumber()) {
                    processNumberColumn(context, entity, column);
                } else if (column.getType().isTimeBased()) {
                    processTimeBasedColumn(context, entity, column);
                } else {
                    throw new SchemaAnalyzerException("Column type not supported :: " + column.getType().getName());
                }
                entityHolder.update(entity);
            }
        }
        processRelationships(context, entityHolder, schema.getRelationships());
        return getDomainModel(context);
    }

    private Schema getDefaultSchema(SchemaAnalyzerContext context) {
        try {
            return DataContextFactoryRegistryImpl.getDefaultInstance()
                    .createDataContext(context.getDataContextProperties()).getDefaultSchema();
        } catch (UnsupportedDataContextPropertiesException e) {
            throw new SchemaAnalyzerException("Unsupported DataContext: ", e);
        } catch (ConnectionException e) {
            throw new SchemaAnalyzerException("Connection Exception: ", e);
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

    public abstract void processPrimaryKey(SchemaAnalyzerContext context, final Entity entity, Column column);

    public abstract void processTable(final SchemaAnalyzerContext context, final Entity entity, final Table table);

    public abstract void processColumn(final SchemaAnalyzerContext context, final Entity entity, final Column column);

    public abstract void processRelationships(final SchemaAnalyzerContext context, final EntityHolder entityHolder,
            final Collection<Relationship> relationships);

    public abstract void processTimeBasedColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);

    public abstract void processNumberColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);

    public abstract void processLiteralColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);

    public abstract void processLargeObjectColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);

    public abstract void processBooleanColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);

    public abstract void processBinaryColumn(final SchemaAnalyzerContext context, final Entity entity,
            final Column column);
}
