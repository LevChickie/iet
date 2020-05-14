package com.complexible.common.csv.generator;

import com.complexible.common.csv.CSV2RDF;
import com.complexible.common.csv.provider.ValueProvider;
import org.openrdf.model.URI;
public class TemplateURIGenerator extends TemplateValueGenerator<URI> {

    public TemplateURIGenerator(String template, ValueProvider[] providers) {
        super(template, providers);
    }

    public URI generate(int rowIndex, String[] row) {
        return CSV2RDF.FACTORY.createURI(applyTemplate(rowIndex, row));
    }
}
