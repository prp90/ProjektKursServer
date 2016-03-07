package server;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.android.gcm.server.InvalidRequestException;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.sql.*;

class TCPServer extends Thread
{
	final static String GCM_API_KEY = "AIzaSyDR71KLlMe_P3gI8nHAEl62hLzbQlS7Roc";
	static Sender sender = new Sender(GCM_API_KEY);
	private static Message msg = new Message.Builder().addData("msg", "Token Received").build();
	private static Message msg_lum_400 = new Message.Builder().addData("msg", "High luminosity").build();

	private static final int MYPORT= 8383;
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static String token;

	private static final String USER = "AntonPrih";
	private static final String PASSWORD = "Puta";

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/project?verifyServerCertificate=false"+
				"&useSSL=false"+
				"&requireSSL=false";

	private static Connection conn = null;
	private  static Statement stmt = null;




	public static void main(String[] args) throws IOException{


		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASSWORD);

			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT status FROM lamp";
			ResultSet rs = stmt.executeQuery(sql);

			//STEP 6: Clean-up environment
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
			}// nothing we can do
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		System.out.println("Goodbye!");


		try{
			serverSocket = new ServerSocket(MYPORT);
		} catch(IOException e){
			System.out.println("TCPEchoServer failed to create ServerSocket.");
			System.exit(-1);
		}

		while (true) {
			new TCPServer(serverSocket.accept());
		}
	}
	private TCPServer(Socket socket) {
		TCPServer.clientSocket = socket;
		start();
	}
	public void run() {
		try
		{
			String TCPclientMess = "";
			String returnMess;
			System.out.printf("TCP echo request from %s", clientSocket.getInetAddress().getHostAddress());
			System.out.printf(" using port %d\n", clientSocket.getPort());


			while(true)
			{
				BufferedReader iptFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				DataOutputStream optToClient = new DataOutputStream(clientSocket.getOutputStream());

				TCPclientMess = iptFromClient.readLine();

				String[] message = TCPclientMess.split("\\s");
				if (message[0].equals("Arduino")){
					System.out.println("Arduino");
					if (message[2].equals("400")){
						System.out.println("Jaaaaay!!");
						String not = TCPclientMess;
						Result result = sender.send(msg_lum_400,token , 3);

						if ((result.getErrorCodeName() == null)) {
							System.out.println(("GCM Notification is sent successfully"));
							returnMess = TCPclientMess;     
							optToClient.writeBytes("okej");
						}

						else {
							System.out.println(("Error occurred while sending push notification :" + result.getErrorCodeName()));
						}

					}
					else if (message[2].equals("100")){
						System.out.println("Priiiiiih");
					}
				}



				if (message[0].equals("phone"))
					System.out.println("Phone");

				System.out.println("You Sent: " + TCPclientMess);
				String not = TCPclientMess;

				if (message[0].equals("phone")){
					try {
						System.out.println("Token   " +not);
						token = not;
						Result result = sender.send(msg, not, 3);
						returnMess = TCPclientMess;     
						optToClient.writeBytes(returnMess);

						if ((result.getErrorCodeName() == null)) {
							System.out.println(("GCM Notification is sent successfully"));
						}

						else {
							System.out.println(("Error occurred while sending push notification :" + result.getErrorCodeName()));
						}
					} catch (InvalidRequestException e) {
						System.out.println(("Invalid Request"));
					} catch (IOException e) {
						System.out.println(("IO Exception"));
					}

					//  returnMess = TCPclientMess;     
					// optToClient.writeBytes(returnMess);
					TCPclientMess = "";
				}
			}
		}
		catch(Exception e)
		{
			e.getStackTrace();
		}
	}
}