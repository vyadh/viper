package viper.util

import java.awt.Color

object Colours {

  def blend(one: Color, two: Color, a: Int = 0): Color = {
    val rb = (((two.getRGB() & 0x00ff00ff) * a) + ((one.getRGB() & 0x00ff00ff) * (0xff - a))) & 0xff00ff00
    val g  = (((two.getRGB() & 0x0000ff00) * a) + ((one.getRGB() & 0x0000ff00) * (0xff - a))) & 0x00ff0000
    return new Color((two.getRGB() & 0xff000000) | ((rb | g) >> 8))
  }

}
