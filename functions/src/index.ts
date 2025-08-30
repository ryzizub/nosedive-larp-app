import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { doLogin } from './login';
import { doProcessRating, doProcessReport, doProcessPostRating, doProcessChatMessage } from './database';

admin.initializeApp({
    databaseURL: "https://nosedive-larp-default-rtdb.europe-west1.firebasedatabase.app"
});

export let login = functions.region('europe-west1').https.onRequest(async (request, response) => {
    await doLogin(request.query["run"] as string, request.query["password"] as string, response)
})

export let processRating = functions.region('europe-west1').database.ref("ratings/{runId}/{ratingId}").onCreate(async (snap, context) => {
    await doProcessRating(snap, context.params.runId)
})

export let processPostRating = functions.region('europe-west1').database.ref("postRatings/{runId}/{postId}/{ratingId}").onCreate(async (snap, context) => {
    await doProcessPostRating(snap, context.params.runId, context.params.postId)
})

export let processReport = functions.region('europe-west1').database.ref("reports/{runId}/{reportId}").onCreate(async (snap, context) => {
    await doProcessReport(snap, context.params.runId)
})

export let processChatMessage = functions.region('europe-west1').database.ref("conversationMessages/{runId}/{conversationId}/{messageId}").onCreate(async (snap, context) => {
    await doProcessChatMessage(snap, context.params.runId, context.params.conversationId)
})