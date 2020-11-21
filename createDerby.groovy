// export CLASSPATH=`ls -1 *.jar | tr '\n' ':'`

import java.text.SimpleDateFormat;
import java.util.Date;
import groovy.util.slurpersupport.NodeChildren;

import org.bridgedb.IDMapperException;
import org.bridgedb.DataSource;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.bridgedb.rdb.construct.DBConnector;
import org.bridgedb.rdb.construct.DataDerby;
import org.bridgedb.rdb.construct.GdbConstruct;
import org.bridgedb.rdb.construct.GdbConstructImpl3;

commitInterval = 500
genesDone = new java.util.HashSet();
linksDone = new java.util.HashSet();

//unitReport = new File("creationNEW.xml")
// unitReport << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
//unitReport << "<testsuite tests=\"12\">\n"

GdbConstruct database = GdbConstructImpl3.createInstance(
  "rhea_interactions", new DataDerby(), DBConnector.PROP_RECREATE
);
database.createGdbTables();
database.preInsert();

blacklist = new HashSet<String>();
//blacklist.add("C00350") //Example of blacklist item.

////Registering Datasources to create mappings
enzymenom = DataSource.register ("E", "Enzyme Nomenclature").asDataSource()
keggreact = DataSource.register ("Rk", "KEGG Reaction").asDataSource()
reactomereact = DataSource.register ("Re", "Reactome").asDataSource()
metacycDS = DataSource.register ("Mc", "MetaCyc").asDataSource() //Not recognised as BioDataSource
ecocycDS = DataSource.register ("Eco", "EcoCyc").asDataSource() //Not recognised as BioDataSource
macieDS = DataSource.register ("Ma", "MACiE").asDataSource() //Not recognised as BioDataSource
unipathDS = DataSource.register ("Up", "Unipathway").asDataSource() //Not recognised as BioDataSource
//Old, can be removed after test run:
wikidataDS = DataSource.register ("Wd", "Wikidata").asDataSource()
omimDS = BioDataSource.OMIM //SysCode: Om
doDS = DataSource.register ("Do", "Disease Ontology").asDataSource() //Syscode part of BridgeDb library (not released officially yet).


String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
database.setInfo("BUILDDATE", dateStr);
database.setInfo("DATASOURCENAME", "EBI-RHEA");
database.setInfo("DATASOURCEVERSION", "115");
database.setInfo("DATATYPE", "standard-interaction"); 
database.setInfo("SERIES", "Interaction"); 

def addXRef(GdbConstruct database, Xref ref, String node, DataSource source, Set genesDone, Set linkesDone) {
   id = node.trim()
   if (id.length() > 0) {
     // println "id($source): $id"
     ref2 = new Xref(id, source);
     if (!genesDone.contains(ref2.toString())) {
       if (database.addGene(ref2) != 0) {
          println "Error (addXRef.addGene): " + database.recentException().getMessage()
          println "                 id($source): $id"
       }
       genesDone.add(ref2.toString())
     }
     if (!linksDone.contains(ref.toString()+ref2.toString())) {
       if (database.addLink(ref, ref2) != 0) {
         println "Error (addXRef.addLink): " + database.recentException().getMessage()
         println "                 id(origin):  " + ref.toString()
         println "                 id($source): $id"
       }
       linksDone.add(ref.toString()+ref2.toString())
     }
   }
}

def addAttribute(GdbConstruct database, Xref ref, String key, String value) {
   id = value.trim()
   // println "attrib($key): $id"
   if (id.length() > 255) {
     println "Warn: attribute does not fit the Derby SQL schema: $id"
   } else if (id.length() > 0) {
     if (database.addAttribute(ref, key, value) != 0) {
       println "Error (addAttrib): " + database.getException().getMessage()
     }
   }
}

//// load the Rhea content

/*

// Rhea-EC
counter = 0
error = 0
new File("omim2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], omimDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (OMIM)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// Rhea-Kegg
counter = 0
error = 0
new File("do2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], doDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (DiseaseOntology)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// Rhea-Reactome
counter = 0
error = 0
new File("cui2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], cuiDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (UMLSCUI)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.



// Rhea-Metacyc
counter = 0
error = 0
new File("orpha2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], orphaDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (OrphaNet)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// Rhea Ecocyc
counter = 0
error = 0
new File("mesh2wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], meshDS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (MeSH)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.

// rhea - macie 
counter = 0
error = 0
new File("icd92wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], icd9DS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ICD-9)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.


// rhea-unipathway
counter = 0
error = 0
new File("icd102wikidata.csv").eachLine { line,number ->
  if (number == 1) return // skip the first line

  fields = line.split(",")
  rootid = fields[0].substring(31)
  Xref ref = new Xref(rootid, wikidataDS);
  if (!genesDone.contains(ref.toString())) {
    addError = database.addGene(ref);
    if (addError != 0) println "Error (addGene): " + database.recentException().getMessage()
    error += addError
    linkError = database.addLink(ref,ref);
    if (linkError != 0) println "Error (addLinkItself): " + database.recentException().getMessage()
    error += linkError
    genesDone.add(ref.toString())
  }

  // add external identifiers
  addXRef(database, ref, fields[1], icd10DS, genesDone, linksDone);

  counter++
  if (counter % commitInterval == 0) {
    println "Info: errors: " + error + " (ICD-10)"
    database.commit()
  }
}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" //Not implemented (yet) for disease IDs.
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"NamesFound\"/>\n"
//unitReport << "</testsuite>\n"

*/

database.commit();
database.finalize();
