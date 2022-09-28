package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.ExtractParameters
import dsl4cc.DSLrecords.RequestSend
import dsl4cc.DSLrecords.TerminalIndex
import jcsp.lang.CSProcess
import jcsp.lang.ChannelOutput
import jcsp.net2.Any2NetChannel
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.NetSharedChannelInput
import jcsp.net2.NetSharedChannelOutput

class DSL4CC_Collecter implements CSProcess{

  // net channel connections to and from Host
  NetSharedChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1


  // net channels
  NetChannelOutput requestWork
  NetAltingChannelInput inputWork
  // internal channels to Node
  ChannelOutput workerToNode
  int workerID    // relative to the cluster
  String outFileName
  String collectMethodName
  List <String> collectParameters
  String finaliseMethodName
  List <String> finaliseParameters

  @Override
  void run() {
    String outFileString = "./${outFileName}${workerID}.dsl4ccout"
    File objFile = new File(outFileString)
    def outStream = objFile.newObjectOutputStream()

    List collectParameterValues, finaliseParameterValues
    collectParameterValues = []
    if (collectParameters != null )  collectParameterValues = ExtractParameters.extractParams(collectParameters)
    if (finaliseParameters != null )  finaliseParameterValues = ExtractParameters.extractParams(finaliseParameters)
    println "Collecter $workerID, $collectMethodName, $collectParameterValues, $finaliseMethodName, $finaliseParameterValues"

    Acknowledgement ack
    ack = new Acknowledgement(6, "Collecter-$workerID")
    toHost.write(ack )
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 6 :"Collecter-$workerID expected ack = 6 got ${ack.ackValue}"
    println "Collecter $workerID running "

    Class CollectClass
    def inData
    RequestSend workRequest
    workRequest = new RequestSend(workerID)
//    println "Collect $workerID has created request $workRequest"
    requestWork.write(workRequest)
//    println "Collect $workerID has sent workRequest"
    inData = inputWork.read()
//    println "Collect $workerID has read $inData"
    // get its class name and create an instance
    String className = inData.getClass().getName()
    CollectClass = Class.forName(className)
    Object collectClass = CollectClass.getDeclaredConstructor().newInstance()
    while (!(inData instanceof TerminalIndex)) {
      inData.&"$collectMethodName"(collectParameterValues)
      outStream.writeObject(inData)
      requestWork.write(new RequestSend(workerID))
      inData = inputWork.read()
    }
    //call the finalise method if it exists and close the output stream
    if (finaliseMethodName != null) collectClass.&"$finaliseMethodName"(finaliseParameterValues)
    outStream.flush()
    outStream.close()
    // a termination has been read, tell the node
    TerminalIndex terminalIndex = new TerminalIndex(workerID)
    workerToNode.write(terminalIndex)
    println "Collector $workerID has terminated"
  }
}
