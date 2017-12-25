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
package mx.infotec.dads.kukulkan.engine.service.technology.conacyt;

import static mx.infotec.dads.kukulkan.metamodel.util.LayerUtils.addCommonDataModelElements;
import static mx.infotec.dads.kukulkan.metamodel.util.JavaFileNameParser.formatToImportStatement;
import static mx.infotec.dads.kukulkan.metamodel.util.JavaFileNameParser.formatToPackageStatement;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.infotec.dads.kukulkan.engine.templating.service.TemplateService;
import mx.infotec.dads.kukulkan.metamodel.foundation.DomainModelElement;
import mx.infotec.dads.kukulkan.metamodel.foundation.ProjectConfiguration;
import mx.infotec.dads.kukulkan.metamodel.util.BasePathEnum;
import mx.infotec.dads.kukulkan.metamodel.util.NameConventions;

/**
 * Repository Layer Task
 * 
 * @author Daniel Cortes Pichardo
 *
 */
@Service("conacytServiceLayerTask")
public class ServiceLayerTask extends AbstractConacytLayerTask {

    @Autowired
    private TemplateService templateService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLayerTask.class);

    @Override
    public void doForEachDataModelElement(ProjectConfiguration pConf, Collection<DomainModelElement> dmElementCollection,
            Map<String, Object> model, String dmgName) {
        LOGGER.debug("doForEachDataModelElement");
        String basePackage = pConf.getPackaging() + dmgName;
        for (DomainModelElement dmElement : dmElementCollection) {
            addCommonDataModelElements(pConf, model, basePackage, dmElement);
            model.put("package", formatToPackageStatement(basePackage, pConf.getServiceLayerName()));
            model.put("packageImpl", formatToPackageStatement(basePackage, pConf.getServiceLayerName(), "impl"));
            model.put("importRepository", formatToImportStatement(basePackage, pConf.getDaoLayerName(),
                    dmElement.getName() + NameConventions.DAO));
            model.put("importService", formatToImportStatement(basePackage, pConf.getServiceLayerName(),
                    dmElement.getName() + NameConventions.SERVICE));
            templateService.fillModel(dmElement, pConf.getId(), "conacyt/service.ftl", model, BasePathEnum.SRC_MAIN_JAVA,
                    basePackage.replace('.', '/') + "/" + dmgName + "/" + pConf.getServiceLayerName() + "/"
                            + dmElement.getName() + NameConventions.SERVICE + ".java");
            templateService.fillModel(dmElement, pConf.getId(), "conacyt/serviceImpl.ftl", model, BasePathEnum.SRC_MAIN_JAVA,
                    basePackage.replace('.', '/') + "/" + dmgName + "/" + pConf.getServiceLayerName() + "/impl/"
                            + dmElement.getName() + NameConventions.SERVICE_IMPLEMENTS + ".java");
        }
    }

    @Override
    public void visitDomainModelElement(ProjectConfiguration pConf, Collection<DomainModelElement> dmElementCollection,
            Map<String, Object> propertiesMap, String dmgName, DomainModelElement dmElement, String basePackage) {
        // TODO Auto-generated method stub
        
    }

}
