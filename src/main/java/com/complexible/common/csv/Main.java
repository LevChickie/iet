package com.complexible.common.csv;

import io.airlift.command.Cli;
import io.airlift.command.Help;

class Main {
        public static void main(String[] args) {
        try {
            Cli.<Runnable> builder("csv2rdf").withDescription("Converts a CSV file to RDF based on a given template")
                            .withDefaultCommand(CSV2RDF.class).withCommand(CSV2RDF.class).withCommand(Help.class)
                            .build().parse(args).run();
        }
        catch (Exception e) {
            CSV2RDF.processLogger.logError("ERROR: "+(e.getMessage()));

        }
    }
}
