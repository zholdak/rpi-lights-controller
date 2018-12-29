package lsprogs


import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import java.awt.*

import static java.awt.Color.*

class FunnyColorsWithFadeProgram extends LedStripProgram {

  String name() {
    "FunnyColorsWithFade"
  }

  int order() {
    40
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

    funnyColorsWithFade((Color[])[RED, ORANGE, MAGENTA, GREEN, BLUE], 3)
  }

  void funnyColorsWithFade(Color[] colours, count) {
    def levelDepth = 6
    def changeEvery = 5
    def colorLevels = new Color[levelDepth + 1][colours.length]
    colours.length.times {
      colorLevels[levelDepth][it] = colours[it]
    }
    levelDepth.times {
      def lvl = it
      colours.length.times {
        colorLevels[levelDepth - 1 - lvl][it] = makeDarker(colorLevels[levelDepth][it], lvl + 1)
      }
    }

    def bulbsCount = (int) (leds.numPixels / 2)
    def colorStart = 0
    def levelPos = 0
    def levelDir = 1
    def chngColor = 0
    ((levelDepth * 2 + 1) * count * changeEvery).times {
      def colorPos = colorStart
      bulbsCount.times {
        leds.setPixelColour(it * 2, colorLevels[levelPos][colorPos].getRGB())
        leds.setPixelColour(it * 2 + 1, colorLevels[levelPos][colorPos].getRGB())

        colorPos++
        colorPos = colorPos >= colours.length ? 0 : colorPos
      }
      leds.render()
      delay 100
      levelPos += levelDir
      if (levelPos >= levelDepth || levelPos <= 0) levelDir = -levelDir

      if (chngColor % changeEvery == 0) {
        colorStart++
        colorStart = colorStart >= colours.length ? 0 : colorStart
      }

      chngColor ++
    }
  }

  static Color makeDarker(colour, count) {
    count.times {
      colour = colour.darker()
    }
    return colour
  }
}
