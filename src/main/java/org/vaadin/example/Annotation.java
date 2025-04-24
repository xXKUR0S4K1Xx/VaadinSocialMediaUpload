package org.vaadin.example;

public class Annotation {
}

/*
NEXT:
Function to reply to a post. Either reply to parent post or new post.
add like bar to parent. That got lost somehow
Sort function

 Features (erste Stufe)
Startseite mit Beitragsübersicht

Beiträge werden mit Datum, Autor und Text angezeigt

Sortiert nach „neueste zuerst“

Jeder Beitrag hat Like-Button

Beitrag erstellen

Eingabefeld für neuen Beitrag (Text)

„Posten“-Button

Kommentare zu Beiträgen

Kommentare unter jedem Beitrag

Kommentarfeld direkt unter dem Beitrag

Likes

Like-Button mit Zähler

Jeder Benutzer kann nur einmal liken

Benutzername speichern (lokal)

Kein Login nötig, aber Benutzername wird gespeichert (zB. beim ersten Aufruf abgefragt und in Datei/Speicher gespeichert)

elemente die ich benutzen könnte:
- Login
- Card  oder
- Accordion
- icons
- Notification
- Avatar
- Side Navigation

Ordner User:
    - UserName
    - E-mail
    - Password
    - all own postNames
    - number of posts
    - number of likes
Variablen:
UserName - String, Zb.xXRetardedXx
PostName - int, 1 - Unendlich

Registrier und login button
    - Nimm username und check ihn
        - Ja ist vorhanden
        - nein wähl einen anderen
    - Nimm Passwort
        - wenigstens 5 chars
    - Registrierung
        - Name als File unter Ordner User und PW als inhalt

Beiträge sehen
    - Mit card probieren
    - File sortiert durch namen (gespeichert als nummer) anzeigen (sort by newest)
    - sort by top
        - such durch die letzten 100 posts und mach liste mit Likes und Namen
        - sort list
        - Zeig posts in Reihenfolge der Sortierte Liste
        - Liken mit button das den Postnamen sucht, reingehen nach likes sucht und falls UserName likes +1
        - Ursprünglicher User like +1

Beiträge schreiben
    - In ein Textfeld eingeben
        - in JSON datei speichern
        - inhalt von post ist die Stelle des Posts (zb. 156), Stelle des posts den man kommentiert (falls neu dann = 0)
          dann Anzahl der likes, dann alle die kommentiert haben, dann inhalt, dann Datum/Zeit, Username. Namen von allen
          Likes.
        - Teilung durch #####
        - Kommentar = Post, dh Kommentar muss alle nötigen Informationen enthalten.
        - Username Posts + 1

0#0#4#User1,User2#Some content#2025-04-15T13:00#MyUser#User3

Index | Meaning | Example value
0 | Post ID | "9"
1 | Parent Post ID | "0"
2 | Number of Likes | "1"
3 | List of Commenters | "User2"
4 | Content | "First post, let’s see how this works!"
5 | Timestamp | "2025-04-15T"
6 | User (author) | "MyUser"
7 | List of Liked Users | "User3" or "User1,User2"

UI login:
    - Login Feld Mitte
    - Registrierung Mitte Unten

UI
    - Postfeld Mitte ober
    - Suche Post New oder Top
    - Cardfeld mit Posts von anderen
        - Avatar, Name, Time of Post
        - Username und timestamp von ParentId
        - Inhalt von ParentId
        - Username und timestamp von PostId
        - Inhalt von PostId
    - Sidenavigation rechts oben bis unten
        - Logout
        - Settings (leer lassen)
        - search for userName
            - Textfield
        -
UI Benutzer
    - Sortierung Neu oder Top
    - Cardfeld
        - Avatar, Name
        - Anzahl der Posts
        - Anzahl der Likes
    - Textbox mit Postinhalt

Klassen:
    - User
    - Post
    - Sort
*/