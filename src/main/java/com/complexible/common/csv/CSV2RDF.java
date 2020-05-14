// Copyright (c) 2014, Clark & Parsia, LLC. <http://www.clarkparsia.com>

package com.complexible.common.csv;

import com.complexible.common.csv.generator.*;
import com.complexible.common.csv.logger.ProcessBehaviourLogger;
import com.complexible.common.csv.provider.RowNumberProvider;
import com.complexible.common.csv.provider.RowValueProvider;
import com.complexible.common.csv.provider.UUIDProvider;
import com.complexible.common.csv.provider.ValueProvider;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.Option;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.RDFHandlerBase;


/**
 * Converts a CSV file to RDF based on a given template.
 *
 * @author Evren Sirin
 */
@Command(name = "convert", description = "Runs the conversion.")
public class CSV2RDF implements Runnable {

    /**
     * The charset used to read the input file.
     */
    private static final Charset INPUT_CHARSET = Charset.defaultCharset();

    /**
     * The charset used to write the output file.
     */
    public static final Charset OUTPUT_CHARSET = StandardCharsets.UTF_8;


    public static final ValueFactory FACTORY = ValueFactoryImpl.getInstance();
    public static final ProcessBehaviourLogger processLogger = new ProcessBehaviourLogger();

    @Option(name = "--no-header", arity = 0, description = "If csv file does not contain a header row")
    private final boolean noHeader = false;

    @Option(name = { "-s", "--separator" }, description = "Separator character used in the csv file or ',' by default.")
    private final String separator = String.valueOf(CSVParser.DEFAULT_SEPARATOR);

    @Option(name = { "-q", "--quote" }, description = "Quote character used in the csv file or '\"' by default.")
    private final String quote = String.valueOf(CSVParser.DEFAULT_QUOTE_CHARACTER);

    @Option(name = { "-e", "--escape" }, description = "Escape character used in the csv file or '\\' by default.")
    private final String escape = String.valueOf(CSVParser.DEFAULT_ESCAPE_CHARACTER);

    @Arguments(required = true, description = "File arguments. The extension of template file and output file determines the RDF format that will be used for them (.ttl = Turtle, .nt = N-Triples, .rdf = RDF/XML)",
            title = "templateFile, csvFile, outputFile" )

    private List<String> files;
    private int inputRows = 0;
    private int outputTriples = 0;

    public void run() {
        Preconditions.checkArgument(files.size() >= 3, "Missing arguments");
        Preconditions.checkArgument(files.size() <= 3, "Too many arguments");

        File templateFile = new File(files.get(0));
        File inputFile = new File(files.get(1));
        File outputFile =  new File(files.get(2));
        processLogger.logInfo("CSV to RDF conversion started...");
        processLogger.logInfo("Template: " + templateFile);
        processLogger.logInfo("Input   : " + inputFile);
        processLogger.logInfo("Output  : " + outputFile);
        
        try (Reader in = Files.newReader(inputFile, INPUT_CHARSET);
             CSVReader reader = new CSVReader(in, toChar(separator), toChar(quote), toChar(escape));
             Writer out = Files.newWriter(outputFile, OUTPUT_CHARSET)){
            String[] row = reader.readNext();

            Preconditions.checkNotNull(row, "Input file is empty!");

            RDFFormat format = Rio.getParserFormatForFileName(outputFile.getName()).orElse(RDFFormat.TURTLE);
            RDFWriter writer = Rio.createWriter(format, out);

            Template template = new Template(Arrays.asList(row), templateFile, writer);

            if (noHeader) {
                template.generate(row, writer);
            }

            while ((row = reader.readNext()) != null) {
                template.generate(row, writer);
            }

            writer.endRDF();
        }
        catch (IOException e) {
            processLogger.logError("IOException occurred during run");
        }
        catch(RDFHandlerException e)
        {
            processLogger.logError("RDFHandlerException occurred during run");
        }
        catch(Exception e)
        {
            processLogger.logError("Exception occurred during run");
        }
        processLogger.logInfo(String.format("Converted %,d rows to %,d triples%n", inputRows, outputTriples));

    }

    private static char toChar(String value) {
        Preconditions.checkArgument(value.length() == 1, "Expecting a single character but got %s", value);
        return value.charAt(0);
    }

