package Tests

import dsl4cc.DSLInvoke.RunNode

class TestNode6 {
  static void main(String[] args) {
    def node = new RunNode("127.0.0.1", "127.0.0.6").invoke()
  }
}
