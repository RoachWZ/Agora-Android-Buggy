// Created by wangzheng  2022-08-29
//todo ble蓝牙遥控
package org.openbot.vehicle;

import android.content.Context;

import org.openbot.env.Logger;

public class BleConnection {
  private static final Logger LOGGER = new Logger();


  private final Context context;


  public BleConnection(Context context, int baudRate) {
    this.context = context;
    }


}
