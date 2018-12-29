package lsprogs


import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import java.awt.*

import static java.awt.Color.*

class JustColorsProgram extends LedStripProgram {

  static Random random = new Random()

  String name() {
    "JustColors"
  }

  int order() {
    30
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

    justColors((Color[])[RED, ORANGE, MAGENTA, GREEN, BLUE], 5)
  }

  void justColors(Color[] colours, count) {

    def bulbsCount = (int) (leds.numPixels / 2)
    def colorStart = random.nextInt(colours.length)

    def colour = 0
    colours.length.times {

      def colorPos = colorStart
      bulbsCount.times {

        if (colorPos == colour) {
          leds.setPixelColour(it * 2, colours[colorPos].getRGB())
          leds.setPixelColour(it * 2 + 1, colours[colorPos].getRGB())
        }

        colorPos++
        colorPos = colorPos >= colours.length ? 0 : colorPos
      }
      leds.render()
      delay 350

      colour ++
    }

    (colours.length * count).times {

      def colorPos = colorStart
      bulbsCount.times {

        leds.setPixelColour(it * 2, colours[colorPos].getRGB())
        leds.setPixelColour(it * 2 + 1, colours[colorPos].getRGB())

        colorPos++
        colorPos = colorPos >= colours.length ? 0 : colorPos
      }
      leds.render()
      delay 350

      colorStart++
      colorStart = colorStart >= colours.length ? 0 : colorStart
    }
  }
}
