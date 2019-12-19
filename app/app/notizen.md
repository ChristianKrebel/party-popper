| Name            | Convention             | Inherits                    |
| --------------- | ---------------------- | --------------------------- |
| Activity        | CreateTodoItemActivity | AppCompatActivity, Activity |
| List Adapter    | TodoItemsAdapter       | BaseAdapter, ArrayAdapter   |
| Database Helper | TodoItemsDbHelper      | SQLiteOpenHelper            |
| Network Client  | TodoItemsClient        | N/A                         |
| Fragment        | TodoItemDetailFragment | Fragment                    |
| Service         | FetchTodoItemService   | Service, IntentService      |



**com.example.myapp.service.*** - Ist ein Unterpaket für alle Service Pakete/Klassen

 **com.example.myapp.features.*** - Alle UI abhängigen Pakete

 **com.example.myapp.features.login** - Alle Klassen die zum Login Screen gehören



 `DetailsActivity`, `DetailsFragment`, `DetailsListAdapter`, `DetailsItemModel` in **ein UI Paket**.



`DetailsListAdapter` und `DetailsItemModel`Klassen können **Package Private** sein. Spart massig Setter und Getter.

 https://medium.com/hackernoon/package-by-features-not-layers-2d076df1964d#.f7znkie19 

 https://dev.to/miguelrodoma95/tips-to-keep-your-android-app-project-organized-361n 

 https://github.com/futurice/android-best-practices 
 
 
firebase deploy --only functions
 
 FireStore
 - Max Dokumenten Größe 1 MiB (1.048.576 bytes) => 1.048.576 UTF-8 Zeichen pro Dokument
 - UID 28 Zeichen lang // Kann sich in Zukunft ändern aber erstmal unwahrscheinlich
 
 Follower
 - Aktuelles Limit ca. 37.449 Follower
    - Skalierbar durch mehrere Dokumente aneinander reihen mit next oder Subcollections?
    
    
    
1.000.000.000
 
 

