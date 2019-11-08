System Description

The designed and provided system is Java based and utilizes Apache Maven to manage its dependencies.
The server utilizes a lightweight Oracle HTTP library and a JDBC SQLite driver that enables the Java program to interact with a SQLite database.

On boot-up, the server creates an empty database with a single table that will map line numbers to lines. As the only guarantee provided is that a single line can fit into memory, the program will read individual lines from the text and insert them into the database. As text files are very inefficient for the requirements of the system, a structure that could offer quicker lookup times was needed. As such a structure cannot be kept in memory due to the given constraints, the next best option is to store such a structure on disk. Hence, a database was chosen to serve this purpose. This solution assumes that there will be enough space on disk to store such a database. As write requests to disk are slower than read requests, the initial preparation of the database will be time consuming. The server will not start accepting connections until the processing of the text file is complete.

Once the database is ready, the server will listen and accept new incoming connections using different threads. A thread pool is used to cap the maximum number of threads that can be created to prevent the system from being overloaded with requests. As a SQLite database is being used, multiple read accesses from different threads is possible. Choosing the maximum number of threads will require testing as it will vary significantly based on the hardware being used.

On finding the line number, the desired line is returned using the HTTP library with the desired code. As the system keeps track of the total number of lines in the database, requesting a line number that is larger will yield a 413 status. Although not in specifications, the system will also deal with common illegal requests with a 400 status.

Scaling Capabilities

The system will take longer to prepare before it can start accepting connections as the file sizes increase. While 1 GB files can be pre-processed rapidly, 100 GB files will cause the server to take a significant amount of time before it can start accepting connections. However, as preprocessing the file and storing it as a database gives it a serious efficiency boost when responding to client requests, this trade-off can be argued to be permissible.

As each request is handled in a separate thread, handling about a 100 concurrent requests should be efficient. 10000 users would be problematic as the queues would start to build up, and there would be a number of dropped connections depending on how many threads the system has been configured to create up to and how large the database is. The system will not be able to serve to 1000000 users concurrently. It will however not fail, and will steadily respond to a smaller number of requests given that the bandwidth has not entirely been flooded with requests. In order to scale for such requirements, multiple copies of the server could be installed with a DNS level distribution system.

Supporting Documentation and libraries

I needed a lightweight library to deal with SQLite databases, and on checking Maven repositories, I came across jdbc, and decided to use it. I considered its documentation and the examples accompanied alongside it.
I used Oracle's lightweight Http library as I had previously used it in the past to build a simple text based board game. The library is old, but is lightweight and has all the functionality that was required for this project.
I also looked through Java docs to implement thread pools, and stack overflow for general debugging information.

Future Plans

I spent a total of 6 hours on the system. If given more time, I would first investigate different established web frameworks as they are probably configured to scale better than Oracle's lightweight HTTPServer. Additionally, as relational databases structure their index in the form of binary trees, lookups can take O(logn) time. As such, it may actually be beneficial to use multiple databases to reduce this lookup time. For instance, one idea could be to store all odd entries in one database and all even entries in another. This way, when a request is sent, the line number can be checked to be odd or even in constant time, and then the look up is still made in logarithmic time but with a much more favorable constant. I believe this could be an example of an approach that could be worth investigating if the text files are going to be huge.

I would have preferred to write more stringent specifications for all my functions to ensure more clarity. I have tried to insert comments wherever I though they could be beneficial. Additionally, at the moment my code recreates the database every time the server is restarted. Instead of doing this, in the future, I could modify the program to take in an argument to specify if a new text file has been passed in and only then re-create the database.
