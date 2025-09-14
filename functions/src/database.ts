import admin = require("firebase-admin");
import { DataSnapshot } from "firebase-functions/lib/v1/providers/database";

export async function doProcessRating(snap: DataSnapshot, runId: string) {
  const rating = snap.val();
  const raterUser = (await admin.database().ref("users/" + runId + "/" + rating.from).once("value")).val()
  // change rating
  const raterRating = raterUser.totalRating
  const weight = raterRating < 1 ? 0 : raterRating < 2 ? 1 : raterRating < 3 ? 2 : raterRating < 4 ? 3 : raterRating < 4.5 ? 4 : 5
  await admin.database().ref("users/" + runId + "/" + rating.to).transaction(
    ratedUser => {
      if (ratedUser == null) return null
      const newRatingCount = ratedUser.ratingCount + weight
      if (rating.stars == undefined) {
        ratedUser.totalRating = ratedUser.totalRating + rating.majorChange
      } else {
        ratedUser.totalRating = (ratedUser.totalRating * ratedUser.ratingCount + rating.stars * weight) / newRatingCount
      }
      ratedUser.ratingCount = newRatingCount
      return ratedUser
    }
  )
  // send notification
  if (rating.stars != undefined) {
    const token = (await admin.database().ref("userSecrets/" + runId + "/" + rating.to + "/notificationsToken").once("value")).val()
    const androidConfig: admin.messaging.AndroidConfig = {
      priority: 'high'
    }
    const message = {
      data: {
        fromNameGenitiv: raterUser.nameGenitiv,
        stars: String(rating.stars)
      },
      android: androidConfig,
      token: token
    };
    console.log("message=" + JSON.stringify(message))
    await admin.messaging().send(message)
  }
}

export async function doProcessPostRating(snap: DataSnapshot, runId: string, postId: string) {
  const rating = snap.val();
  const post = (await admin.database().ref("posts/" + runId + "/" + postId).once("value")).val()
  await admin.database().ref("ratings/" + runId).push().set({
    "from": rating.from,
    "to": post.author,
    "createdAt": rating.createdAt,
    "stars": rating.stars
  })
}

export async function doProcessReport(snap: DataSnapshot, runId: string) {
  const report = snap.val();
  await admin.database().ref("users/" + runId + "/" + report.victim).transaction(
    victimUser => {
      if (victimUser == null) return null
      victimUser.totalRating = victimUser.totalRating - report.penalty
      return victimUser
    }
  )
  if (report.reporter2 == "unknown") {
    await admin.database().ref("users/" + runId + "/" + report.reporter1).transaction(
      user => {
        if (user == null) return null
        user.totalRating = user.totalRating + report.reward
        return user
      }
    )
  } else {
    await admin.database().ref("users/" + runId + "/" + report.reporter1).transaction(
      user => {
        if (user == null) return null
        user.totalRating = user.totalRating + report.reward / 2
        return user
      }
    )
    await admin.database().ref("users/" + runId + "/" + report.reporter2).transaction(
      user => {
        if (user == null) return null
        user.totalRating = user.totalRating + report.reward / 2
        return user
      }
    )
  }
}

export async function doProcessChatMessage(snap: DataSnapshot, runId: string, conversationId: string) {
  const chatMessage = snap.val();
  const author = (await admin.database().ref("users/" + runId + "/" + chatMessage.author).once("value")).val()
  const conversationUsers = (await admin.database().ref("conversationUsers/" + runId + "/" + conversationId).once("value")).val()
  const otherUserId = Object.keys(conversationUsers).find((userId: string) => userId != chatMessage.author)
  // send notification
  const token = (await admin.database().ref("userSecrets/" + runId + "/" + otherUserId + "/notificationsToken").once("value")).val()
  const androidConfig: admin.messaging.AndroidConfig = {
    priority: 'high'
  }
  const message = {
    data: {
      authorName: author.name,
      authorPictureUrl: author.profilePictureUrl,
      messageText: chatMessage.text,
      attachmentUrl: String(chatMessage.attachmentUrl),
      conversationId: conversationId
    },
    android: androidConfig,
    token: token
  };
  console.log("message=" + JSON.stringify(message))
  await admin.messaging().send(message)
}

export async function doProcessComment(snap: DataSnapshot, runId: string, postId: string) {
  const comment = snap.val();
  const author = (await admin.database().ref("users/" + runId + "/" + comment.author).once("value")).val()
  const postAuthor = (await admin.database().ref("posts/" + runId + "/" + postId + "/author").once("value")).val()
  // send notification
  const token = (await admin.database().ref("userSecrets/" + runId + "/" + postAuthor + "/notificationsToken").once("value")).val()
  const androidConfig: admin.messaging.AndroidConfig = {
    priority: 'high'
  }
  const message = {
    data: {
      authorName: author.name,
      authorPictureUrl: author.profilePictureUrl,
      messageText: comment.text,
      attachmentUrl: String(comment.attachmentUrl),
      postId: postId
    },
    android: androidConfig,
    token: token
  };
  console.log("message=" + JSON.stringify(message))
  await admin.messaging().send(message)
}

export async function doProcessPost(snap: DataSnapshot, runId: string, postId: string) {
  const post = snap.val();
  if (post.important == true) {
    const author = (await admin.database().ref("users/" + runId + "/" + post.author).once("value")).val()
    // send notifications to all
    let tokens: string[] = new Array<string>
    await (admin.database().ref("userSecrets/" + runId).once("value", (snap) => {
      console.log("snap=" + JSON.stringify(snap))
      const value = snap.val()
      console.log("val=" + JSON.stringify(value))
      tokens = Object.values(value).map((v: any) => v.notificationsToken).filter((t: any) => t != undefined)
      console.log("tokens=" + JSON.stringify(tokens))
    }))
    const androidConfig: admin.messaging.AndroidConfig = {
      priority: 'high'
    }
    const message = {
      data: {
        authorName: author.name,
        authorPictureUrl: author.profilePictureUrl,
        postText: post.text,
        postId: postId
      },
      android: androidConfig,
      tokens: tokens
    };
    console.log("message=" + JSON.stringify(message))
    await admin.messaging().sendMulticast(message)
  }
}