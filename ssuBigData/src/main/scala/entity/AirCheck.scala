package entity

case class AirCheck(date: String,
                    BEN: String,
                    CO: Option[Double],
                    EBE: Option[Double],
                    MXY: Option[Double],
                    NMHC: Option[Double],
                    NO_2: Option[Double],
                    NOx: Option[Double],
                    OXY: Option[Double],
                    O_3: Option[Double],
                    PM10: Option[Double],
                    PXY: Option[Double],
                    SO_2: Option[Double],
                    TCH: Option[Double],
                    TOL: Option[Double],
                    station: Option[Int]
                   )
