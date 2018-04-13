import java.io.IOException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.*;

public class Iperfer {
   public static void main(String[] args){

     if(!args[0].equals("-c") && !args[0].equals("-s")) {
        System.out.println("Wrong flag");
        argumentError();
      }

      //Run Client mode
      if(args[0].equals("-c")){
         checkClientArgs(args, 7, 4);

         byte[] data = new byte[1000];
         int count = 0;

         String hostName = args[2];
         int portNumber = Integer.parseInt(args[4]);
         long duration = Long.parseLong(args[6]);
         duration = duration * (long) 1E9;

         try (
            Socket ClientSocket = new Socket(hostName, portNumber);
            BufferedOutputStream BuffOut = new BufferedOutputStream(ClientSocket.getOutputStream(), 1000);
         ) {
            long timeStart = System.nanoTime() ;
            while ((System.nanoTime() - timeStart) <= duration) {
              BuffOut.write(data, 0, 1000);
              BuffOut.flush();
              count++;
            }
         } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
         } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
         }

         printResult(count, duration);

      //Run Server mode
      } else {
         checkServerArgs(args, 3, 2);
         int portNumber = Integer.parseInt(args[2]);
         int count = 0;
         long bytesRead = 0;
         long startTime = 0;
         long endTime = 0;
         byte[] b = new byte[2000];

         try (
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientsSocket = serverSocket.accept();
            BufferedInputStream BuffIn = new BufferedInputStream(clientsSocket.getInputStream(), 1000);
        ) {
            startTime = System.nanoTime();
            int bytesAvailable = 0;

            while (true){
              bytesAvailable = BuffIn.available();
              if((bytesAvailable < 1000) && (bytesAvailable > 0)) {
                // System.out.println("Waiting only " + bytesAvailable +
                //     " are available");
                continue;
              }
              bytesRead = BuffIn.read(b, 0, 1000);
              if(bytesRead == 1000) {
                count++;
              } else if(bytesRead == -1) {
                endTime = System.nanoTime();
                break;
              } else {
                System.out.println("Error: only " + bytesRead +
                    " bytes were read");
              }
            }

            long duration = endTime - startTime;
            printResult(count, duration);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
      }
   }

   // Helper Method that checks if the arguments are correctly given for client
   public static void checkClientArgs(String[] args, int num, int port){
      if(args.length != num){
         //System.out.println(args.length);
         argumentError();
      }

      if (!args[1].equals("-h") || !args[3].equals("-p") || !args[5].equals("-t")) {
        argumentError();
      }

      checkPorts(Integer.parseInt(args[port]));
   }

   // Helper Method that checks if the arguments are correctly given for server
   public static void checkServerArgs(String[] args, int num, int port){
      if(args.length != num){
         //System.out.println(args.length);
         argumentError();
      }

      if (!args[1].equals("-p")) {
        argumentError();
      }

      checkPorts(Integer.parseInt(args[port]));
   }

   // Helper method to check that port provided was from 1024-65535
   public static void checkPorts(int port) {
     if ((port > 65535) ||
          (port < 1024)){
         System.err.println("Error: port number must be in the range 1024 to 65535");
         System.exit(1);
      }
   }

   // Hlper method to display generic argument error and exit
  public static void argumentError() {
    System.err.println("Error: missing or additional arguments");
    System.exit(1);
  }

  // helper method to print results
   public static void printResult(int count, long duration){
     // duration is in nanoseconds so 1E9 is to convert to seconds
     float rate = (float)(count * 8.00 * 1E9) / (float)(1024 * duration);
     System.out.printf("sent= %d KB rate=%.3f Mbps\n", count, rate);
   }
}
