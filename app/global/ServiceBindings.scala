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

package global

import java.io.{FileInputStream, InputStream}

import controllers.RoomController
import database.firestore.Client
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

class ServiceBindings extends Module {

  override def bindings(environment: Environment, configuration: Configuration): collection.Seq[Binding[_]] = Seq(
    bind[InputStream].qualifiedWith("firestoreCreds").to(new FileInputStream(configuration.get[String]("database.firestore.credPath"))).eagerly(),
    bind[Client].toSelf.eagerly(),
    bind[RoomController].toSelf.eagerly()
  )
}
