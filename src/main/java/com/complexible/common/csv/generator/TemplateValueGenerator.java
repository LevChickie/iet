package com.complexible.common.csv.generator;


import com.complexible.common.csv.provider.ValueProvider;
import org.openrdf.model.Value;

abstract class TemplateValueGenerator<V extends Value> implements ValueGenerator<V> {
    private final String template;
    private final ValueProvider[] providers;

    TemplateValueGenerator(String template, ValueProvider[] providers) {
        this.template = template;
        this.providers = providers;
    }

    String applyTemplate(int rowIndex, String[] row) {
        String result = template;
        for (ValueProvider provider : providers) {
            String value = provider.provide(rowIndex, row);
            if (value != null && !value.isEmpty()) {
                result = result.replace(provider.getPlaceholder(), value);
            }
        }
        return result;
    }
}
