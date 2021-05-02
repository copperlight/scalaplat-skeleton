import java.io.File
import java.io.PrintStream
import java.util.Calendar
import java.util.TimeZone
import scala.io.Source
import sbt._

object License {
  private val lineSeparator = System.getProperty("line.separator")

  def year: Int = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)

  val apache2: String =
    s"""
       |/*
       | * Copyright 2020-$year Matthew Johnson
       | *
       | * Licensed under the Apache License, Version 2.0 (the "License");
       | * you may not use this file except in compliance with the License.
       | * You may obtain a copy of the License at
       | *
       | *     http://www.apache.org/licenses/LICENSE-2.0
       | *
       | * Unless required by applicable law or agreed to in writing, software
       | * distributed under the License is distributed on an "AS IS" BASIS,
       | * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       | * See the License for the specific language governing permissions and
       | * limitations under the License.
       | */
  """.stripMargin.trim

  def findFiles(dir: File): Seq[File] = {
    (dir ** "*.scala").get ++ (dir ** "*.java").get
  }

  def getLines(file: File): List[String] = {
    val source = Source.fromFile(file, "UTF-8")
    val lines = source.getLines().toList
    source.close()
    lines
  }

  def checkLicenseHeaders(log: Logger, srcDir: File): Unit = {
    val badFiles = findFiles(srcDir).filterNot(checkLicenseHeader)

    if (badFiles.nonEmpty) {
      badFiles.foreach { f => log.error(s"bad license header: $f") }
      sys.error(s"${badFiles.size} files with incorrect header, run formatLicenseHeaders to fix")
    } else {
      log.info("all files have correct license header")
    }
  }

  def checkLicenseHeader(file: File): Boolean = {
    val lines = getLines(file)
    checkLicenseHeader(lines)
  }

  def checkLicenseHeader(lines: List[String]): Boolean = {
    val header = lines.takeWhile(!_.startsWith("package ")).mkString(lineSeparator)
    header == apache2
  }

  def formatLicenseHeaders(log: Logger, srcDir: File): Unit = {
    findFiles(srcDir).foreach { f => formatLicenseHeader(log, f) }
  }

  def formatLicenseHeader(log: Logger, file: File): Unit = {
    val lines = getLines(file)

    if (!checkLicenseHeader(lines)) {
      log.info(s"fixing license header: $file")
      writeLines(file, apache2 :: removeExistingHeader(lines))
    }
  }

  def removeExistingHeader(lines: List[String]): List[String] = {
    val res = lines.dropWhile(!_.startsWith("package "))
    if (res.isEmpty) lines else res
  }

  def writeLines(file: File, lines: List[String]): Unit = {
    val out = new PrintStream(file)
    try lines.foreach(out.println) finally out.close()
  }
}
