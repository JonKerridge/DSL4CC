package dsl4cc.DSLbuild

import dsl4cc.DSLprocesses.DSL4CC_Host
import dsl4cc.DSLrecords.ParseRecord
import groovy_jcsp.PAR

class DSLBuild {
  String structureFile

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
}
