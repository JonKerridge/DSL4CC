package dsl4cc.DSLprocesses

import dsl4cc.DSLrecords.Acknowledgement
import dsl4cc.DSLrecords.CollectInterface
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

class DSL4CC_Collector implements CSProcess {

  // net channel connections to and from Host
  NetSharedChannelOutput toHost     // writes to Host: fromNodes
  NetSharedChannelInput fromHost  // reads from Host: hostToNodes[i] vcn = 1

  // net channels to manager
  NetChannelOutput requestWork
  NetAltingChannelInput inputWork

  // internal channels to Node
  ChannelOutput workerToNode

  // class associated with the collector that defines the collate and finalise methods
  Class collectClass // methods only no publicly accessible properties

  int workerID    // relative to the cluster
  String outFileName // name of file to which incoming objects will be written unchanged
//  String collectMethodName
  List <String> collectParameters
//  String finaliseMethodName
  List <String> finaliseParameters

  @Override
  void run() {
    ObjectOutputStream outStream
    long startTime, elapsed
    startTime = System.currentTimeMillis()
    if (outFileName != null ) {
      String outFileString = "./${outFileName}${workerID}.dsl4ccout"
      File objFile = new File(outFileString)
      outStream = objFile.newObjectOutputStream()
    }
    else outStream = null

    def collectInstance = collectClass.getDeclaredConstructor().newInstance()

    List collectParameterValues, finaliseParameterValues
    collectParameterValues = []
    finaliseParameterValues = []
    if (collectParameters != null )  collectParameterValues = ExtractParameters.extractParams(collectParameters)
    if (finaliseParameters != null )  finaliseParameterValues = ExtractParameters.extractParams(finaliseParameters)

    Acknowledgement ack
    ack = new Acknowledgement(6, "Collecter-$workerID")
    toHost.write(ack )
    ack = fromHost.read() as Acknowledgement
    assert ack.ackValue == 6 :"Collecter-$workerID expected ack = 6 got ${ack.ackValue}"
//    println "Collecter $workerID running "
    println "Collecter id: $workerID file: $outFileName collectParams: $collectParameterValues finalParams: $finaliseParameterValues running"

    def inData
    RequestSend workRequest
    workRequest = new RequestSend(workerID)
//    println "Collect $workerID has created request $workRequest"
    requestWork.write(workRequest)
//    println "Collect $workerID has sent workRequest"
    inData = inputWork.read()
//    println "Collect $workerID has read $inData"

    while (!(inData instanceof TerminalIndex)) {
      (collectInstance as CollectInterface).collate(inData, collectParameterValues)
      if (outStream != null) outStream.writeObject(inData)
      requestWork.write(new RequestSend(workerID))
      inData = inputWork.read()
    }
    //call the finalise method if it exists and close the output stream
    (collectInstance as CollectInterface).finalise(finaliseParameterValues)
    if (outStream != null) {
      outStream.flush()
      outStream.close()
    }
    // a termination has been read, tell the node
    elapsed = System.currentTimeMillis() - startTime
    TerminalIndex terminalIndex = new TerminalIndex("Collector", workerID, elapsed)
//    println "Collector $workerID terminating after $elapsed milliseconds"
    workerToNode.write(terminalIndex)
  }


}
