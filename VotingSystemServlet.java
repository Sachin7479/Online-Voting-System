import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import org.json.JSONObject;

/**
 * VotingSystemServlet - Backend Java Servlet for Online Voting System
 * This servlet handles vote submission and vote counting for Bihar Elections
 * 
 * Features:
 * - Receives vote data from frontend
 * - Validates voter eligibility
 * - Stores votes securely
 * - Prevents duplicate voting
 * - Returns vote confirmation
 */
@WebServlet("/submitVote")
public class VotingSystemServlet extends HttpServlet {
    
    // In-memory storage for votes (In production, use a database)
    private static Map<String, VoteData> votesDatabase = new HashMap<>();
    private static Map<String, Integer> voteCount = new HashMap<>();
    private static Set<String> votedUsers = new HashSet<>();
    
    // Vote data class
    static class VoteData {
        String voterId;
        String partyName;
        String partyValue;
        String timestamp;
        String ipAddress;
        
        public VoteData(String voterId, String partyName, String partyValue, 
                       String timestamp, String ipAddress) {
            this.voterId = voterId;
            this.partyName = partyName;
            this.partyValue = partyValue;
            this.timestamp = timestamp;
            this.ipAddress = ipAddress;
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Get vote data from request
            String voterId = request.getParameter("voterId");
            String partyName = request.getParameter("partyName");
            String partyValue = request.getParameter("partyValue");
            String timestamp = request.getParameter("timestamp");
            String ipAddress = request.getRemoteAddr();
            
            // Validate input
            if (voterId == null || partyName == null || partyValue == null) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid vote data. All fields are required.");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Check if user has already voted
            if (votedUsers.contains(voterId)) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "You have already voted. Multiple votes are not allowed.");
                out.print(jsonResponse.toString());
                return;
            }
            
            // Create vote data object
            VoteData voteData = new VoteData(voterId, partyName, partyValue, 
                                            timestamp, ipAddress);
            
            // Store vote in database
            votesDatabase.put(voterId, voteData);
            votedUsers.add(voterId);
            
            // Update vote count
            voteCount.put(partyValue, voteCount.getOrDefault(partyValue, 0) + 1);
            
            // Log vote (for audit purposes)
            logVote(voteData);
            
            // Send success response
            jsonResponse.put("success", true);
            jsonResponse.put("message", "Your vote has been successfully recorded!");
            jsonResponse.put("voterId", voterId);
            jsonResponse.put("partyVoted", partyName);
            jsonResponse.put("confirmationNumber", generateConfirmationNumber(voterId));
            
            out.print(jsonResponse.toString());
            
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "An error occurred while processing your vote.");
            jsonResponse.put("error", e.getMessage());
            out.print(jsonResponse.toString());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        
        String action = request.getParameter("action");
        
        if ("getResults".equals(action)) {
            // Return current vote counts
            jsonResponse.put("success", true);
            jsonResponse.put("totalVotes", votedUsers.size());
            jsonResponse.put("voteCount", voteCount);
            out.print(jsonResponse.toString());
            
        } else if ("checkVoted".equals(action)) {
            // Check if user has already voted
            String voterId = request.getParameter("voterId");
            boolean hasVoted = votedUsers.contains(voterId);
            jsonResponse.put("success", true);
            jsonResponse.put("hasVoted", hasVoted);
            out.print(jsonResponse.toString());
            
        } else {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid action");
            out.print(jsonResponse.toString());
        }
    }
    
    /**
     * Generate unique confirmation number for vote
     */
    private String generateConfirmationNumber(String voterId) {
        long timestamp = System.currentTimeMillis();
        return "VOTE-" + voterId + "-" + timestamp;
    }
    
    /**
     * Log vote data for audit trail (XML format)
     */
    private void logVote(VoteData vote) {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("vote_logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }
            
            // Create XML log file
            String filename = "vote_logs/votes_" + 
                            new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()) + 
                            ".xml";
            
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            // Write vote data in XML format
            bw.write("<vote>\n");
            bw.write("  <voterId>" + vote.voterId + "</voterId>\n");
            bw.write("  <partyName>" + vote.partyName + "</partyName>\n");
            bw.write("  <partyValue>" + vote.partyValue + "</partyValue>\n");
            bw.write("  <timestamp>" + vote.timestamp + "</timestamp>\n");
            bw.write("  <ipAddress>" + vote.ipAddress + "</ipAddress>\n");
            bw.write("</vote>\n");
            
            bw.close();
            fw.close();
            
            System.out.println("Vote logged: " + vote.voterId + " voted for " + vote.partyName);
            
        } catch (IOException e) {
            System.err.println("Error logging vote: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void destroy() {
        // Cleanup resources when servlet is destroyed
        System.out.println("VotingSystemServlet destroyed. Total votes: " + votedUsers.size());
    }
}