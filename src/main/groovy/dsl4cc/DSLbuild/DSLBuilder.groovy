package dsl4cc.DSLbuild

import dsl4cc.DSLprocesses.DSL4CC_Host
import dsl4cc.DSLrecords.ExtractVersion
import dsl4cc.DSLrecords.ParseRecord
import groovy_jcsp.PAR

class DSLBuilder {
  String structureFile
  Class emitClass, collectClass
  String version = "1.1.1"

  DSLBuilder(String structureFileName, Class emitClass, Class collectClass){
    structureFile = structureFileName +".dsl4ccstruct"
    this.emitClass = emitClass
    this.collectClass = collectClass
  }

  boolean builder(){
    if (!ExtractVersion.extractVersion(version)){
      println "DSL4CC:Version $version needs to be downloaded, please modify the gradle.build file"
      System.exit(-1)
    }
    File objFile = new File(structureFile)
    List<ParseRecord> structure = []
    objFile.withObjectInputStream { inStream ->
      inStream.eachObject { structure << (it as ParseRecord) } }
    int requiredManagers = structure.size() - 2
    int totalNodes, totalWorkers
    totalNodes = 0
    totalWorkers = 0
    structure.each {
      totalNodes = totalNodes + it.nodes
      totalWorkers = totalWorkers + (it.nodes * it.workers)
    }
    String hostIP = structure[0].hostAddress
    println "DSL4CC: Build system version $version"
    def host = new DSL4CC_Host(hostIP, requiredManagers, totalNodes, totalWorkers, structure, emitClass, collectClass)
    new PAR([host]).run()
    println "DSL4CC: Build system has finished"
    return true
  } // builder

}
