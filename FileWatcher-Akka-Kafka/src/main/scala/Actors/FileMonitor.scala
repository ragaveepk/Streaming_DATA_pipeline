package org.cs441.proj
package Actors

import java.nio.file.{Path, WatchEvent}

/**
 * Trait [[FileMonitor]]
 * The methods defined in this trait will be
 * implemented by [[ThreadFileMonitor]]
 * */
trait FileMonitor {
  val root: Path
  def start(): Unit
  def onCreate(path: Path): Unit = {}
  def onModify(path: Path): Unit = {}
  def onDelete(path: Path): Unit = {}
  def onUnknownEvent(event: WatchEvent[_]): Unit = {}
  def onException(e: Throwable): Unit = {}
  def stop()
}
