package mx.infotec.dads.kukulkan.engine.translator.database;

import static mx.infotec.dads.kukulkan.engine.util.DataBaseMapping.createDefaultPrimaryKey;

import java.util.Collection;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Relationship;
import org.springframework.stereotype.Service;

import mx.infotec.dads.kukulkan.engine.translator.dsl.EntityHolder;
import mx.infotec.dads.kukulkan.metamodel.foundation.DatabaseType;
import mx.infotec.dads.kukulkan.metamodel.foundation.Entity;

/**
 * DefatulSchemaAnalyzer
 * 
 * @author Daniel Cortes Pichardo
 *
 */
@Service
public class DefaultSchemaAnalyzer extends TemplateSchemaAnalyzer {

    @Override
    public void processPrimaryKey(SchemaAnalyzerContext context, Entity entity, Column column) {
        DatabaseType databaseType = context.getProjectConfiguration().getDatabase().getDatabaseType();
        entity.setPrimaryKey(createDefaultPrimaryKey(databaseType));
    }

    @Override
    public void processTimeBasedColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
    }
    
    @Override
    public void processNumberColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processLiteralColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processLargeObjectColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processBooleanColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processBinaryColumn(SchemaAnalyzerContext context, Entity entity, Column column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processRelationships(SchemaAnalyzerContext context, EntityHolder entityHolder,
            Collection<Relationship> relationships) {
        // TODO Auto-generated method stub

    }

}