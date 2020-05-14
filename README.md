> This project was updated by team **csv-szindikatus** for the Integration and Verification Techniques course at the Budapest University of Technology and Economics. The documentation is available inside the **doc** folder in **Hungarian** language.

CSV2RDF
=======

CSV2RDF is a simple tool for **generating RDF** output **from CSV/TSV** files.<br>
Conversion is done by a **template file** describing one row of the desired output.<br>
See [examples/cars](examples/cars) for details. 

### Building

**Build jar** file in the build directory:<br>
`mvn -B package`

### Running

List **available commands**:<br>
`java -jar build/CSV2RDF.jar help convert`

**Run conversion** (assuming a directory named out has been created):<br>
`java -jar build/CSV2RDF.jar convert examples/cars/template.ttl examples/cars/cars.csv out/cars.ttl`
