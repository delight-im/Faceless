# Topics

## Adding new topics

Whenever you want to add new topics to the Android client and the server, you have to add them in several places:

 1. In the `string-array` named `topics_list_human` in file `res/values/global.xml` with a reference to the locale-specific translation
 2. In the `string-array` named `topics_list_machine` in file `res/values/global.xml` with the machine-readable topic name/key
 3. In the `string-array` named `topics_list_default` in file `res/values/global.xml` with the same value as in `topics_list_machine`, unless the topic should not be selected/enabled by default
 4. As single `string` resources in file `strings.xml` for each locale-specific directory `res/values[-xx]`
 5. In the definition of the `topic` column in the `CREATE TABLE messages (...)` statement in file `Database/STRUCTURE.sql`
