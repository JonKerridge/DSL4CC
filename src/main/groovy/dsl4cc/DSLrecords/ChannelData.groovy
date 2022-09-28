package dsl4cc.DSLrecords

import jcsp.net2.NetChannelLocation
import jcsp.net2.NetLocation

class ChannelData implements  Serializable{
  String channelType   // inputWork, sendTo.
  int nodeIndex // index of node within cluster
  List <NetLocation> chanLocation // the location of the net input channels

  ChannelData (){
    channelType = ''
    nodeIndex = -1  // an invalid value
    chanLocation = []
  }

  String toString(){
    String s
    s = "Chan Data t= $channelType, i= $nodeIndex, locs:"
    chanLocation.each{ s = s + "$it, "}
    return s
  }
}
