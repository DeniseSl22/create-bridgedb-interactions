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
rheaDS = DataSource.register ("Rh", "Rhea").asDataSource()
enzymenom = DataSource.register ("E", "Enzyme Nomenclature").asDataSource()
keggreact = DataSource.register ("Rk", "KEGG Reaction").asDataSource()
reactomereact = DataSource.register ("Re", "Reactome").asDataSource()
metacycDS = DataSource.register ("Mc", "MetaCyc").asDataSource() //Not recognised as BioDataSource
ecocycDS = DataSource.register ("Eco", "EcoCyc").asDataSource() //Not recognised as BioDataSource
macieDS = DataSource.register ("Ma", "MACiE").asDataSource() //Not recognised as BioDataSource
//unipathDS = DataSource.register ("Up", "Unipathway").asDataSource() //Not recognised as BioDataSource, Not part of Rhea download (any more?)

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

// Split up data per column
counter = 0
error = 0
new File("data/mappingsrhea.tsv").eachLine { line,number ->
  if (number == 1) return // skip the first line


	fields = line.split(/\t/)
	def rootid ;
	def direction ;
	def masterid ;
	def databaseid ;
	def databasename ;
	if (fields.size() >= 5) {
		(rootid, direction, masterid, databaseid, databasename) = line.split(/\t/) 
	}
        //if (number == 3) println databasename ////Test if data is read correctly
	//if (number == 4) println rootid	////Test if data is read correctly

  

 //Rhea-EC 
	if(databasename == "EC" ) {  //Only use the 4th column for EC mappings if it contains "EC".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, enzymenom, genesDone, linksDone);	//Updated database name (enzymenom)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (EC Nomenclature)" //Updated database error output (EC Nomenclature)
     	    database.commit()
	}
  }

 //Rhea-KEGG 
	if(databasename == "KEGG_REACTION" ) {  //Only use the 4th column for Kegg mappings if it contains "KEGG_REACTION".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, keggreact, genesDone, linksDone);	//Updated database name (keggreact)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (KEGG Reactions)" //Updated database error output (KEGG Reactions)
     	    database.commit()
	}
  }

 //Rhea-MetaCyC
	if(databasename == "METACYC" ) {  //Only use the 4th column for MetaCyc mappings if it contains "METACYC".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, metacycDS, genesDone, linksDone);	//Updated database name (metacycDS)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (MetaCyc)" //Updated database error output (MetaCyc)
     	    database.commit()
	}
  }

 //Rhea-EcoCyC
	if(databasename == "ECOCYC" ) {  //Only use the 4th column for EcoCyc mappings if it contains "ECOCYC".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, ecocycDS, genesDone, linksDone);	 //Updated database name (ecocycDS)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (EcoCyc)" //Updated database error output (EcoCyc)
     	    database.commit()
	}
  }

 //Rhea-MaCIE
	if(databasename == "MACIE" ) {  //Only use the 4th column for MaCIE mappings if it contains "MACIE".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, macieDS, genesDone, linksDone);	 //Updated database name (macieDS)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (MaCIE)" //Updated database error output (MaCIE)
     	    database.commit()
	}
  }

 //Rhea-Reactome
	if(databasename == "REACTOME" ) {  //Only use the 4th column for Reactome mappings if it contains "REACTOME".
	  Xref ref = new Xref(rootid, rheaDS);
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
	  addXRef(database, ref, databaseid, reactomereact, genesDone, linksDone);	 //Updated database name (reactomereact)

	  counter++
	  if (counter % commitInterval == 0) {
      	    println "Info: errors: " + error + " (Reactome Reactions)" //Updated database error output (Reactome interactions)
     	    database.commit()
	}
  }


}
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"CASNumbersFound\"/>\n" 	//No tests implemented (yet) for interaction IDs.
//unitReport << "  <testcase classname=\"WikidataCreation\" name=\"NamesFound\"/>\n"		///These lines are merely here as examples
//unitReport << "</testsuite>\n"

database.commit();
database.finalize();
