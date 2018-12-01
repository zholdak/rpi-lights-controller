package com.zholdak.rbpi.lightscontroller.utils;

import java.util.Iterator;

/**
 * This class iterates across an iterable object with the option to recycle amongst the elements infinitely.
 * In case the recycle option is not set, the original data is iterated upon once, and all subsequent calls to
 * next() will return the defaultDataValue
 *
 * @param <T>
 */
public class CyclicIterator<T> implements Iterator<T> {

  final private Iterable<T> data;
  private Iterator<T> dataIterator;

  public CyclicIterator(Iterable<T> data) {
    this.data = data;
    resetDataIterator();
  }

  private void resetDataIterator() {
    this.dataIterator = this.data.iterator();
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public T next() {
    if (!dataIterator.hasNext()) {
      resetDataIterator();
    }
    return dataIterator.next();
  }

  @Override
  public void remove() {
    ;
  }
}