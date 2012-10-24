package viper.ui

import viper.domain._

/**
 * Manage state of a subscriber for visualisation. This class holds the unread count,
 * as well as the highest severity (non-read) item in the subscription. This could have
 * been more nicely done with a Calculation in GlazedLists, but updating the list with
 * changes to a large number of elements (like 10,000) is pretty slow.
 */
case class Subscribed(subscriber: Subscriber) {

  /** Unread counts for each severity. */
  private var unreadCounts = new Array[Int](Severities.count)


  def unread = unreadCounts.sum

  def severity: Severity = {
    for (i <- Severities.max.ordinal to Severities.min.ordinal by -1) {
      if (unreadCounts(i) > 0) {
        return Severities.values.find(_.ordinal == i).get
      }
    }
    Info
  }


  def added(rs: Seq[Record]) {
    rs.foreach(added(_))
  }

  def added(r: Record) {
    if (!isRead(r)) {
      update(r, 1)
    }
  }

  def deleted(ds: Seq[Record]) {
    ds.foreach(deleted(_))
  }

  def deleted(r: Record) {
    if (!isRead(r)) {
      update(r, -1)
    }
  }

  def read(r: Record, oldRead: Boolean) {
    if (!oldRead) {
      update(r, -1)
    }
  }

  def unread(r: Record, oldRead: Boolean) {
    if (oldRead) {
      update(r, 1)
    }
  }


  private def update(record: Record, inc: Int) {
    unreadCounts(record.severity.ordinal) += inc
  }

  private def isRead(record: Record) = record match {
    case r: Readable => r.read
    case _ => false //todo maybe true, so we always show colour of non-Readable lists
  }

}
