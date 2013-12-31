Proof of concept that converts Spray's chunks to Observable events. Right now it reads the chunks from Twitter's stream.

Special thanks to Jan Machacek's [blog](http://www.cakesolutions.net/teamblogs/2013/12/08/streaming-twitter-api-in-akka-and-spray/) and [code](https://github.com/eigengo/activator-spray-twitter)

TODO

- Subscribing an Observer should really use an RxJava scheduler. Right now the log is showing that it's using the threads of the Akka scheduler.

- Check out if [rxjava-akka](https://github.com/jmhofer/rxjava-akka) may be useful.