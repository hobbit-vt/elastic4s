package com.sksamuel.elastic4s

import com.sksamuel.elastic4s.DefinitionAttributes.{ DefinitionAttributePreference, DefinitionAttributeRefresh }
import org.elasticsearch.action.get.{ MultiGetRequest, MultiGetRequestBuilder, MultiGetResponse }
import org.elasticsearch.client.Client

import scala.concurrent.Future

/** @author Stephen Samuel */
trait MultiGetDsl extends GetDsl {

  def multiget(gets: Iterable[GetDefinition]) = new MultiGetDefinition(gets)
  def multiget(gets: GetDefinition*) = new MultiGetDefinition(gets)

  implicit object MultiGetDefinitionExecutable
      extends Executable[MultiGetDefinition, MultiGetResponse] {
    override def apply(c: Client, t: MultiGetDefinition): Future[MultiGetResponse] = {
      injectFuture(c.multiGet(t.build, _))
    }
  }
}

class MultiGetDefinition(gets: Iterable[GetDefinition])
    extends DefinitionAttributePreference
    with DefinitionAttributeRefresh {

  val _builder = new MultiGetRequestBuilder(ProxyClients.client)
  gets.foreach(get => {
    val item = new MultiGetRequest.Item(get.indexesTypes.index, get.indexesTypes.typ.orNull, get.id)
    item.routing(get.build.routing())
    item.fields(get.build.fields(): _*)
    item.version(get.build.version())
    item.versionType(get.build.versionType())
    item.fetchSourceContext(get.build.fetchSourceContext())

    _builder.add(item)
  })
  def build: MultiGetRequest = _builder.request()

  def realtime(realtime: Boolean): this.type = {
    _builder.setRealtime(realtime)
    this
  }
}
