package dsl4cc.DSLrecords

interface CollectInterface <T> {
  // T is the type of the object read by the collect phase
  void collate (T data, List params)
  void finalise(List params)
}