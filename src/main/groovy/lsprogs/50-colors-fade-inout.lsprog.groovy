package lsprogs


import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import java.awt.*

import static java.awt.Color.*

class ColorsFadeInOutProgram extends LedStripProgram {

  static def MAX_DARK_LEVEL = 11
  static def RAINBOW = [RED, ORANGE, MAGENTA, GREEN, BLUE]

  static Random random = new Random()

  String name() {
    "ColorsFadeInOut"
  }

  int order() {
    50
  }

  String loadPredicate() {
    if (leds.numPixels % 2 != 0) {
      "This program supports only strips with even number of leds"
    }
  }

  void clean() throws Exception {
    ledDriver.allOff()
  }

  /**
   * Here you should to realize your amazing light show
   */
  void algorithm() throws Exception {

    colorsFadeInOut([RED, ORANGE, MAGENTA, GREEN, BLUE], 3)
  }

  void colorsFadeInOut(colours, count) throws Exception {
    count.times {
      colours.each {
        def colour = makeDarkest(it)
        MAX_DARK_LEVEL.times {
          colour = colour.brighter()
          setAllPixels(colour)
          leds.render()
          delay 50
        }
        MAX_DARK_LEVEL.times {
          colour = colour.darker()
          setAllPixels(colour)
          leds.render()
          delay 50
        }
      }
    }
  }

  void setAllPixels(colour) {
    for (int px = 0; px < leds.numPixels; px++) {
      leds.setPixelColour(px, colour.getRGB())
    }
  }

  static Color makeDarker(colour, count) {
    count.times {
      colour = colour.darker()
    }
    return colour
  }

  static Color makeDarkest(colour) {
    return makeDarker(colour, MAX_DARK_LEVEL)
  }
}
