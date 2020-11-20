Create BridgeDb Identity Mapping files
======================================

This groovy script creates a Derby file for BridgeDb [1,2] for use in PathVisio,
etc.

The script uses Rhea [3] for each new version release (~monthly), and is based on the [create_bridgedb_metabolites repository](https://github.com/bridgedb/create-bridgedb-metabolites)

Releases
--------

The files are released via the BridgeDb Website: #Fix link: http://www.bridgedb.org/mapping-databases/hmdb-metabolite-mappings/

The mapping files are also archived on Figshare: #Fix link: https://figshare.com/search?q=metabolite+bridgedb+mapping+database&quick=1

License
-------

This repository: New BSD.

Derby License -> http://db.apache.org/derby/license.html
BridgeDb License -> http://www.bridgedb.org/browser/trunk/LICENSE-2.0.txt

Run the script and test the results
-----------------------------------

1. add the jars to your classpath, e.g. on Linux with:

  export CLASSPATH=\`ls -1 *.jar | tr '\n' ':'\`

2. download Rhea tsv mapping file

2.1 ID mappings

Use the below curl command line operations.

  ```
  cd create-bridgedb-interactions/data
  curl -o mappingsrhea.tsv ftp://ftp.expasy.org/databases/rhea/tsv/rhea2xrefs.tsv
  
  ```

4. Update the [createDerby.groovy file](https://github.com/bridgedb/create-bridgedb-hmdb/blob/master/createDerby.groovy#L61) with the new version numbers ("DATASOURCEVERSION" field) and run the script with Groovy: #Update line

  ```
  export CLASSPATH=`ls -1 *.jar | tr '\n' ':'`
  groovy createDerby.groovy
  ```

5. Test the resulting Derby file by opening it in PathVisio

6. Use the BridgeDb QC tool to compare it with the previous mapping file

The BridgeDb repository has a tool to perform quality control (qc) on ID
mapping files:

  ```
  sh qc.sh old.bridge new.bridge
  ```

8. Upload the data to Figshare and update the following pages:

* http://www.bridgedb.org/mapping-databases/hmdb-metabolite-mappings/ #Update link
* http://bridgedb.org/data/gene_database/ #Update link

9. Tag this repository with the DOI of the latest release.

To ensure we know exactly which repository version was used to generate
a specific release, the latest commit used for that release is tagged
with the DOI on Figshare. To list all current tags:

  ```
  git tag
  ```

To make a new tag, run:

  ```
  git tag $DOR
  ````

where $DOI is replaced with the DOI of the release.

10. Inform downstream projects

At least the following projects need to be informed about the availability of the new mapping database:

* BridgeDb webservice
* WikiPathways RDF generation team (Jenkins server)
* WikiPathways indexer (supporting the WikiPathways web service)

References
----------

1. http://bridgedb.org/
2. Van Iersel, M. P., Pico, A. R., Kelder, T., Gao, J., Ho, I., Hanspers, K., Conklin, B. R., Evelo, C. T., Jan. 2010. The BridgeDb framework: standardized access to gene, protein and metabolite identifier mapping services. BMC bioinformatics 11 (1), 5+. http://dx.doi.org/10.1186/1471-2105-11-5
3. Vrandečić, Denny. "Wikidata: a new platform for collaborative data collection." Proceedings of the 21st International Conference on World Wide Web. ACM, 2012. https://doi.org/10.1145/2187980.2188242
4. Morgat, Anne, et al. "Updates in rhea—an expert curated resource of biochemical reactions." Nucleic acids research, 2016. https://dx.doi.org/10.1093%2Fnar%2Fgkw990 