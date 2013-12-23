package com.lucho

import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import spray.http._
import spray.json._
import spray.client.pipelining._
import akka.actor.{ActorRef, Actor}
import spray.http.HttpRequest
import scala.Some
import scala.io.Source
import scala.util.Try
import spray.can.Http
import akka.io.IO
import rx.lang.scala.Observer
import com.lucho.ToObservableActor.UnSuscribe

trait TwitterAuthorization {
  def authorize: HttpRequest => HttpRequest
}

trait OAuthTwitterAuthorization extends TwitterAuthorization {
  import OAuth._
  val home = System.getProperty("user.home")
  val lines = Source.fromFile(s"$home/.twitter/rxspray").getLines().toList

  val consumer = Consumer(lines(0), lines(1))
  val token = Token(lines(2), lines(3))

  val authorize: (HttpRequest) => HttpRequest = oAuthAuthorizer(consumer, token)
}

object TweetMarshaller {

  implicit object TweetUnmarshaller extends Unmarshaller[Tweet] {

    def mkUser(user: JsObject): Deserialized[User] = {
      (user.fields("id_str"), user.fields("lang"), user.fields("followers_count")) match {
        case (JsString(id), JsString(lang), JsNumber(followers)) => Right(User(id, lang, followers.toInt))
        case (JsString(id), _, _)                                => Right(User(id, "", 0))
        case _                                                   => Left(MalformedContent("bad user"))
      }
    }

    def mkPlace(place: JsValue): Deserialized[Option[Place]] = place match {
      case JsObject(fields) =>
        (fields.get("country"), fields.get("name")) match {
          case (Some(JsString(country)), Some(JsString(name))) => Right(Some(Place(country, name)))
          case _                                               => Left(MalformedContent("bad place"))
        }
      case JsNull => Right(None)
      case _ => Left(MalformedContent("bad tweet"))
    }

    def apply(entity: HttpEntity): Deserialized[Tweet] = {
      Try {
        val json = JsonParser(entity.asString).asJsObject
        (json.fields.get("id_str"), json.fields.get("text"), json.fields.get("place"), json.fields.get("user")) match {
          case (Some(JsString(id)), Some(JsString(text)), Some(place), Some(user: JsObject)) =>
            val x = mkUser(user).fold(x => Left(x), { user =>
              mkPlace(place).fold(x => Left(x), { place =>
                Right(Tweet(id, user, text, place))
              })
            })
            x
          case _ => Left(MalformedContent("bad tweet"))
        }
      }
    }.getOrElse(Left(MalformedContent("bad json")))
  }
}

object TweetStreamerActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
}

class TweetStreamerActor(uri: Uri, processor: ActorRef) extends Actor {
  this: TwitterAuthorization =>
  val io = IO(Http)(context.system)

  def receive: Receive = {
    case query: String =>
      val body = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"track=$query")
      val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = body) ~> authorize
      sendTo(io).withResponsesReceivedBy(processor)(rq)
  }
}

class ToObservableActor(observer: Observer[HttpEntity]) extends Actor {

  val ignoreMessages: Receive = PartialFunction.empty

  def receive: Receive = {
    case UnSuscribe => context.become(ignoreMessages)
    case ChunkedResponseStart(_) =>
    case MessageChunk(entity, _) => observer.onNext(entity)
    case ChunkedMessageEnd => observer.onCompleted()
  }
}
 /*
 TweetUnmarshaller(entity) match {
      case Right(value) => observer.onNext(value)
      //case Left(MalformedContent(message, Some(cause))) => observer.onError(new Exception(message, cause))
      //case Left(MalformedContent(message, None)) => observer.onError(new Exception(message))
      case _ =>
    }
  */

object ToObservableActor {

  case object UnSuscribe

}
