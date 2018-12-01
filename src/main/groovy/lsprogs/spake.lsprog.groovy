package lsprogs

import com.zholdak.rbpi.lightscontroller.program.LedStripProgram
import com.zholdak.rbpi.lightscontroller.utils.CyclicIterator

import java.awt.Color
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger

import static java.util.Arrays.asList

class SnakeProgram extends LedStripProgram {

  String name() {
    "Snake"
  }

  int order() {
    20
  }

  String loadPredicate() {
    if (ledDriver.numPixels % 2 != 0) {
      "This program supports only strips with even number of leds"
    }
  }


  void clean() throws Exception {
    ledDriver.allOff()
  }

  private void render(Bulb[] strip) {
    for (bulbNo in 0..(strip.length - 1)) {
      ledDriver.setPixelColour(bulbNo * 2, Bulb.get(strip, bulbNo).color.RGB)
      ledDriver.setPixelColour((bulbNo * 2) + 1, Bulb.get(strip, bulbNo).color.RGB)
    }
    ledDriver.render()
    sleep(25)
  }

  static CyclicIterator<Color> rainbowIterator = asList(Color.GREEN, Color.BLUE, Color.RED, Color.YELLOW, Color.CYAN,
      Color.ORANGE, Color.PINK, Color.LIGHT_GRAY, Color.MAGENTA)

  void algorithm() throws Exception {
    try {

      int FWD = 1
      int REV = -1

      int tailLen = 5

      // create and init strip
      Bulb[] strip = new Bulb[leds.numPixels / 2]

      // render strip on advance
      Phaser phaser = new Phaser() {
        boolean onAdvance(int phase, int parties) {
          render(strip)
          sleep(25)
          false
        }
      }

      if (terminated) return

      snakeTrainsForwardThenBackward(
          strip, phaser,
          new SnakeCfg(0, 1, FWD, tailLen, 0, 50, 1, Color.GREEN),
          new SnakeCfg(49, 1, REV, tailLen, 0, 50, 1, Color.BLUE)
      )

      if (terminated) return

      crossingSnakes(
          strip, phaser,
          new SnakeCfg(0, 1, FWD, 7, 0, 50, 1, Color.YELLOW),
          new SnakeCfg(49, 1, REV, 7, 0, 50, 1, Color.YELLOW)
      )

      if (terminated) return

//      pushingAndCrossingSnakes(
//          strip, phaser,
//          new SnakeCfg(24, 1, REV, tailLen, 0, 25, 2),
//          new SnakeCfg(25, 1, FWD, tailLen, 25, 25, 2)
//      )
//
//      if (terminated) return
//
//      pushingAndCrossingSnakes(
//          strip, phaser,
//          new SnakeCfg(0, 1, FWD, tailLen, 0, 50, 2),
//          new SnakeCfg(49, 1, REV, tailLen, 0, 50, 2)
//      )

    } finally {
      clean()
    }
  }

  private static void snakeTrainsForwardThenBackward(Bulb[] strip, Phaser phaser, SnakeCfg... cfgs) {

    int distance = 10
    for (cfgNo in 0..(cfgs.length - 1)) {
      int steps = 0
      int carriageCount = 25
      // register monitor & first snake
      phaser.bulkRegister(2)
      // start first snake
      new SnakeThread(strip, phaser, cfgs[cfgNo], rainbowIterator.next()).start()
      // monitor
      while (true) {
        if (phaser.registeredParties > 1) {
          phaser.arriveAndAwaitAdvance()
          if (++steps % distance == 0 && carriageCount-- > 0) {
            phaser.register()
            new SnakeThread(strip, phaser, cfgs[cfgNo], rainbowIterator.next()).start()
          }
        } else {
          phaser.arriveAndDeregister()
          break
        }
      }
    }
  }

  private static void crossingSnakes(Bulb[] strip, Phaser phaser, SnakeCfg cfg1, SnakeCfg cfg2) {

    int carriageCount = 10
    // register monitor & first snakes
    phaser.bulkRegister(3)
    // start first snake
    new SnakeThread(strip, phaser, cfg1, rainbowIterator.next()).start()
    new SnakeThread(strip, phaser, cfg2, rainbowIterator.next()).start()
    // monitor
    while (true) {
      if (phaser.registeredParties > 1) {
        phaser.arriveAndAwaitAdvance()
      } else {
        if (-- carriageCount > 0) {
          phaser.bulkRegister(2)
          new SnakeThread(strip, phaser, cfg1, rainbowIterator.next()).start()
          new SnakeThread(strip, phaser, cfg2, rainbowIterator.next()).start()
        } else {
          phaser.arriveAndDeregister()
          break
        }
      }
    }
  }

  private static class SnakeCfg {

    private int pos
    private int step
    private int direction
    private int tailLen
    private int boundBegin
    private int boundLen
    private int pass
    private Color colour

    SnakeCfg(int pos, int step, int direction, int tailLen, int boundBegin, int boundLen, int pass, Color colour) {
      this.pos = pos
      this.step = step
      this.direction = direction
      this.tailLen = tailLen
      this.boundBegin = boundBegin
      this.boundLen = boundLen
      this.pass = pass
      this.colour = colour
    }

    int getPos() {
      return pos
    }

    int getStep() {
      return step
    }

    int getDirection() {
      return direction
    }

    int getTailLen() {
      return tailLen
    }

    int getBoundBegin() {
      return boundBegin
    }

    int getBoundLen() {
      return boundLen
    }

