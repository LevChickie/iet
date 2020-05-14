package com.complexible.common.csv.generator;

import com.complexible.common.csv.CSV2RDF;
import org.openrdf.model.*;

public class StatementGenerator {
    private final ValueGenerator<Resource> subject;
    private final ValueGenerator<URI> predicate;
    private final ValueGenerator<Value> object;

    public StatementGenerator(ValueGenerator<Resource> s, ValueGenerator<URI> p, ValueGenerator<Value> o) {
        this.subject = s;
        this.predicate = p;
        this.object = o;
    }

    public Statement generate(int rowIndex, String[] row) {
        Resource s = subject.generate(rowIndex, row);
        URI p = predicate.generate(rowIndex, row);
        Value o = object.generate(rowIndex, row);
        return CSV2RDF.FACTORY.createStatement(s, p, o);
    }
}
