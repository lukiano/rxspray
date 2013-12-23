package com.lucho

import akka.actor.{Props, ActorSystem}
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject
import spray.http.HttpEntity


object Main extends App {

  val system = ActorSystem("rxspray")
  val subject = ReplaySubject[HttpEntity]()
  val observable: Observable[HttpEntity] = subject
  val x:Observable[Tweet] = observable map { entity =>
    TweetMarshaller.TweetUnmarshaller(entity) match {
      case Right(value) => value
      //case Left(MalformedContent(message, Some(cause))) => observer.onError(new Exception(message, cause))
      //case Left(MalformedContent(message, None)) => observer.onError(new Exception(message))
      case _ => throw new Exception
    }
  }

  val processor = system.actorOf(Props[ToObservableActor](new ToObservableActor(subject)))
  val streamer = system.actorOf(Props[TweetStreamerActor](new TweetStreamerActor(TweetStreamerActor.twitterUri, processor) with OAuthTwitterAuthorization))

  x subscribe { tweet => println(tweet) }
  Thread.sleep(10000)
  system.shutdown()
}
