/*
 * Copyright 2020 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import database.firestore.Client
import javax.inject.Inject
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

import scala.concurrent.Future

class RoomController @Inject()(cc: ControllerComponents, firestore: Client)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def socket = WebSocket.acceptOrResult[String, String] { req =>
    Future.successful(req.getQueryString("room") match {
      case Some(room) => Right(
        ActorFlow.actorRef { out =>
          MyWebSocketActor.props(out, room)
        }
      )
      case None => Left(NotFound)
    })
  }

  object MyWebSocketActor {
    def props(out: ActorRef, room: String): Props = Props(new MyWebSocketActor(out, room))
  }

  class MyWebSocketActor(out: ActorRef, room: String) extends Actor {
    firestore.listenToRoom(room) {
      case Left(data) => out ! (s"Room: ${data.get("code")} People:" + data.get("people"))
      case Right(_) =>
        Logger("Poisoned").info("Sent poisoned pill")
        out ! PoisonPill
    }

    override def receive: Receive = {
      case x: String => out ! x
    }

    override def postStop(): Unit = {
      super.postStop()
      Logger("Disconnect").info(s"Connection with client requesting room ${room} closed")
    }
  }
}
