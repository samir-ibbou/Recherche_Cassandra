# Search addresses with DSE

This project shows how to use DSE to search into the French addresses database (Bano) available here: https://www.data.gouv.fr/fr/datasets/base-d-adresses-nationale-ouverte-bano/.
It uses DSE Search to execute solr queries to look at your closest addresses, and to autocomplete addresses.
It uses DSE Analytics to load the data from the big CSV file.
It uses Spring Boot with the new 2.0 java driver. This version could be set up without code only conf files.
It uses VueJS for the UI.


## Start DSE with Search and Analytics
This project uses the last current version of DSE 6.7.3

`dse cassandra -k -s`

## Create the schema

```cql
CREATE KEYSPACE poc WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': 1 };
CREATE TABLE poc.address ( id text PRIMARY KEY , num text, type text, zipcode text, city text, coord 'PointType');
```

## Create the search index
`create SEARCH INDEX on poc.address;`

## Get the conf files locally to edit them
```
dsetool get_core_schema poc.address > schema.xml
dsetool get_core_config poc.address > config.xml 
```

## Modify the schema conf file (could be done also in CQL with alter)
```bash
cat <<EOF > schema.xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<schema name="autoSolrSchema" version="1.5">
  <types>
    <fieldType class="org.apache.solr.schema.StrField" name="StrField"/>
 <fieldType class="org.apache.solr.schema.SpatialRecursivePrefixTreeFieldType" geo="true" name="SpatialRecursivePrefixTreeFieldType" spatialContextFactory="org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory" useJtsMulti="false"/>
     <fieldType class="org.apache.solr.schema.TextField" name="TextField">
      <analyzer type="index">
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.ASCIIFoldingFilterFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
        <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" 
                format="solr" ignoreCase="false" expand="true"/>
</analyzer>
      <analyzer type="query">
        <tokenizer class="solr.StandardTokenizerFactory"/>
        <filter class="solr.ASCIIFoldingFilterFactory"/>
        <filter class="solr.LowerCaseFilterFactory"/>
</analyzer>
    </fieldType>
</types>
<fields>
    <field indexed="false" multiValued="false" name="num" type="StrField"/>
    <field indexed="false" multiValued="false" name="type" type="StrField"/>
    <field indexed="false" multiValued="false" name="city" type="StrField"/>
    <field indexed="false" multiValued="false" name="zipcode" type="StrField"/>
    <field docValues="true" indexed="true" multiValued="false" name="id" type="StrField"/>
    <field docValues="false" indexed="true" multiValued="true" name="full_address" type="TextField"/>
    <field indexed="true" multiValued="false" name="coord" type="SpatialRecursivePrefixTreeFieldType"/>
    <copyField source="num" dest="full_address" />
    <copyField source="type" dest="full_address" />
    <copyField source="city" dest="full_address" />
    <copyField source="zipcode" dest="full_address" />
  </fields>
  <uniqueKey>id</uniqueKey>
</schema>
EOF
```

## Create a synonyms file
```bash
cat <<EOF > synonyms.txt
av,avenue
r,rue
EOF
```

## Apply the new search conf
```
dsetool write_resource poc.address name=synonyms.txt file=synonyms.txt
dsetool reload_core poc.address solrconfig=config.xml schema=schema.xml deleteAll=true reindex=true
```

## Load data into DSE with spark
`dse spark --conf spark.cassandra.output.batch.grouping.buffer.size=1 --conf spark.cassandra.output.concurrent.writes=2000` to launch a spark shell

```scala
val df=spark.read.format("csv").option("header", "false").load("file:///home/florent/Downloads/full.csv").toDF("id","num","type","zipcode","city","source","lat","long")
df.withColumn("coord", concat(lit("POINT("),'long,lit(" "),'lat,lit(")"))).drop("source","lat","long").filter("city is not null AND type is not null AND coord is not null AND num is not null").write.cassandraFormat("address","poc").option("confirm.truncate","true").mode(org.apache.spark.sql.SaveMode.Overwrite).save
```


## Example of queries
```
select count(*) from poc.address where solr_query='{"q":"*:*"}';
select * from poc.address where solr_query='{"q":"*:*","fq":"{!geofilt sfield=coord pt=48.856613,2.352222 d=20.0 cache=false}"}';
select * from poc.address WHERE solr_query='{"q":"full_address:(73C AND av AND gamb* AND PAris~) "}';
select * from poc.address WHERE solr_query='{"q":"+full_address:(73 AND av AND gambetta) OR {!geofilt sfield=coord pt=48.856613,2.352222 d=10.0 score=kilometers}^100"}';
select * from poc.address WHERE solr_query='{"q":"+full_address:(73 AND av ) OR {!geofilt sfield=coord pt=43.71613,7.262222 d=50.0 score=recipDistance}","sort":"score desc"}' limit 10;
```

## Launch the application
```bash
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/ ./mvnw spring-boot:run

# or simply if your JAVA_HOME is set up with JAVA 11
./mvnw spring-boot:run
```

## Open you browser and go to
`http://localhost:8080/`


