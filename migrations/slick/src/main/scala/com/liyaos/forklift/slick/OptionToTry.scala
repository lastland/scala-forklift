package com.liyaos.forklift.slick

import scala.util.{Failure, Success, Try}

object OptionToTry {
  implicit class OptionOps[A](a: Option[A]) {
    def toTry(ex: Exception): Try[A] = a match {
      case Some(x) => Success(x)
      case None => Failure(ex)
    }
  }
}
