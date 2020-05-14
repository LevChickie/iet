package com.complexible.common.csv.generator;

import com.complexible.common.csv.CSV2RDF;
import com.complexible.common.csv.provider.ValueProvider;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;


public class TemplateLiteralGenerator extends TemplateValueGenerator<Literal> {
    private final URI datatype;
    private final String lang;

    public TemplateLiteralGenerator(Literal literal, ValueProvider[] providers) {
        super(literal.getLabel(), providers);

        this.datatype = literal.getDatatype();
        this.lang = literal.getLanguage().orElse(null);
    }

    public Literal generate(int rowIndex, String[] row) {
        String value = applyTemplate(rowIndex, row);
        if (datatype != null)
            return CSV2RDF.FACTORY.createLiteral(value, datatype);

        if (lang != null)
            return CSV2RDF.FACTORY.createLiteral(value, lang);

        return CSV2RDF.FACTORY.createLiteral(value);
    }
}
