package scala.migrations.core.tools.helpers

import java.io.{InputStream, OutputStream}
import scala.util.{Try, Success, Failure}
import scala.annotation.tailrec
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.postfixOps
import scala.sys.process._

object Helpers {

  trait HelperProcessBuilder {
    @tailrec final def noOutput(
      flag: Boolean, input: InputStream, text: String): Unit = {
      var byte: Array[Byte] = new Array(1)
      val f = Future {
        if (input.read(byte) != -1) ()
      }
      val fl = text.endsWith("Waiting for source changes... (press enter to interrupt)\n")
      val duration = if (fl) 2 seconds else 30 seconds
      val r = Try { Await.result(f, duration) }
      val s = new String(byte)
      print(s)
      r match {
        case Success(_) => noOutput(fl, input, text + s)
        case Failure(ex) => ex match {
          case te: TimeoutException =>
            if (fl) input.close() else throw te
        }
      }
    }
  }

  implicit class SeqHelperProcessBuilder(val pb: Seq[String])
      extends HelperProcessBuilder {
    private def runCommandUntilNoOutput(command: Seq[String]) {
      command run new ProcessIO(_.close(), noOutput(false, _, ""), _.close())
    }

    def !-> = {
      runCommandUntilNoOutput(pb)
    }
  }

  implicit class StringHelperProcessBuilder(val pb: String)
      extends HelperProcessBuilder {
    private def runCommandUntilNoOutput(command: String) {
      command run new ProcessIO(_.close(), noOutput(false, _, ""), _.close())
    }

    def !-> = {
      runCommandUntilNoOutput(pb)
    }
  }
}
