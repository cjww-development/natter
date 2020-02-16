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

package database.firestore

import java.io.InputStream

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.{DocumentSnapshot, Firestore, QueryDocumentSnapshot}
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import javax.inject.{Inject, Named}
import play.api.Logger

class Client @Inject()(@Named("firestoreCreds") serviceAccount: InputStream) {

  private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
  private val options: FirebaseOptions = new FirebaseOptions.Builder()
    .setCredentials(credentials)
    .build()

  FirebaseApp.initializeApp(options)

  def getDb: Firestore = {
    FirestoreClient.getFirestore()
  }

  def documentExists(field: String, value: String): Boolean = {
    !getDb.collection("rooms").whereEqualTo(field, value).get().get().isEmpty
  }

  def listenToRoom[T](code: String)(f: Either[QueryDocumentSnapshot, String] => T): Unit = {
    getDb.collection("rooms").whereEqualTo("code", code).addSnapshotListener((querySnap, _) => {
      if(querySnap.size() == 1) {
        f(Left(querySnap.getDocuments.get(0)))
        return
      } else {
        Logger("ListToRoom").info("No Matching room found")
        f(Right("No matching room"))
        return
      }
    })
  }


  def listenToDoc[T](id: String)(f: DocumentSnapshot => T): Unit = {
    val docRef = getDb.collection("rooms").document(id)
    docRef.addSnapshotListener((snapshot, _) => {
      f(snapshot)
      return
    })
  }
}
