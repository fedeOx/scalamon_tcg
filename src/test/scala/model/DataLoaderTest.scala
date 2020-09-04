package model

import org.scalatest.FlatSpec

class DataLoaderTest extends FlatSpec {
  behavior of "A DataLoader"

  it must "load a not empy list" in {
    val l = DataLoader.loadData(SetType.base)
    assert(l.nonEmpty)
  }

  it must "work with every indexed data source" in {
    for (set <- SetType.values) {
      val l = DataLoader.loadData(set)
      assert(l.nonEmpty)
    }
  }
}
