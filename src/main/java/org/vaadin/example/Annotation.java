package org.vaadin.example;

public class Annotation {
}
/*

Fixed notifications.
fixed gui
fixed screen scrolling

next is? idk
the entire media shenanigans are still missing in userpage so maybe that i guess?

Next objectives:
Status der user.            DONE
popup mit status der user.  NO
Follows.                    DONE
Notifications.              DONE
Search users/threads        NO
Whitemode/darkmode          ONLY DARK
Subreddits                  DONE

Notifications:
Commenter side:
if a user has made a reply
Go into folder of the user you made a comment in (check the forum first with the variable initialised already: String forumname = readCurrentForum(username);)
    - example path: C:\Users\sdachs\IdeaProjects\VaadinSocialMediaUpload\Forum\all
Find Notifications folder (don't forget to create them for each user on profile creation. DONE)
    - example path ("a" is a user): C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users/a/Notifications
check the numbers of text files in there
Add text file name 1,2,3 and so on (the newest)
the content of the text file is the parent content

Commentee side:
check notifications folder
    - example path: - example path ("a" is a user): C:/Users/sdachs/IdeaProjects/VaadinSocialMediaUpload/users/a/Notifications
add one to variable notificationNumber for each file
replace number in File NotificationNumber with the one in the variable (example path where "a" is a user: C:\Users\sdachs\IdeaProjects\VaadinSocialMediaUpload/users\a\NotificationNumber
add the number of notifications as a div on the bell
    -CARE the bell is currently an icon. Maybe create a see-through overlay with a number
if clicked on open a dropdown that contains all comments (first 20 letters or so, maybe 50 px)
if clicked on open a site with the exact comment (maybe userpage and sort by notifications in new list?)
after it was clicked remove the text file
go trhough each file in notifications folder and rename them in order




How to make subreddits:
    - check which subreddit you are on via: .txt file in User
    - always go to the default
    - Different List of posts
        - Make new Folder with Subreddit name
        - A "posts" folder is inside.
        - Load posts from that folder.
    - Search button searches subreddit
    - You can follow a subreddit. It shows in userpage



    - The "Popular" div opens a list of the subreddits ranked by number of posts
    - recommended wont work yet.
    - All brings you to the default page where EVERYTHING is saved.
    - Home brings you to the userpage








ForUserPage copy and paste this:
    postList = new VirtualList<>();
        postList.getElement().getStyle().set("scrollbar-gutter", "stable both-edges");  // Ensures the scrollbar appears on both edges.
        postList.getElement().getStyle()
                .set("padding", "0")
                .set("margin", "0");
        // Create list: first the post input card, then the posts
        List<Object> items = new ArrayList<>();  // Create a list to store the input card and posts.
        items.add(middleBar); // Middle bar with button
        items.add(inputCard);  // Add the input card to the list.
        items.addAll(UserPost.readPostsForUser(username));

Als nächsters:
Switch the sort button into the new layout.
Should display Sort
when you click it should show a dropdown menu with: top (und häkchen icon)
                                                    bottom.
häkchen je nach sort modus otherwise invisible.
dann background color zurück auf 1a1a1b




bio als text für jeden user:
    - In Userpage

vorgefehrtigte avatar bilder:
    - save avatar picturs somewhere and make them selectable in a drop down menu maybe?
    Maybe it's necessary to save the location and name in the UserFile

Notifications:
    - for every reply or like
    - seek ParentUser
    - on ParenProfile set number of replies or likes +1
    - set "# +Text" for each reply
    - set "# +Text" for each like (your text!)
    - Show Notifications by number
    - when clicked open a vitual list
        -for each like notification say "*User* liked your post: *PostText*"
        -for each reply notification say "*User* replied to your Post: *Reply*"

Wie speichern?
    When you like or reply to a Post:
        - go to User.Posts (of the person you replied to or liked)
            -First line only: "t#" or "f#" If its then there are no notifications
                -It's set to f every time one clicks on the notfication bell
                -It's set to t every time somebody likes or replies to your post
            -set "PostId#"
            -set "1#" for checked and "0#" for unchecked // is it checked? 1 for yes or 0 for no
            -set "l#" for like and "r#" for reply
            -set "Username#" for Person who replied or liked
            -next line

 */


/*
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