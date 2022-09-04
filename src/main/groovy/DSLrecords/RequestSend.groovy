package DSLrecords

class RequestSend implements  Serializable {
    int index

    RequestSend(int index){
        this.index = index
    }

    String toString(){
        return "RS: $index"
    }
}
