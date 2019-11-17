# scalapb example

Small example demonstrating proto files for many services.

### Current issue

spark can't serialize the `scalapb.TypeMapper` class, so a spark transformation that uses the generated class `.parseFrom(Array[Byte])` constructor fails. I'm able to recreate this by defining a proto3 object with a `map<string, string>` field.

To reproduce this example, run `sbt test`
- [proto file](./protorepo/bad_example/event.proto)
- [test](./src/test/scala/ExampleTest.scala)
