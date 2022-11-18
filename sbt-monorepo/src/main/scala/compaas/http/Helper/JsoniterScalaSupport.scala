package compaas.http.Helper

// "de.heikoseeberger" %% "akka-http-jsoniter-scala" % "1.39.2"
// NOTE: ask heikoseeberger to add cross build for Scala 3
// cross(for3Use2_13) won't work, as scala 2 macro not compatible with scala 3

import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.*
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{
  ContentType,
  ContentTypeRange,
  HttpEntity,
  MediaType,
  MessageEntity
}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.github.plokhotnyuk.jsoniter_scala.core.*

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/** Automatic to and from JSON marshalling/unmarshalling using an in-scope instance of
  * JsonValueCodec
  */
object JsoniterScalaSupport extends JsoniterScalaSupport:
  val defaultReaderConfig: ReaderConfig =
    ReaderConfig.withPreferredBufSize(100 * 1024).withPreferredCharBufSize(10 * 1024)
  val defaultWriterConfig: WriterConfig = WriterConfig.withPreferredBufSize(100 * 1024)

/** JSON marshalling/unmarshalling using an in-scope instance of JsonValueCodec
  */
trait JsoniterScalaSupport:
  type SourceOf[A] = Source[A, ?]

  import JsoniterScalaSupport.*

  private val defaultMediaTypes: Seq[MediaType.WithFixedCharset] = List(`application/json`)
  private val defaultContentTypes: Seq[ContentTypeRange] =
    defaultMediaTypes.map(ContentTypeRange.apply)
  private val byteArrayUnmarshaller: FromEntityUnmarshaller[Array[Byte]] =
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(unmarshallerContentTypes*)

  private def sourceByteStringMarshaller(
      mediaType: MediaType.WithFixedCharset
  ): Marshaller[SourceOf[ByteString], MessageEntity] =
    Marshaller[SourceOf[ByteString], MessageEntity] { implicit ec => value =>
      try
        FastFuture.successful {
          Marshalling.WithFixedContentType(
            mediaType,
            () => HttpEntity(contentType = mediaType, data = value)
          ) :: Nil
        }
      catch
        case NonFatal(e) => FastFuture.failed(e)
    }

  private val jsonSourceStringMarshaller =
    Marshaller.oneOf(mediaTypes*)(sourceByteStringMarshaller)

  private def jsonSource[A](entitySource: SourceOf[A])(implicit
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig,
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(writeToArray(_, config))
      .map(ByteString(_))
      .via(support.framingRenderer)

  def unmarshallerContentTypes: Seq[ContentTypeRange] = defaultContentTypes

  def mediaTypes: Seq[MediaType.WithFixedCharset] = defaultMediaTypes

  /** HTTP entity => `A`
    */
  implicit def unmarshaller[A](implicit
      codec: JsonValueCodec[A],
      config: ReaderConfig = defaultReaderConfig
  ): FromEntityUnmarshaller[A] =
    byteArrayUnmarshaller.map { bytes =>
      if bytes.length == 0 then throw Unmarshaller.NoContentException
      readFromArray[A](bytes, config)
    }

  /** `A` => HTTP entity
    */
  implicit def marshaller[A](implicit
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig
  ): ToEntityMarshaller[A] =
    val mediaType   = mediaTypes.head
    val contentType = ContentType.WithFixedCharset(mediaType)
    Marshaller.withFixedContentType(contentType) { obj =>
      HttpEntity.Strict(contentType, ByteString.fromArrayUnsafe(writeToArray(obj, config)))
    }

  /** `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A](implicit
      codec: JsonValueCodec[A],
      config: ReaderConfig = defaultReaderConfig
  ): Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.fromTry(Try(readFromArray(bs.toArray, config))))

  /** HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: JsonValueCodec](implicit
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json(),
      config: ReaderConfig = defaultReaderConfig
  ): FromEntityUnmarshaller[SourceOf[A]] =
    Unmarshaller
      .withMaterializer[HttpEntity, SourceOf[A]] { implicit ec => implicit mat => entity =>
        def asyncParse(bs: ByteString) =
          Unmarshal(bs).to[A]

        def ordered =
          Flow[ByteString].mapAsync(support.parallelism)(asyncParse)

        def unordered =
          Flow[ByteString].mapAsyncUnordered(support.parallelism)(asyncParse)

        Future.successful {
          entity.dataBytes
            .via(support.framingDecoder)
            .via(if support.unordered then unordered else ordered)
        }
      }
      .forContentTypes(unmarshallerContentTypes*)

  /** `SourceOf[A]` => HTTP entity
    *
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `SourceOf[A]` value
    */
  implicit def sourceMarshaller[A](implicit
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
