package util

object Utils {

  def toDouble(r: String): Option[Double] = {
    try {
      Some(r.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  def toInt(r: String): Option[Int] = {
    try {
      Some(r.toInt)
    } catch {
      case e: Exception => None
    }
  }
}
