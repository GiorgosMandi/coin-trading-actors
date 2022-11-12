package gr.gm.industry

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.scaladsl.Sink
import com.typesafe.config.{Config, ConfigFactory}
import gr.gm.industry.graphs.PriceFlow
import gr.gm.industry.graphs.actors.SimpleTrader
import gr.gm.industry.listeners.BinanceListener

object Main extends App {

    val conf: Config = ConfigFactory.load()
    implicit val system: ActorSystem = ActorSystem("CryptoTrader", conf)
    val priceSource = BinanceListener(parallelism = 5, throttle = (2, 1))
    val priceFlow = PriceFlow.priceFlow
    val trader: ActorRef = system.actorOf(Props[SimpleTrader], "SimpleTrader")


    priceSource.apply
      .via(priceFlow)
      .to(Sink.foreach { case (tradeAction, priceDao) => trader ! (tradeAction, priceDao.price) })
      .run()
}
