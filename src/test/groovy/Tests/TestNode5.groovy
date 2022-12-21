package Tests

import dsl4cc.DSLInvoke.RunNode

class TestNode5 {
  static void main(String[] args) {
    def node = new RunNode("127.0.0.1", "127.0.0.5").invoke()
  }
}
