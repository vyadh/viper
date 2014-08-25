/*
 * Copyright 2012-2014 Kieron Wilkinson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package viper.util

import javax.swing.{ImageIcon, Icon}
import com.kitfox.svg.SVGCache
import com.kitfox.svg.app.beans.SVGIcon
import java.awt.{Transparency, GraphicsEnvironment, Dimension}
import java.io.{File, InputStream}
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * SVG takes a while to decode, so this class converts them to PNG as needed.
 */
object IconCache {

  val cacheDir = new File(System.getProperty("java.io.tmpdir"), ".viper")
  cacheDir.mkdirs()

  def load(name: String, size: Int, stream: InputStream): Icon = {
    val cacheFile = cachePath(name, size)

    read(cacheFile) match {
      case Some(image) => new ImageIcon(image)
      case None => convertAndCache(name, size, stream)
    }
  }

  private def cachePath(name: String, size: Int): File = {
    new File(cacheDir, name + '_' + size + ".png")
  }

  private def read(file: File): Option[BufferedImage] = {
    if (file.exists()) {
      Option(ImageIO.read(file))
    } else {
      None
    }
  }

  /** Next time app is started up, cached version should be available. */
  private def convertAndCache(name: String, size: Int, stream: InputStream): Icon = {
    val svg = loadIconSVG(name, size, stream)
    val image = bufferedImage(svg)

    val cacheFile = cachePath(name, size)
    write(image, cacheFile)

    svg
  }

  private def loadIconSVG(name: String, size: Int, stream: InputStream): Icon = {
    val svgURI = SVGCache.getSVGUniverse.loadSVG(stream, name)
    val icon = new SVGIcon
    icon.setSvgURI(svgURI)
    icon.setScaleToFit(true)
    icon.setAntiAlias(true)
    val height = size
    val width = ((size.toDouble / icon.getIconHeight) * icon.getIconWidth).toInt
    icon.setPreferredSize(new Dimension(width, height))
    icon
  }

  private def bufferedImage(icon: Icon): BufferedImage = {
    val configuration = GraphicsEnvironment
          .getLocalGraphicsEnvironment()
          .getDefaultScreenDevice()
          .getDefaultConfiguration()
    val image = configuration.createCompatibleImage(
      icon.getIconWidth, icon.getIconHeight, Transparency.TRANSLUCENT);
    val g = image.createGraphics
    icon.paintIcon(null, g, 0, 0)
    g.dispose()
    image
  }

  private def write(image: BufferedImage, file: File) {
    ImageIO.write(image, "PNG", file);
  }

}
