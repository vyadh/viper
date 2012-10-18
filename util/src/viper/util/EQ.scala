package viper.util

import java.awt.EventQueue

object EQ {

  def later(work: => Unit) {
    EventQueue.invokeLater(new Runnable {
      def run() {
        work
      }
    })
  }

}
