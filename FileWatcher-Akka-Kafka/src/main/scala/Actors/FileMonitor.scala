package org.cs441.proj
package Actors

import java.nio.file.{Path, WatchEvent}

trait FileMonitor {
  val root: Path

  def start(): Unit

  def onCreate(path: Path) = {}

  def onModify(path: Path) = {}

  def onDelete(path: Path) = {}

  def onUnknownEvent(event: WatchEvent[_]) = {}

  def onException(e: Throwable) = {}

  def stop(): Unit
}
