package lsprogs


import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import java.awt.Color

import static java.awt.Color.*

class ColoursUpDownSingleProgram extends LedStripProgram {

  String name() {
    "ColoursUpDownSingle"
  }

  int order() {
    20
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

    coloursUpDownSingle([RED, ORANGE, MAGENTA, GREEN, BLUE], 3)
  }

  void coloursUpDownSingle(colours, count) {
    count.times {
      colours.each {
        def colour = makeDarker(it, 3)
        ((int)(leds.numPixels/2-1)..0).each {
          leds.setPixelColour(it * 2, colour.getRGB())
          leds.setPixelColour(it * 2 + 1, colour.getRGB())
          leds.render()
          delay 20
        }
        delay 2000
      }
    }
  }

  static Color makeDarker(colour, count) {
    count.times {
      colour = colour.darker()
    }
    return colour
  }
}
