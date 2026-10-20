package Project;

import java.sql.*;
import java.util.Scanner;

public class Project {
	
		static Scanner sc = null;
	    static Connection conn = null;
	    static PreparedStatement psmt = null;
	    static Statement stmt = null;
	    static ResultSet rs = null;
		
	    public static void main(String[] args) throws Exception
	  {
	    	sc = new Scanner(System.in); 
	    	
	        // Loading Driver
	        Class.forName("com.mysql.cj.jdbc.Driver");

	        // Establishing Connection
	        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/janbatch?user=root&password=suresh");
	        
	        System.out.println("      BANK TRANSACTION MANAGEMENT SYSTEM");
	        System.out.println();
	        System.out.println("Press 1 for View Customer Details");
	        System.out.println("Press 2 for Perform Bank Transaction");
	        System.out.println("Press 3 for Exit");
	        System.out.println();
	        System.out.println("Enter your choice: ");
	        int operation = sc.nextInt();
	        	
	        switch (operation) 
	        {
	        case 1: retrieveData(); break;
	        
	        case 2: bankTransactionManagement(); break;
	        
	        case 3 :  System.out.println("Transaction Ended"); return;
	        default: System.out.println("Invalid Choice");
	        
	        return;
	        }
	        sc.close();
}

	  public static void retrieveData() throws SQLException {
		
		 stmt = conn.createStatement();
		
		 String retrieve = "SELECT * FROM bank";
		
		 rs = stmt.executeQuery(retrieve);
		
		 System.out.printf("%-10s %-15s %-15s %-10s%n",
		        "Acc No", "Name", "Balance", "PIN");
		
		 System.out.println("-----------------------------------------------");
		
		 while (rs.next()) {
		
		 System.out.printf("%-10d %-15s %-15d %-10d%n",
		            rs.getInt(1),
		            rs.getString(2),
		            rs.getInt(3),
		            rs.getInt(4));
		    }
		    rs.close();
		    stmt.close();
		    psmt.close();
	    }
	      
	 // Transaction Flow
	    public static void bankTransactionManagement() {

	        try {

	            System.out.print("Enter Sender Account Number : ");
	            int sender = sc.nextInt();

	            boolean senderExists = isAccountExists(sender);
	            if(!senderExists) {
	                System.out.println("Sender Account Not Found.");
	                return;
	            }

	            System.out.print("Enter Receiver Account Number : ");
	            int receiver = sc.nextInt();

	            boolean receiverExists = isAccountExists(receiver);
	            if(!receiverExists) {
	                System.out.println("Receiver Account Not Found.");
	                return;
	            }
	            
	            if (sender == receiver) {
	                System.err.println("Sender and Receiver account Number cannot be the same.");
	                System.out.println("Transaction failed");
	                return;
	            }

	            System.out.print("Enter Amount : ");
	            int amount = sc.nextInt();
	            
	            if (amount <= 0) {
	                System.err.println("Enter a valid amount. Amount must be Greater than 0");
	                System.out.println("Transaction  Failed");
	                return;
	            }

	            System.out.print("Enter PIN : ");
	            int pin = sc.nextInt();

	             boolean  isPinValid = validatePin(sender, pin);
	             if(!isPinValid) {
	                System.out.println("Incorrect PIN.");
	                return;
	            }

	            if (!checkBalance(sender, amount)) {
	                System.out.println("Insufficient Balance.");
	                return;
	            }

	            transferAmount(sender, receiver, amount);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    // Check Account Exists
	    public static boolean isAccountExists(int accNo) throws SQLException {

	        String query = "SELECT accNo FROM bank WHERE accNo=?";

	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, accNo);

	        ResultSet rs = ps.executeQuery();

	        boolean exists = rs.next();

	        rs.close();
	        ps.close();

	        return exists;
	    }

	    // Validate PIN
	    public static boolean validatePin(int accNo, int pin) throws SQLException {

	        String query = "SELECT pin FROM bank WHERE accNo=?";

	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, accNo);

	        ResultSet rs = ps.executeQuery();

	        boolean valid = false;

	        if (rs.next()) {
	            valid = rs.getInt(1) == pin;
	        }

	        rs.close();
	        ps.close();

	        return valid;
	    }

	    // Check Balance
	    public static boolean checkBalance(int accNo, int amount) throws SQLException {

	        String query = "SELECT balance FROM bank WHERE accNo=?";

	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, accNo);

	        ResultSet rs = ps.executeQuery();

	        boolean sufficient = false;

	        if (rs.next()) {

	            int balance = rs.getInt("balance");

	            if (balance >= amount)
	                sufficient = true;
	          
	        }

	        rs.close();
	        ps.close();

	        return sufficient;
	    }

	    // Transfer Amount
	    public static void transferAmount(int sender, int receiver, int amount) {

	        try {

	            conn.setAutoCommit(false);

	            String debit = "UPDATE bank SET balance = balance-? WHERE accNo=?";
	            PreparedStatement ps1 = conn.prepareStatement(debit);

	            ps1.setInt(1, amount);
	            ps1.setInt(2, sender);

	            int row1 = ps1.executeUpdate();

	            String credit = "UPDATE bank SET balance=balance + ? WHERE accNo=?";
	            PreparedStatement ps2 = conn.prepareStatement(credit);

	            ps2.setInt(1, amount);
	            ps2.setInt(2, receiver);

	            int row2 = ps2.executeUpdate();

	            if (row1 == 1 && row2 == 1) {

	                conn.commit();
	                System.out.println("Transaction Successful.");

	            } else {

	                conn.rollback();
	                System.out.println("Transaction Failed.");
	            }

	            ps1.close();
	            ps2.close();

	        } catch (Exception e) {

	            try {
	                conn.rollback();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }

	            e.printStackTrace();
	        }
	    }
	}
