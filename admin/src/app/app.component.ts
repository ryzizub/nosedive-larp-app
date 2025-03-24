import { HttpClient } from '@angular/common/http';
import { Component, Injectable, inject } from '@angular/core';
import { Auth, signInWithCustomToken } from '@angular/fire/auth';
import { Database, listVal, query, ref, push, serverTimestamp, objectVal, update, remove } from '@angular/fire/database';
import { EMPTY, map } from 'rxjs';
import { Observable } from 'rxjs/internal/Observable';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
@Injectable()
export class AppComponent {
  title = 'admin';
  private database: Database = inject(Database);
  private auth: Auth = inject(Auth);
  npcs: User[] = [];
  players: User[] = [];
  channels: Observable<Channel[]> = EMPTY;
  state = new State(NO_USER, new Channel("", ""), "", NO_USER, NO_USER, NO_USER, 0.05, 0.05, "", NO_USER, "", 0, NO_USER, "", "")

  constructor(private http: HttpClient) {
    const subdomain = new URL(window.location.href).hostname.split('.')[0];
    this.http.get<{ token: string }>('https://europe-west1-nosedive-larp.cloudfunctions.net/login?password=' + subdomain).subscribe(response => {
      signInWithCustomToken(this.auth, response.token).then(() => {
        objectVal(ref(this.database, "config")).subscribe(config => {
          this.state.slackBotToken = (config as Config).slackBotToken
          this.state.feedChannelId = (config as Config).feedChannelId
    
          this.channels = http.post<Channels>("https://slack.com/api/conversations.list?types=public_channel%2C%20private_channel", "token=" + this.state.slackBotToken, { headers: { "Content-Type": "application/x-www-form-urlencoded" } }).pipe(map(channels => channels.channels))
        })
      })
    });
    listVal(query(ref(this.database, "nearbyUsers")), { keyField: "id" }).subscribe(users => {
      if (users != null) {
        users.push(NO_USER)
        let npcUsers = (users as User[]).filter(user => user.id.startsWith("_")).sort((a, b) => a.name.localeCompare(b.name))
        let playerUsers = (users as User[]).filter(user => !user.id.startsWith("_") || LIVE_NPC_IDS.includes(user.id)).sort((a, b) => a.name.localeCompare(b.name))
        if (!this.isSame(npcUsers, this.npcs)) {
          this.npcs = npcUsers
        }
        if (!this.isSame(playerUsers, this.players)) {
          this.players = playerUsers
        }
      }
    })
  }

  onMessageSubmit() {
    this.sendSlackMessage(this.state.user, this.state.channel.id, this.state.text)
  }

  onReportSubmit() {
    push(ref(this.database, "reports"), {
      "reporter1": this.state.reporter1.id,
      "reporter2": this.state.reporter2.id,
      "victim": this.state.victim.id,
      "penalty": this.state.penalty,
      "reward": this.state.reward,
      "reason": this.state.reportReason,
      "createdAt": serverTimestamp()
    })
    let message = (this.state.reporter2 == NO_USER) ?
      "Uživateli " + this.state.victim.name + " bylo sníženo hodnocení o " + this.state.penalty + "\n\nDůvod: " + this.state.reportReason + "\n\nDěkujeme uživateli " + this.state.reporter1.name + " za reportování, za odměnu bylo zvýšeno hodnocení o " + this.state.reward
      :
      "Uživateli " + this.state.victim.name + " bylo sníženo hodnocení o " + this.state.penalty + "\n\nDůvod: " + this.state.reportReason + "\n\nDěkujeme uživatelům " + this.state.reporter1.name + " a " + this.state.reporter2.name + " za reportování, za odměnu jim bylo zvýšeno hodnocení o " + this.state.reward / 2
    this.sendSlackMessage(new User("_dive_safety", "Dive Safety", "https://firebasestorage.googleapis.com/v0/b/nosedive-larp.appspot.com/o/profile_pics%2FDive%20Safety.png?alt=media&token=1003e7ad-28fe-4093-b0f2-6cfc96bd2ee9", undefined), this.state.feedChannelId, message)
  }

  onResetSubmit() {
    if (confirm("Fakt chceš všechno smazat a začít nový běh?")) {
      update(ref(this.database, "config"), {
        "slackBotToken": this.state.slackBotToken,
        "feedChannelId": this.state.feedChannelId
      })
      this.players.forEach(player => {
        if (player.defaultRating != undefined) {
          update(ref(this.database, "nearbyUsers/" + player.id), {
            "totalRating": player.defaultRating,
            "ratingCount": 5000
          })
        }
      })
      this.npcs.forEach(npc => {
        if (npc.defaultRating != undefined) {
          update(ref(this.database, "nearbyUsers/" + npc.id), {
            "totalRating": npc.defaultRating,
            "ratingCount": npc.id == "_karolina" ? 500000 : 5000
          })
        }
      })
      remove(ref(this.database, "reports"))
      remove(ref(this.database, "ratings"))
    }
  }

  onRatingSubmit() {

  }

  onMakeVisible() {
    update(ref(this.database, "nearbyUsers/" + this.state.visibilityUser.id), {
      "isVisible": true
    })
  }

  onMakeInvisible() {
    update(ref(this.database, "nearbyUsers/" + this.state.visibilityUser.id), {
      "isVisible": false
    })
  }

  isSame(first: User[], second: User[]): Boolean {
    return first.length === second.length &&
      first.every((element, index) => element.name === second[index].name && element.profilePictureUrl === second[index].profilePictureUrl);
  }

  sendSlackMessage(user: User, channelId: string, message: string) {
    let url = "https://slack.com/api/chat.postMessage?channel=" + channelId + "&icon_url=" + encodeURIComponent(user.profilePictureUrl) + "&text=" + encodeURIComponent(message) + "&username=" + user.name
    console.log("url=" + url)
    this.http.post(url, "token=" + this.state.slackBotToken, { headers: { "Content-Type": "application/x-www-form-urlencoded" } }).subscribe(response => {
      console.log(JSON.stringify(response))
      this.state.text = ""
    })
  }
}

export class State {

  constructor(
    public user: User,
    public channel: Channel,
    public text: string,
    public reporter1: User,
    public reporter2: User,
    public victim: User,
    public penalty: number,
    public reward: number,
    public reportReason: string,
    public ratingUser: User,
    public ratingReason: string,
    public ratingChange: number,
    public visibilityUser: User,
    public feedChannelId: string,
    public slackBotToken: string
  ) { }

}

export class User {

  constructor(
    public id: string,
    public name: string,
    public profilePictureUrl: string,
    public defaultRating: number | undefined
  ) { }

}

let NO_USER = new User("unknown", "-- Nikdo --", "", undefined)
let LIVE_NPC_IDS = ["_barman", "_david", "_vaclav", "_radka"]

export interface Channels {
  channels: Channel[]
}

export class Channel {

  constructor(
    public name: string,
    public id: string
  ) { }

}

export class Config {

  constructor(
    public feedChannelId: string,
    public slackBotToken: string
  ) { }

}


