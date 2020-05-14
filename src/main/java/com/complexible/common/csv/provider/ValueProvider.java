package com.complexible.common.csv.provider;

import com.complexible.common.csv.CSV2RDF;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.util.UUID;

public abstract class ValueProvider {


    private final String placeholder = UUID.randomUUID().toString();

    private boolean hashed;

    public String provide(int rowIndex, String[] row) {
         String value = provideValue(rowIndex, row);
         if (value != null && hashed) {
            HashCode hash = Hashing.sha1().hashString(value, CSV2RDF.OUTPUT_CHARSET);
            value = BaseEncoding.base32Hex().omitPadding().lowerCase().encode(hash.asBytes());
         }
         return value;
     }

     protected abstract String provideValue(int rowIndex, String[] row);

    public String getPlaceholder() {
        return placeholder;
    }

    public boolean isHashed() {
        return hashed;
    }

    public void setHashed(boolean hashed) {
        this.hashed = hashed;
    }
}