    private static ParserConfig getParserConfig() {
        ParserConfig config = new ParserConfig();

        Set<RioSetting<?>> aNonFatalErrors = Sets.newHashSet(
                        BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES);

        config.setNonFatalErrors(aNonFatalErrors);

        config.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, false);
        config.set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, false);
        config.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        config.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
        config.set(BasicParserSettings.VERIFY_RELATIVE_URIS, false);

        return config;
    }

    private class Template {
        private final List<StatementGenerator> stmts = Lists.newArrayList();
        private final List<ValueProvider> valueProviders = Lists.newArrayList();

        Template(List<String> cols, File templateFile, RDFWriter writer) throws IOException {
            parseTemplate(cols, templateFile, writer);
        }

        private String insertPlaceholders(List<String> cols, File templateFile) throws IOException {
            Pattern p = Pattern.compile("([\\$|\\#]\\{[^}]*\\})");

            Matcher m = p.matcher(Files.toString(templateFile, INPUT_CHARSET));
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String var = m.group(1);
                String varName = var.substring(2, var.length() - 1);
                ValueProvider valueProvider = valueProviderFor(varName, cols);
                Preconditions.checkArgument(valueProvider != null, "Invalid template variable", var);
                valueProvider.setHashed((var.charAt(0) == '#'));
                m.appendReplacement(sb, valueProvider.getPlaceholder());
                valueProviders.add(valueProvider);
            }
            m.appendTail(sb);

            return sb.toString();
        }

        private ValueProvider valueProviderFor(String varName, List<String> cols) {
            if (varName.equalsIgnoreCase("_ROW_")) {
                return new RowNumberProvider();
            }
            if (varName.equalsIgnoreCase("_UUID_")) {
                return new UUIDProvider();
            }
            
            int index = -1;            
            if (!noHeader) {
                index = cols.indexOf(varName);
            }
            else {
                try {
                    index = Integer.parseInt(varName);
                }
                catch (NumberFormatException e) {
                    if (varName.length() == 1) {
                        char c = Character.toUpperCase(varName.charAt(0));
                        if (c >= 'A' && c <= 'Z') {
                            index = c - 'A';
                        }
                    }
                }
            }
            return index == -1 ? null : new RowValueProvider(index);
        }

        private void parseTemplate(List<String> cols, File templateFile, final RDFWriter writer) throws IOException {
            String templateStr = insertPlaceholders(cols, templateFile);

            RDFFormat format = Rio.getParserFormatForFileName(templateFile.getName()).orElse(RDFFormat.TURTLE);
            RDFParser parser = Rio.createParser(format);

            parser.setParserConfig(getParserConfig());
            parser.setRDFHandler(new RDFHandlerBase() {
                @SuppressWarnings("rawtypes")
                private final Map<Value, ValueGenerator> generators = Maps.newHashMap();

                @Override
                public void startRDF() {
                    writer.startRDF();
                }

                @Override
                public void handleNamespace(String prefix, String uri) {
                    writer.handleNamespace(prefix, uri);
                }

                @Override
                public void handleStatement(Statement st) {
                    ValueGenerator<Resource> subject = generatorFor(st.getSubject());
                    ValueGenerator<URI> predicate = generatorFor(st.getPredicate());
                    ValueGenerator<Value> object = generatorFor(st.getObject());
                    stmts.add(new StatementGenerator(subject, predicate, object));
                }

                @SuppressWarnings({ "unchecked"})
                private <V extends Value> ValueGenerator<V> generatorFor(V value) {
                    ValueGenerator<V> generator = generators.get(value);
                    if (generator != null) {
                        return generator;
                    }
                    if (value instanceof BNode) {
                        generator = (ValueGenerator<V>) new BNodeGenerator();
                    }
                    else {
                        String str = value.toString();
                        ValueProvider[] providers = providersFor(str);
                        if (providers.length == 0) {
                            generator = new ConstantValueGenerator<>(value);
                        }
                        else if (value instanceof URI) {
                            generator = (ValueGenerator<V>) new TemplateURIGenerator(str, providers);
                        }
                        else {
                            Literal literal = (Literal) value;
                            generator = (ValueGenerator<V>) new TemplateLiteralGenerator(literal, providers);
                        }
                    }
                    generators.put(value, generator);
                    return generator;
                }

                private ValueProvider[] providersFor(String str) {
                    List<ValueProvider> result = Lists.newArrayList();
                    for (ValueProvider provider : valueProviders) {
                        if (str.contains(provider.getPlaceholder())) {
                            result.add(provider);
                        }  
                    }
                    return result.toArray(new ValueProvider[0]);
                }
            });

            parser.parse(new StringReader(templateStr), "urn:");
        }

        void generate(String[] row, RDFHandler handler) {
            inputRows++;
            for (StatementGenerator stmt : stmts) {
                outputTriples++;
                handler.handleStatement(stmt.generate(inputRows, row));
            }
        }
    }

}
