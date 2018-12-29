package lsprogs

import com.zholdak.rbpi.lightscontroller.program.LedStripProgram

import static com.diozero.ws281xj.PixelAnimations.delay
import static com.diozero.ws281xj.PixelColour.RAINBOW

class RainbowProgram extends LedStripProgram {

  String name() {
    "Rainbow"
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

    rainbowColours()
  }

  void rainbowColours() {
    for (i in 0..250) {
      for (pixel in 0..leds.numPixels - 1) {
        leds.setPixelColour(pixel, RAINBOW[(i + pixel) % RAINBOW.length])
      }
      leds.render()
      delay(50)
    }
  }
}
