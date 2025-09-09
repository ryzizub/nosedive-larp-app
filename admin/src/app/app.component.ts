import { HttpClient } from '@angular/common/http';
import { Component, Injectable, inject } from '@angular/core';
import { Auth, signInWithCustomToken } from '@angular/fire/auth';
import { Database, listVal, query, ref, push, serverTimestamp, objectVal, update, remove } from '@angular/fire/database';
import { getDownloadURL, Storage, ref as storageRef, uploadBytes } from '@angular/fire/storage';
import { firstValueFrom } from 'rxjs';

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
  private storage: Storage = inject(Storage)
  npcs: User[] = [];
  players: User[] = [];
  runs: Run[] = [];
  runId: string | null = null
  conversationId: string | null = null
  state = new State()
  chatMessages: any[] = [];

  constructor(private http: HttpClient) {
    const url = new URL(window.location.href)
    const subdomain = url.hostname.split('.')[0];
    this.runId = url.searchParams.get("run")
    this.http.get<{ token: string }>(`https://europe-west1-nosedive-larp.cloudfunctions.net/login?password=${subdomain}&run=${this.runId}`).subscribe(response => {
      signInWithCustomToken(this.auth, response.token).then(() => {
      })
    });
    listVal(query(ref(this.database, "users/" + this.runId)), { keyField: "id" }).subscribe(users => {
      if (users != null) {
        let npcUsers = (users as User[]).filter(user => user.id.startsWith("_")).sort((a, b) => a.name.localeCompare(b.name))
        let playerUsers = (users as User[]).filter(user => !user.id.startsWith("_") || LIVE_NPC_IDS.includes(user.id)).sort((a, b) => a.name.localeCompare(b.name))
        npcUsers.push(NO_USER)
        playerUsers.push(NO_USER)
        if (!this.isSame(npcUsers, this.npcs)) {
          this.npcs = npcUsers
        }
        if (!this.isSame(playerUsers, this.players)) {
          this.players = playerUsers
        }
      }
    })
    listVal(query(ref(this.database, "runs")), { keyField: "id" }).subscribe(runs => {
      if (runs != null) {
        this.runs = runs as Run[]
      }
    })
  }

  onConversationSubmit() {
    const runId = this.runId;
    const fromId = this.state.chatFrom?.id;
    const toId = this.state.chatTo?.id;
    if (!runId || !fromId || !toId || fromId === "unknown" || toId === "unknown") {
      this.conversationId = null;
      return;
    }
    firstValueFrom(objectVal(ref(this.database, `userConversations/${runId}/${fromId}`))).then(async (convs) => {
      if (!convs) {
        this.conversationId = null;
        return;
      }
      let found = false
      for (const convId of Object.keys(convs)) {
        const exists = await firstValueFrom(objectVal(ref(this.database, `conversationUsers/${runId}/${convId}/${toId}`)));
        if (exists) {
          this.conversationId = convId;
          found = true
          // Subscribe to chat messages for this conversation
          const messagesRef = ref(this.database, `conversationMessages/${runId}/${convId}`);
          listVal(messagesRef, { keyField: 'id' }).subscribe((msgs: any[] | null) => {
            this.chatMessages = msgs?.slice(-10) || [];
          });
          return;
        }
      }
      if (!found) {
        this.conversationId = null;
      }
    });
  }

  onChatAttachmentChange(event: any) {
    const file = event.target.files[0];
    this.state.chatAttachment = file ? file : null;
  }

  async onMessageSubmit() {
    this.state.chatUploading = true
    let downloadUrl = null
    if (this.state.chatAttachment) {
      const filePath = `chat_attachments/${this.runId}/${Date.now()}_${this.state.chatAttachment.name}`;
      const fileRef = storageRef(this.storage, filePath);
      await uploadBytes(fileRef, this.state.chatAttachment)
      downloadUrl = await getDownloadURL(fileRef)
    }
    push(ref(this.database, `conversationMessages/${this.runId}/${this.conversationId}`), {
      "author": this.state.chatFrom.id,
      "text": this.state.chatText,
      "attachmentUrl": downloadUrl,
      "createdAt": serverTimestamp()
    })
    this.state.chatUploading = false
    this.state.chatText = ""
    this.state.chatAttachment = null
  }

  onPostSubmit() {

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
    this.state.reporter1 = NO_USER
    this.state.reporter2 = NO_USER
    this.state.victim = NO_USER
    this.state.penalty = 0.05
    this.state.reward = 0.05
    this.state.reportReason = ""
  }

  onResetRunSubmit() {
    if (confirm("Fakt chceš všechno smazat a začít nový běh?")) {
      this.players.forEach(player => {
        if (player.defaultRating != undefined) {
          update(ref(this.database, "nearbyUsers/" + player.id), {
            "totalRating": player.defaultRating,
            "ratingCount": 4000
          })
        }
      })
      this.npcs.forEach(npc => {
        if (npc.defaultRating != undefined) {
          update(ref(this.database, "nearbyUsers/" + npc.id), {
            "totalRating": npc.defaultRating,
            "ratingCount": npc.id == "_karolina" ? 500000 : 4000
          })
        }
      })
      remove(ref(this.database, "reports"))
      remove(ref(this.database, "ratings"))
    }
  }

  onNewRunSubmit() {

  }

  onRatingSubmit() {
    push(ref(this.database, "ratings"), {
      "from": "_karolina",
      "to": this.state.ratingUser.id,
      "majorChange": this.state.ratingChange,
      "createdAt": serverTimestamp()
    })
    let message = (this.state.ratingChange > 0) ? "Nečekaná změna hodnocení! Uživateli " + this.state.ratingUser.name + " se zvýšilo hodnocení o " + this.state.ratingChange + "\n\nDůvod: " + this.state.ratingReason : "Nečekaná změna hodnocení! Uživateli " + this.state.ratingUser.name + " se snížilo hodnocení o " + -this.state.ratingChange + "\n\nDůvod: " + this.state.ratingReason
    const superblesk = this.npcs.find(npc => npc.id === "_superblesk");
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
}

export class State {

  constructor(
    public chatFrom: User = NO_USER,
    public chatTo: User = NO_USER,
    public chatText: string = "",
    public chatToAll: boolean = false,
    public chatAttachment: File | null = null,
    public chatUploading: boolean = false,
    public feedFrom: User = NO_USER,
    public feedPhoto: string = "",
    public feedText: string = "",
    public feedNotification: boolean = false,
    public reporter1: User = NO_USER,
    public reporter2: User = NO_USER,
    public victim: User = NO_USER,
    public penalty: number = 0.5,
    public reward: number = 0.5,
    public reportReason: string = "",
    public ratingUser: User = NO_USER,
    public ratingReason: string = "",
    public ratingChange: number = 0,
    public visibilityUser: User = NO_USER,
    public newRunId: number = 0,
    public newRunName: string = "",
    public newRunBasedOn: string = "",
  ) { }

}

export class User {

  constructor(
    public id: string,
    public name: string,
    public profilePictureUrl: string,
    public defaultRating: number | undefined,
    public totalRating: number | undefined
  ) { }

}

export class Run {

  constructor(
    public id: string,
    public name: string
  ) { }

}

let NO_USER = new User("unknown", "-- Nikdo --", "", undefined, undefined)
let LIVE_NPC_IDS = ["_barman", "_david", "_vaclav"]


