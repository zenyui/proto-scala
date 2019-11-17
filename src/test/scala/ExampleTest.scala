import org.apache.log4j.{LogManager, Logger, Level}
import org.apache.spark.sql.Dataset
import org.scalatest.{FunSpec, MustMatchers}

import scala.util.Try

class ExampleTest extends FunSpec with MustMatchers with SparkSupport {

  @transient
  lazy val logger: Logger = LogManager.getLogger(this.getClass)
  logger.setLevel(Level.INFO)

  describe("when unmarshalling normal types") {
    it("works") {
      import com.namely.protobuf.bad_example.event.GoodEvent
      import scalapb.spark.Implicits._

      // make a normal instance of the proto class
      val msg = GoodEvent().withSomeString("some good string")
      println(msg.toString)

      // spark transformation that unpacks the bytes
      val byteArray: Array[Byte] = msg.toByteArray
      val ds: Dataset[Array[Byte]] = spark.createDataset(Seq(byteArray))
      val output: Try[Dataset[GoodEvent]] = Try {
        val tmp = ds.map(GoodEvent.parseFrom)
        tmp.show
        tmp
      }

      // this should succeed
      output.isSuccess must be (true)
      output.map(_.first).toOption must be (Some(msg))
    }
  }

  describe("when unmarshalling a nested map") {
    it("works") {
      import com.namely.protobuf.bad_example.event.BadEvent
      import scalapb.spark.Implicits._

      // instantiate class that has a map attribute
      val msg = BadEvent().withSomeString("some bad string")
      println(msg.toString)

      // make a dataset of the proto byte array
      val byteArray: Array[Byte] = msg.toByteArray
      val ds: Dataset[Array[Byte]] = spark.createDataset(Seq(byteArray))

      // demonstrate that unmarshalling fails
      val output: Try[Dataset[BadEvent]] = Try {
        val tmp = ds.map(BadEvent.parseFrom)
        tmp.show
        tmp
      }

      // print the failure logs
      output.failed.map(t => {
        logger.error(t)
        logger.error(t.getCause)
      })

      // fail the test
      output.isSuccess must be (true)
      output.map(_.first).toOption must be (Some(msg))
    }
  }

}
