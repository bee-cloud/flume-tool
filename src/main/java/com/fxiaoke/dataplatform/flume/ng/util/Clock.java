/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fxiaoke.dataplatform.flume.ng.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


abstract public class Clock {
  private static Clock clock = new DefaultClock();

  private final static DateFormat dateFormat = new SimpleDateFormat(
      "yyyyMMdd-HHmmssSSSZ");

  public static String timeStamp() {
    synchronized(dateFormat) {
      return dateFormat.format(date());
    }
  }

  static class DefaultClock extends Clock {

    @Override
    public long getNanos() {
      return System.nanoTime();
    }

    @Override
    public long getUnixTime() {
      return System.currentTimeMillis();
    }

    @Override
    public Date getDate() {
      return new Date();
    }

    @Override
    public void doSleep(long millis) throws InterruptedException {
      Thread.sleep(millis);
    }

  };

  public static void resetDefault() {
    clock = new DefaultClock();
  }

  public static void setClock(Clock c) {
    clock = c;
  }

  public static long unixTime() {
    return clock.getUnixTime();
  }

  public static long nanos() {
    return clock.getNanos();
  }

  public static Date date() {
    return clock.getDate();
  }

  public static void sleep(long millis) throws InterruptedException {
    clock.doSleep(millis);
  }

  public abstract long getUnixTime();

  public abstract long getNanos();

  public abstract Date getDate();

  abstract public void doSleep(long millis) throws InterruptedException;
}
