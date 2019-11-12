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

