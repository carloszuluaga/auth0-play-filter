package co.s4n.play.filters

import play.api.Logger
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.auth0.jwt.JWTVerifier
import org.apache.commons.codec.binary.Base64
import scala.util.Try
import com.typesafe.config.ConfigFactory

class Auth0Filter extends Filter {

  lazy val config = ConfigFactory.load()
  lazy val clientId = config.getString("auth0.clientId")
  lazy val clientSecret = config.getString("auth0.clientSecret")
  lazy val jwtVerifier = new JWTVerifier(new Base64(true).decode(clientSecret), clientId)

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    val godMode = config.getBoolean("auth0.godmode")
    if(godMode){
      Logger.warn("God mode is activated!. This is not recommended for production environments.")
      nextFilter(requestHeader)
    }else if(requestHeader.method == "OPTIONS" ){
      nextFilter(requestHeader)
    }else{
      val tokenEither = getToken(requestHeader)
      tokenEither match {
        case Right(token) =>
          val tokenTry = Try{
            jwtVerifier.verify(token)
            nextFilter(requestHeader)
          }.recover{
            case e => Future(Unauthorized(e.getMessage))
          }
          tokenTry.get
        case Left(error) =>
          Future(Unauthorized(error))
      }
    }
  }

  private def getToken(requestHeader: RequestHeader): Either[String,String] = {
    val authorizationHeaderOpt = requestHeader.headers.get("authorization")
    authorizationHeaderOpt.fold[Either[String, String]](Left("Unauthorized: No Authorization header was found")){authorizationHeader =>
      val parts = authorizationHeader.split(" ")
      if(parts.size != 2){
        Left("Unauthorized: Format is Authorization: Bearer [token]")
      }else{
        val scheme = parts(0)
        val credentials = parts(1)
        scheme match {
          case "Bearer" => Right(credentials)
          case _ => Left("Unauthorized: Format is Authorization: Bearer [token]")
        }
      }
    }
  }
}
