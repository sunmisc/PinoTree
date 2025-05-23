PinoTree: Immutable B-Tree Key-Value Store
PinoTree is a high-performance, immutable B-tree key-value store implemented in Java. It leverages an immutable B-tree structure for data persistence and uses linear regression to optimize key distribution for efficient range queries. Designed for reliability and scalability, PinoTree is ideal for applications requiring fast, versioned key-value storage with robust range query capabilities.
Features

Immutable B-Tree: Ensures data integrity by creating new tree nodes for each update, preserving historical data states for versioning and consistency.
Linear Regression on Keys: Optimizes range queries by modeling key distributions, reducing traversal time for large datasets.
Command-Line Interface: Provides a simple TCP-based interface for interacting with the store.
Persistent Storage: Stores data on disk, ensuring durability across server restarts.
Java-Based: Built with Java for cross-platform compatibility and ease of integration.

``Commands
PinoTree supports the following commands for interacting with the key-value store via a TCP client:
Commands:
put [table] [key] [value] ... <key_n> <value_n>
get [table] [key] ... <key_n>
delete [table] [key] ... <key_n>
deleteIfMapped [table] [key] [value] ... <key_n> <value_n>
first/last [table]
list [table] <offset> <count>
range [table] [from] [to]
size [table]
``

- put: Inserts or updates one or more key-value pairs in the specified table.
- get: Retrieves values for one or more keys from the table.
- delete: Removes one or more keys from the table.
- deleteIfMapped: Conditionally deletes key-value pairs if the key maps to the specified value.
- first/last: Returns the first or last key-value pair in the table.
- list: Fetches a subset of key-value pairs starting at an offset, up to a specified count.
- range: Retrieves key-value pairs within a specified key range.
- size: Returns the number of key-value pairs in the table.

Prerequisites:

- Java 21 or later
- Maven 3.6 or later


Build the Project:
``mvn clean package``


Run the Server:
``java -jar target/pinotree-1.0-SNAPSHOT.jar <port>``

Replace <port> with your desired port number (e.g., 8080).


Usage

Start the PinoTree server:
``java -jar target/pinotree-1.0-SNAPSHOT.jar 8080``

Output: Server started on port 8080

Connect to the server using a TCP client (e.g., netcat or a custom client):

``nc localhost 8080``


Execute commands, for example:
``
put mytable key1 value1 key2 value2
get mytable key1
range mytable key1 key3
size mytable
``

Technical Details

- Immutable B-Tree: PinoTree uses an immutable B-tree to ensure that updates (put, delete, etc.) create new tree versions, preserving previous states. This is ideal for applications requiring audit trails or rollback capabilities.
- Linear Regression: By applying linear regression to key distributions, PinoTree optimizes range queries (range, list) by predicting key locations, reducing the number of nodes traversed.
- Persistence: Data is stored on disk in a compact format, with the B-tree structure ensuring efficient access and updates.
- Codebase: The project contains approximately 2,500 lines of Java code, with ~1,800 lines of executable code (excluding comments and blank lines).

Please include tests and follow the project’s coding style (standard Java conventions).
License
GNU General Public License v3.0
Contact
For questions or suggestions, open an issue on GitHub or contact the maintainer at sunmisc.
