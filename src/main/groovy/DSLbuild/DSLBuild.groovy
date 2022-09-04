package DSLbuild

import DSLprocesses.DSL4CC_Host
import DSLrecords.ParseRecord
import groovy_jcsp.PAR

class DSLBuild {
  String structureFile

  boolean builder(){
    File objFile = new File(structureFile)
    List<ParseRecord> structure = []
    objFile.withObjectInputStream { inStream -> inStream.eachObject { structure << it }
    }
    int requiredManagers = structure.size() - 2
    int totalNodes = 0
    structure.each { totalNodes = totalNodes + it.nodes }
    String hostIP = structure[0].hostAddress
    def host = new DSL4CC_Host(hostIP, requiredManagers, totalNodes, structure)
    new PAR([host]).run()
    println "Build system has finished"
    return true
  }
}
