package io.github.copperlight.skeleton

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.lang.reflect.ParameterizedType

object Json {

  val mapper: ObjectMapper = {
    JsonMapper
      .builder()
      .addModule(DefaultScalaModule)
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build()
  }

  def encode[T: Manifest](obj: T): Array[Byte] = {
    mapper.writeValueAsBytes(obj)
  }

  def decode[T: Manifest](bytes: Array[Byte]): T = {
    val reader: ObjectReader = {
      if (manifest.runtimeClass.isArray)
        mapper.readerFor(manifest.runtimeClass.asInstanceOf[Class[T]])
      else
        mapper.readerFor(typeReference[T])
    }

    reader.readValue[T](bytes)
  }

  type JType = java.lang.reflect.Type

  // Taken from com.fasterxml.jackson.module.scala.deser.DeserializerTest.scala
  def typeReference[T: Manifest]: TypeReference[T] = new TypeReference[T] {
    override def getType: JType = typeFromManifest(manifest[T])
  }

  // Taken from com.fasterxml.jackson.module.scala.deser.DeserializerTest.scala
  private def typeFromManifest(m: Manifest[_]): java.lang.reflect.Type = {
    if (m.typeArguments.isEmpty) {
      m.runtimeClass
    } else {
      new ParameterizedType {
        def getRawType: Class[_] = m.runtimeClass
        def getActualTypeArguments: Array[JType] = m.typeArguments.map(typeFromManifest).toArray
        def getOwnerType: JType = null
      }
    }
  }
}
