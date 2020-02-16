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
import com.google.cloud.firestore.{DocumentSnapshot, EventListener, Firestore, FirestoreException}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.firebase.cloud.FirestoreClient
import javax.inject.{Inject, Named}

class Client @Inject()(@Named("firestoreCreds") serviceAccount: InputStream) {

  private val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
  private val options: FirebaseOptions = new FirebaseOptions.Builder()
    .setCredentials(credentials)
    .build()

  FirebaseApp.initializeApp(options)

  def getDb: Firestore = {
    FirestoreClient.getFirestore()
  }

  def getDocument(id: String): Unit = {
    val docRef = getDb.collection("rooms").document(id)
    val future = docRef.get()
    val doc = future.get()
    if (doc.exists) println("Document data: " + doc.getData)
    else println("No such document!")
  }

  def listenToDoc(id: String): Unit = {
    val docRef = getDb.collection("rooms").document(id)
    docRef.addSnapshotListener((snapshot, e) => {
      if (e != null) {
        println("Listen failed: " + e)
        return
      }

      if (snapshot != null && snapshot.exists) println("Current data: " + snapshot.getData)
      else println("Current data: null")
    })
  }
}
