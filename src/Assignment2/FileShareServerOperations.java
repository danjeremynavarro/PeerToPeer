package Assignment2;


/**
* Assignment2/FileShareServerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./server.idl
* Saturday, January 25, 2025 5:51:19 o'clock PM MST
*/

public interface FileShareServerOperations 
{
  Assignment2.KeyVal[] getFiles ();
  boolean insertFile (String host, int port, String filename);
  String getFile (String filename);
  boolean unshareFile (String host, String filename);
} // interface FileShareServerOperations
