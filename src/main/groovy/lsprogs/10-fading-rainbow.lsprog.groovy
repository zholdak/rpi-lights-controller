package lsprogs

import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import java.awt.Color

import static java.awt.Color.BLUE
import static java.awt.Color.GREEN
import static java.awt.Color.MAGENTA
import static java.awt.Color.ORANGE
import static java.awt.Color.RED

class FadingRainbowProgram extends LedStripProgram {

  static def MAX_DARK_LEVEL = 11
  static def RAINBOW = [RED, ORANGE, MAGENTA, GREEN, BLUE]

  static Random random = new Random()

  String name() {
    "FadingRainbow"
  }

  int order() {
    10
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

    fadingRainbow((Color[]) [RED, ORANGE, MAGENTA, GREEN, BLUE], 100)
  }

  class Bulb {

    def colour
    def level
    def dir
    def depth
    def out = false

    Bulb(Color colour, int level, int depth) {
      this.colour = colour
      this.level = level < 1 ? 1 : (level > depth ? depth : level)
      this.dir = level < (int) (depth / 2) ? 1 : -1
      this.depth = depth
    }

    /** Change level by cycle between [1........and....bulb.depth] */
    static boolean nextLevel(Bulb bulb) {
      if (!bulb.out) {
        bulb.level += bulb.dir
        if (bulb.level >= bulb.depth || bulb.level <= 0) bulb.dir = -bulb.dir
        true
      } else {
        if (bulb.level < bulb.depth + 3) {
          bulb.level++
          true
        } else false
      }
    }
  }

  void fadingRainbow(Color[] colours, count) {

    def bulbsCount = (int) (leds.numPixels / 2)
    def bulbs = new Bulb[bulbsCount]
    def levelDepth = MAX_DARK_LEVEL
    def colorPos = random.nextInt(colours.length)

    // fill bulbs strip
    bulbsCount.times {
      def level = random.nextInt(levelDepth) + 1
      bulbs[it] = new Bulb(colours[colorPos], level, levelDepth)
      colorPos++
      colorPos = colorPos >= colours.length ? 0 : colorPos
    }

    // showing bulbs from top and fading simultaneously
    def window = bulbsCount
    (levelDepth * count).times {

      ((int)(bulbsCount-1)..0).each {

        Bulb.nextLevel(bulbs[it])

        if (it + 1 >= window) {
          def rgb = makeDarker(bulbs[it].colour, bulbs[it].level).getRGB()
          leds.setPixelColour(it * 2, rgb)
          leds.setPixelColour(it * 2 + 1, rgb)
        }
      }
      leds.render()
      delay 50

      if (window > 1) window --
    }

    // fade it out
    def bulbOut = bulbsCount - 1
    def haveBulbsOn = true
    while (haveBulbsOn) {

      haveBulbsOn = false
      ((int)(bulbsCount-1)..0).each {

        if (it == bulbOut) {
          bulbs[it].out = true
        }

        def chngResult = Bulb.nextLevel(bulbs[it])

        haveBulbsOn = chngResult ? chngResult : haveBulbsOn

        def rgb = makeDarker(bulbs[it].colour, bulbs[it].level).getRGB()
        leds.setPixelColour(it * 2, rgb)
        leds.setPixelColour(it * 2 + 1, rgb)
      }
      leds.render()
      delay 50

      if (bulbOut >= 0) bulbOut --
    }

    leds.allOff()
  }

  static Color makeDarker(colour, count) {
    count.times {
      colour = colour.darker()
    }
    return colour
  }
}