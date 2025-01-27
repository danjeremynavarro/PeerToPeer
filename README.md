# Peer to Peer File Sharing

This program is composed of a server and a client. The server is a CLI application
that connect to a database that keeps a list of host, port and file that is currently being 
shared.

The client is a gui application which connects through CORBA communicates with the server
to update the database of the file it is currently sharing. The client would also communicate
through other clients to check if a file is available to be downloaded and
to download a file from it.

Follow the steps below to run the program.

#### Notes:
1. This program is tested on a Debian computer
2. This program is compiled on Java 8

### Server instructions
1. First ensure that corba is installed on the computer
2. Run the corba server with the following command 
    1.  <code>orbd -ORBInitialPort 1050 -ORBInitialHost localhost&</code>
3. Ensure that the identifier.sqlite database is on the same directory as the Main class file
4. Run the following command to run the server application
    1. <code>java Main server -ORBInitialPort 1050 -ORBInitialHost localhost</code>
5. The program should say it is ready and waiting

### Client instructions
1. The client command accepts the same ORB arguments and also arguments to the directory where files are shared and 
where it will be downloaded and the port number to run from. The syntax to run the client program is as follows
    1. <code> java Main client -ORBInitialPort {orb port} -ORBInitialHost {orb host} {directory} {port} </code>
    