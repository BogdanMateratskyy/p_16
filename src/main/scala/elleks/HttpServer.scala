package elleks

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import elleks.networkEntity.Developer
import play.api.libs.json.Json
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import scala.concurrent.{ExecutionContext, Future}

object HttpServer extends App{

  val host = "0.0.0.0"
  val port = 9000

  implicit val formats: DefaultFormats.type = DefaultFormats
  implicit val system: ActorSystem = ActorSystem("httpServer")
  implicit val executor: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  var devInfo = scala.collection.mutable.Map[Int, Developer]()
  //var count = 0

  devInfo += (1 -> Developer("Bogdan", "software engineer"))

  def route = path("user"){
    pathEndOrSingleSlash{
      get {
        complete(write(devInfo))
      } ~
        post {
          entity(as[HttpEntity]) { entity => {
            val responseString: Future[String] = Unmarshal(entity).to[String]
            responseString onSuccess {
              case msg =>

                val json = Json.parse(msg)
                val id = json("id").validate[Int].get
                val devName = json("devName").validate[String].get
                val description = json("description").validate[String].get

                devInfo += (id -> Developer(devName, description))
            }
            complete(StatusCodes.Created)
          }
          }
        }
    } //~

  }
  Http().bindAndHandle(route, host, port)
  println(s"Your server is running here: $host $port")
}

