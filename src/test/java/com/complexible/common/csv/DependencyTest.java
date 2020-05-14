package com.complexible.common.csv;


import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains tests for the project dependencies.
 */
public class DependencyTest {

    /**
     * Check if tests run properly.
     */
    @Test
    public void sanityCheck() {

        assertTrue(true);

    }

     /**
     * Check if the Rio.getParserFormatForFileName() method works properly.
     */
    @Test
    public void formatDetectionTest() {

        String templateFilename = "examples/cars/template.ttl";
        Optional<RDFFormat> format = Rio.getParserFormatForFileName(templateFilename);

        assertEquals(Optional.of(RDFFormat.TURTLE), format);

    }

    /**
     * Check if the Rio.createWriter() method creates the writer properly.
     */
    @Test
    public void createWriterTest() {

        RDFFormat format = RDFFormat.TURTLE;
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, System.out);

        /* test passes if no exceptions were thrown */

    }

    /**
     * Check if the Preconditions.checkArguments works correctly.
     */
    @Test
    public void preconditionsCheckArgumentTest(){
        String value = "T";
        try{
            Preconditions.checkArgument(value.length() == 1, "Expecting a single character but got %s", value);
        }
        catch(Exception e){
            /*To make a failure rather than an error if the test did not pass
              Either IllegalArgumentException or NullPointerException is thrown
            */
            assertTrue(false);
        }
        try{
            Preconditions.checkArgument(value.length() == 1, "Expecting a single character but got multiple");
        }
        catch(IllegalArgumentException e){
            /*The method with only 2 arguments can only throw IllegalArgumentException*/
            fail();
        }

        /*Test passes if neither of the calls throw an exception*/
    }

    @Test
    public void preconditionsCheckNotNullTest(){
        String[] row = new String[]{"Hello", "this", "is", "a", "test"};

        try{
        Preconditions.checkNotNull(row, "Input file is empty!");
        }
        catch(NullPointerException e){
            /*The method can only throw a NullPointerException*/
            fail();
        }

        /*Test passes of no exceptions were thrown */
    }

    @Test
    public void listsSizeTest(){
        List<String> list1 = Lists.newArrayList();

        assertEquals(0, list1.size());
        assertEquals("class java.util.ArrayList", list1.getClass().toString());
    }
}
