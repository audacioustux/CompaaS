package common.types

opaque type ParsingError = String
object ParsingError {
  def apply(throwable: Throwable): ParsingError =
    s"JSON parsing error due to $throwable"
}
