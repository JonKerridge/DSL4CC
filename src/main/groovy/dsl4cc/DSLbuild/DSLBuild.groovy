package dsl4cc.DSLbuild

import dsl4cc.DSLprocesses.DSL4CC_Host
import dsl4cc.DSLrecords.ParseRecord
import groovy_jcsp.PAR
import jcsp.userIO.Ask

class DSLBuild {
  String structureFile

  DSLBuild (String structureFilename){
    structureFile = structureFilename
  }

  boolean builder(){
    File objFile = new File(structureFile)
    List<ParseRecord> structure = []
    objFile.withObjectInputStream { inStream -> inStream.eachObject { structure << it }
    }
    int requiredManagers = structure.size() - 2
    int totalNodes, totalWorkers
    totalNodes = 0
    totalWorkers = 0
    structure.each {
      totalNodes = totalNodes + it.nodes
      totalWorkers = totalWorkers + (it.nodes * it.workers)
    }
    String hostIP = structure[0].hostAddress
    def host = new DSL4CC_Host(hostIP, requiredManagers, totalNodes, totalWorkers, structure)
    new PAR([host]).run()
    println "Build system has finished"
    return true
  }

  static void runBuild(){
    String dslStructPath, dslStructName, dslStructFile
    dslStructPath = Ask.string(" DSL4CC Build: Please specify the path to parsed application : " )
    dslStructName = Ask.string(" DSL4CC Build: Please specify the name of the parsed application : ")
    dslStructFile = dslStructPath + "/" + dslStructName + "dsl4ccstruct"
    DSLBuild build = new DSLBuild(dslStructFile)
    assert build.builder():"DSL4CC Build: Build process has failed"
  }
}
