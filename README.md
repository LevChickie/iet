csv2rdf
=======

csv2rdf is a simple tool for generating RDF output from CSV/TSV files. The conversion is done by a template file
that shows how the RDF output will look for one row. See [examples/cars](examples/cars) for details. 

Building
--------

`mvn -B package` will create a local build in the `build` sub-directory.

Running
-------

You can run the tool with the command `java -jar build/CSV2RDF.jar` followed by arguments.

You can see the help screen with the command `java -jar build/CSV2RDF.jar help convert`.

You can run the conversion for the example using `java -jar build/CSV2RDF.jar convert examples/cars/template.ttl examples/cars/cars.csv cars.ttl`. 
