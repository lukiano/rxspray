package com.lucho

import akka.actor.{Props, ActorSystem}
import rx.lang.scala.{Observer, Observable}
import rx.lang.scala.subjects.ReplaySubject
import spray.http.HttpEntity
import org.slf4j.LoggerFactory
import Thread.sleep
import rx.lang.scala.concurrency.Schedulers

object Main extends App {

  val log = LoggerFactory.getLogger("Main")

  val system = ActorSystem("rxspray")
  val subject = ReplaySubject[HttpEntity]()
  val observable: Observable[HttpEntity] = subject
  val x:Observable[Tweet] = observable map { entity =>
    log.debug("Mapping")
    TweetMarshaller.TweetUnmarshaller(entity) match {
      case Right(value) => value
      //case Left(MalformedContent(message, Some(cause))) => observer.onError(new Exception(message, cause))
      //case Left(MalformedContent(message, None)) => observer.onError(new Exception(message))
      case _ => throw new Exception
    }
  }

  val processor = system.actorOf(Props[ToObservableActor](new ToObservableActor(subject)))
  val streamer = system.actorOf(Props[TweetStreamerActor](new TweetStreamerActor(TweetStreamerActor.twitterUri, processor) with OAuthTwitterAuthorization))

  val scheduler = Schedulers.threadPoolForIO

  streamer ! "obama"


  val observer = new rx.Observer[Tweet] {
    def onNext(tweet: Tweet) = log.info(s"${tweet.user.name}: ${tweet.text}")

    def onError(e: Throwable) = log.error("An error occurred", e)

    def onCompleted() = {}
  }

  //x.subscribe { tweet => log.info(s"${tweet.user.name}: ${tweet.text}") }
  x.subscribe(Observer(observer), scheduler)

  sleep(20000)
  system.shutdown()
  /*
  sys.addShutdownHook {
    log.warn("Shutting down ...")
    system.shutdown()
  }
  */
}
