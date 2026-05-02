<div align="center">

# <div style="display: flex; align-items: center; justify-content: center; gap: 10px;"><img src="src/main/resources/logo.png" alt="Ruota della Fortuna" width="50"> Ruota della Fortuna</div>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Swing](https://img.shields.io/badge/UI-Swing-5382A1?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/package-summary.html)

**Gioco multiplayer in Java: indovina la frase a turni, gira la ruota per punti ed eventi speciali, condivide le vite dell’impiccato.**

</div>

---

## ✨ Funzionalità

### 🎯 Funzionalità principali

- **Turni e frase segreta** – Più giocatori sulla stessa partita; a ogni turno si gira la ruota e si può tentare una lettera o l’intera frase
- **Ruota della fortuna** – Esiti come **100 / 200 / 500** punti (moltiplicati per le lettere trovate), **PERDI TURNO** e **BANCAROTTA** (pesi bilanciati sul server)
- **Impiccato condiviso** – Vite globali per tutti i giocatori; errori costruiscono il manichino; esaurite le vite si passa a un nuovo round con altra frase
- **Server authoritative** – Stato e risultato della ruota decisi dal server; il client anima la ruota fino al bonus comunicato dal server
- **Protocollo a pacchetti** – Comunicazione TCP su socket con messaggi tipizzati (`GamePacket` e sottoclassi), serializzazione/deserializzazione strutturata
- **Interfaccia grafica** – Connessione configurabile a runtime, nome utente univoco (minimo 3 caratteri), sala d’attesa, classifica e ruota trascinabile in senso antiorario

### 🛠️ Stack tecnologico

| Livello    | Tecnologie                                                                      |
| ---------- | ------------------------------------------------------------------------------- |
| Linguaggio | Java 21                                                                         |
| Build      | Maven (`maven-compiler-plugin`, `maven-jar-plugin`, `maven-shade-plugin`)       |
| Rete       | `ServerSocket` / socket TCP, lettura pacchetti su thread dedicati               |
| Client UI  | Swing (`ClientGUI` e pannelli: gioco, ruota, lista giocatori, attesa, ecc.)     |
| Dati frasi | File CSV `frasi.txt` (argomento e frase per riga), nella cartella di esecuzione |

---

## 🚀 Avvio rapido

1. **Compila** il progetto dalla root del repository:

   ```bash
   mvn -q package
   ```

2. **Prepara le frasi**: il file [`frasi.txt`](frasi.txt) deve trovarsi nella **directory di lavoro** da cui avvii client e server (puoi lasciare la root del repo se lanci i comandi da lì).

3. **Avvia il server** (porta predefinita **6789**):

   ```bash
   java -cp target/Impiccato-1.0-SNAPSHOT.jar game.server.Server
   ```

4. **Avvia il client** (JAR eseguibile con entry point GUI):

   ```bash
   java -jar target/Impiccato-1.0-SNAPSHOT.jar
   ```

5. Dal client inserisci host e porta del server, poi il nome utente. Con **almeno due** giocatori connessi è possibile avviare la partita dalla sala d’attesa.

### Prerequisiti

- [JDK 21](https://openjdk.org/) o compatibile
- [Maven 3.x](https://maven.apache.org/install.html)

---

## 📁 Struttura del progetto

```
RuotaFortuna/
├── pom.xml
├── frasi.txt                          # Frasi di gioco (CSV)
├── src/main/
│   ├── java/game/
│   │   ├── Game.java / Round.java / GamePlayer.java
│   │   ├── client/                    # Client, lettura socket, giocatore locale
│   │   │   └── ui/                    # ClientGUI e pannelli Swing
│   │   ├── server/                    # Server, accettazione client, broadcast
│   │   ├── packets/                   # GamePacket, pacchetti client/server/comuni
│   │   └── utils/                     # UI e colori
│   └── resources/
│       └── logo.png
└── target/                            # Artefatti dopo mvn package
```

---

## 📡 Protocollo (panoramica)

I messaggi estendono `GamePacket` e seguono un formato con **ID** e dati serializzati. Panoramica dei tipi principali:

| Direzione       | Esempi di pacchetti                                      |
| --------------- | -------------------------------------------------------- |
| Client → Server | `AUTH`, `TRY`                                            |
| Client ↔ Server | `RESTART`, `SPIN`                                        |
| Server → Client | `AUTH_SUCCESS`, `UPDATE`, `NEXT_ROUND`, `WIN`, `LOSE`, … |

---

## 📹 Showcase

<div align="center">

https://github.com/user-attachments/assets/c3cd73aa-4ffb-4867-b6ee-c9ccf2d6db30

</div>

---

<div align="center">

Fatto con ❤️ da [@Lorenzo0111](https://github.com/Lorenzo0111)

</div>