    int getPass() {
      return pass
    }

    Color getColour() {
      return colour
    }
  }

  private static class SnakeThread extends Thread {

    private static AtomicInteger layerCounter = new AtomicInteger(0)

    private int layer

    private Phaser phaser
    private Bulb[] strip

    private int pos
    private int step
    private int direction
    private int tailLen
    private int boundBegin
    private int boundLen
    private int pass
    private Color colour

    SnakeThread(Bulb[] strip, Phaser phaser, SnakeCfg cfg) {
      this.layer = layerCounter.incrementAndGet()
      this.strip = strip
      this.phaser = phaser
      this.pos = cfg.pos
      this.step = cfg.step
      this.direction = cfg.direction
      this.tailLen = cfg.tailLen
      this.boundBegin = cfg.boundBegin
      this.boundLen = cfg.boundLen
      this.pass = cfg.pass
      this.colour = new Color(cfg.colour.RGB)
    }

    SnakeThread(Bulb[] strip, Phaser phaser, SnakeCfg cfg, Color colour) {
      this(strip, phaser, cfg)
      this.colour = new Color(colour.RGB)
    }

    @Override
    void run() {
      animate(pos, step, direction, tailLen, boundBegin, boundLen, pass, colour)
    }

    private void awaitRender() {
      phaser.arriveAndAwaitAdvance()
    }

    private void finished() {
      phaser.arriveAndDeregister()
    }

    private void animate(int pos, int step, int direction, int tailLen, int boundBegin, int boundLen, int pass,
                         Color colour) {

      long count = 0 // total steps count. this needs only to detect newborn snake and limit it's tail drawing
      int boundEnd = boundBegin + boundLen - 1
      // correct wrong pass
      if (pass <= 0) {
        pass = 1
      }
      // correct wrong start position
      if (pos < boundBegin) {
        pos = boundBegin
      } else if (pos > boundEnd) {
        pos = boundEnd
      }
      // correct wrong direction
      if ((direction > 0 && pos == boundEnd) || (direction <= 0 && pos == boundBegin)) {
        direction *= -1
      }
      while (true) {
        if (pass > 1 && (pos < boundBegin || pos > boundEnd)) {
          if (pos < boundBegin) {
            pos = boundBegin + 1
          } else {
            pos = boundEnd - 1
          }
          direction *= -1
          pass--
        }
        drawSnake(pos, direction, tailLen, step, colour, boundBegin, boundEnd, count++, pass == 1)
        pos += step * direction
        if (pass && (pos >= boundEnd + tailLen + 2 || pos < boundBegin - tailLen - 1)) {
          finished()
          break
        }
        awaitRender()
      }
    }

    private void drawSnake(int pos, int direction, int tailLen, int step, Color color,
                           int boundBegin, int boundEnd, long count, boolean once) {

      boolean newborn = count < tailLen + 1

      // make our tail
      Color tailColor = new Color(color.getRGB())
      Color[] tail = new Color[tailLen + (newborn ? 0 : step)] // newborn: не нужно очищать, мы ещё не наследили
      for (i in 0..(tailLen - 1)) {
        tail[i] = (tailColor = tailColor.darker().darker())
      }
      // erase
      if (!newborn) { // newborn: не нужно очищать, мы ещё не наследили
        for (i in 0..(step - 1)) {
          tail[tailLen + i] = Color.BLACK
        }
      }
      // draw tail
      if (count > 0) { // хвост вообще ещё не нужно рисовать если это самый первый шаг!
        for (i in (count < tailLen ? (int) count : tail.length)..1) { // длина хвоста увеличивается с первыми шагами
          int p = pos - (i * direction)
          // это не последний проход и голова ещё не достигла конца: нужно хвост разворачивать
          if (!once || (direction < 0 && pos >= boundBegin) || (direction > 0 && pos < boundEnd)) {
            // если не newborn и если упираемся в границы хвост разворачивать и вообще рисовать ещё не нужно
            if ((p >= boundBegin && p <= boundEnd) || !newborn) {
              if (p < boundBegin) {
                p *= -1
              } else if (p > boundEnd) {
                p = boundEnd - (p % boundEnd)
              }
              Bulb.set(strip, p, layer, tail[i - 1])
            }
          } else {
            if (p >= boundBegin && p <= boundEnd) {
              Bulb.set(strip, p, layer, tail[i - 1])
            }
          }
        }
      }
      // draw head
      if (pos >= boundBegin && pos <= boundEnd) {
        Bulb.set(strip, pos, layer, color)
      }
    }
  }

  private static class Bulb {

    private TreeMap<Integer, Color> layers

    Bulb() {
      layers = new TreeMap<>()
    }

    synchronized void setColor(int layer, Color color) {
      if (color.equals(Color.BLACK)) {
        layers.remove(layer)
      } else {
        layers.put(layer, color)
      }
    }

    synchronized Color getColor() {
      Map.Entry<Integer, Color> entry = layers.lastEntry()
      if (entry == null) {
        return Color.BLACK
      } else {
        entry.getValue()
      }
    }

    static Bulb get(Bulb[] strip, int pos) {
      Bulb b = strip[pos]
      if (b == null) {
        strip[pos] = (b = new Bulb())
      }
      return b
    }

    static void set(Bulb[] strip, int pos, int layer, Color color) {
      Bulb b = strip[pos]
      if (b == null) {
        strip[pos] = (b = new Bulb())
      }
      b.setColor(layer, color)
    }
  }
}