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
package mx.infotec.dads.kukulkan.engine.service;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import mx.infotec.dads.kukulkan.metamodel.context.GeneratorContext;
import mx.infotec.dads.kukulkan.metamodel.generator.Generator;

/**
 * Default Generation service.
 *
 * @author Daniel Cortes Pichardo
 */
@Service("defaultGenerationService")
public class GenerationServiceImpl implements GenerationService {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerationServiceImpl.class);

    /** The application context. */
    @Autowired
    ApplicationContext applicationContext;

    /* (non-Javadoc)
     * @see mx.infotec.dads.kukulkan.engine.service.GenerationService#process(mx.infotec.dads.kukulkan.metamodel.foundation.GeneratorContext, mx.infotec.dads.kukulkan.metamodel.generator.Generator)
     */
    @Override
    public void process(GeneratorContext context, Generator generator) {
        generator.process(context);
    }

    /* (non-Javadoc)
     * @see mx.infotec.dads.kukulkan.engine.service.GenerationService#findGeneratorByName(java.lang.String)
     */
    @Override
    public Optional<Generator> findGeneratorByName(String name) {
        Collection<Generator> generators = applicationContext.getBeansOfType(Generator.class).values();
        LOGGER.debug("Generators found: {}", generators.size());
        for (Generator generator : generators) {
            if (generator.getName().equals(name)) {
                LOGGER.debug("Generator found: {}", generator.getName());
                return Optional.of(generator);
            }
        }
        return Optional.empty();
    }

    /* (non-Javadoc)
     * @see mx.infotec.dads.kukulkan.engine.service.GenerationService#findAllGenerators()
     */
    @Override
    public Collection<Generator> findAllGenerators() {
        return applicationContext.getBeansOfType(Generator.class).values();
    }
}
