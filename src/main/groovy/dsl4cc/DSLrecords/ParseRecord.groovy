package dsl4cc.DSLrecords


import jcsp.net2.NetLocation

class ParseRecord implements Serializable {
  String typeName                 // host, emit, work, collect
  String hostAddress
  List <String> fixedIPAddresses  // used for fixed nodes with specific role
  int nodes       //number of nodes in cluster
  int workers     //number of worker processes in each node of cluster
  String classNameString  // name of class that emits objects from emitter (assumes method called create)
  String methodNameString // name of method to be executed in node (work, collect)
  List <String> parameterString  // a list of parameters in their String representation (work only)
  List <List <String>> emitParameterString // list of list of parameters used by each emitter
  String outFileName  // base name of file to which collected objects are written, multiples differentiated by WorkerID
  String finaliseNameString  // used in collect type only
  List <List <String>> collectParameterString // list of list of collectParameters used by each collect method
  List <List <String>> finaliseParameterString // list of list of finaliseParameters used in each finalise method
  NetLocation outputManagerLocation // NetChannelLocation of manager that manages output from the Cluster
  NetLocation inputManagerLocation   // NetChannelLocation of manager that manages input from Cluster
  List <String> allocatedNodeIPs  // completed by host to indicate node ips

  ParseRecord(){
    fixedIPAddresses = []
    parameterString = []
    emitParameterString = []
    collectParameterString = []
    finaliseParameterString = []
    allocatedNodeIPs = []
  } // constructor

  @Override
  String toString() {
    String s =  "type=$typeName, host=$hostAddress, fixedIP=$fixedIPAddresses, nodes=$nodes, workers=$workers, " +
        "\n\tclass=$classNameString, emitParams= $emitParameterString" +
        "\n\tmethod=$methodNameString, params=$collectParameterString, " +
        "\n\tfinalise method = $finaliseNameString, params = $finaliseParameterString, outFile - $outFileName" +
        "\n\timcn=$inputManagerLocation, omcn=$outputManagerLocation, " + "allocIps=$allocatedNodeIPs"

    return s
  }
}
