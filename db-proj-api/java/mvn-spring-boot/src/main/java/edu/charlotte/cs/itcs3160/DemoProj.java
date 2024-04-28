/** **
* ITCS 3160-0002, Spring 2024
* Marco Vieira, marco.vieira@charlotte.edu
* University of North Carolina at Charlotte

* IMPORTANT: this file includes the Python implementation of the REST API
* It is in this file that yiu should implement the functionalities/transactions   
*/
package edu.charlotte.cs.itcs3160;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoProj {

    public enum StatusCode {

        SUCCESS ("success", 200),
        API_ERROR ("api_error", 400),
        INTERNAL_ERROR ("internal_error", 500);
    
        private final String description; 
        private final int code; 
        
        private StatusCode(String description, int code) {
            this.description = description;
            this.code = code;
        }
        
        public String description() { 
            return description; 
        }

        public int code() { 
            return code; 
        }
    }

    private boolean validateToken(String token) {
        Boolean validated = false;

        Connection conn = RestServiceApplication.getConnection();

        try {
            Statement stmt = conn.createStatement();
            int affectedRows = stmt.executeUpdate("delete from tokens where timeout<current_timestamp");
            conn.commit();

            PreparedStatement ps = conn.prepareStatement("select username from tokens where token = ?");
            ps.setString(1, token);
            ResultSet rows = ps.executeQuery();

            if (!rows.next())
                validated=false;                 
            else
                validated=true;
        } 
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            validated=false;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
                validated=false;
            }
        }

        return validated;
    }

    private Map<String, Object> invalidToken() {
        Map<String, Object> returnData = new HashMap<String, Object>();

        returnData.put("status", 400);
        returnData.put("messAge", "a valid token is missing");

        return returnData;
    }

    // To use JWT tokens
    private JwtUtil jwtUtil = new JwtUtil();

    // For logging
    private static final Logger logger = LoggerFactory.getLogger(DemoProj.class);

    @GetMapping("/")
    public String landing() {
        return "Hello World (Java)!  <br/>\n"
                + "<br/>\n"
                + "Check the sources for instructions on how to use the endpoints!<br/>\n"
                + "<br/>\n"
                + "ITCS 3160-002, Spring 2024<br/>\n"
                + "<br/>";
    }

    // Login user using JWT tokens
    // curl -X PUT http://localhost:8080/loginJWT -H "Content-Type: application/json" -d '{"username": "ssmith", "password": "ssmith_pass"}'

    @PutMapping("/loginJWT")
    public Map<String, Object> loginUserJWT(@RequestBody Map<String, Object> payload) {
        Map<String, Object> returnData = new HashMap<String, Object>();
        
        if (!payload.containsKey("username") || !payload.containsKey("password"))
         {
            logger.warn("missing credentials");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing credentials");
            return returnData;    
        }
        
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");

        Connection conn = RestServiceApplication.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement("select 1 from person where username = ? and password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rows = stmt.executeQuery();

            if (!rows.next()) {
                logger.warn("invalid credentials");
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "invalid credentials");
                return returnData;                 
            }
            else {
                String token = jwtUtil.generateTokenJWT(username);
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("token", token);
            }

        } 
        catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());;
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return returnData;
    }

    // List all auctions using JWT tokens

    @GetMapping(value = "/auctions", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getAllAuctions(@RequestHeader("x-access-tokens") String token) {
        // Token validation if using JWT
        if (!jwtUtil.validateTokenJWT(token))
            return invalidToken();

        logger.info("###              DEMO: GET /departments              ### ");
        
        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        Connection conn = RestServiceApplication.getConnection();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id FROM auction");
            logger.debug("---- auctions  ----");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();

                content.put("aid", rows.getString("aid"));
                content.put("isbn", rows.getString("isbn"));
                content.put("start_date", rows.getString("start_date"));
                content.put("end_date", rows.getString("end_date"));
                content.put("current_bid", rows.getString("current_bid"));
                content.put("description", rows.getString("description"));
                content.put("item_isbn", rows.getString("item_isbn"));
                content.put("seller_person_id", rows.getString("seller_person_id"));
                results.add(content);
            }

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", results);

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return returnData;
    }

    // List auction by ID using JWT tokens

    @GetMapping(value = "/auctions/{aid}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getAuctionById(
        @RequestHeader("x-access-tokens") String token,
        @PathVariable("aid") Integer aid) {
        // Token validation if using JWT
        if (!jwtUtil.validateTokenJWT(token))
            return invalidToken();

        logger.info("###              DEMO: GET /auction              ### ");

        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        Connection conn = RestServiceApplication.getConnection();

        try {
            Statement stmt = conn.createStatement();
            PreparedStatement ps = conn.prepareStatement("SELECT aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id FROM auction WHERE aid = ?");
            ps.setInt(1, aid);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- auction  ----");
            if (rows.next()) {
                Map<String, Object> content = new HashMap<>();

                content.put("aid", rows.getString("aid"));
                content.put("isbn", rows.getString("isbn"));
                content.put("start_date", rows.getString("start_date"));
                content.put("end_date", rows.getString("end_date"));
                content.put("current_bid", rows.getString("current_bid"));
                content.put("description", rows.getString("description"));
                content.put("item_isbn", rows.getString("item_isbn"));
                content.put("seller_person_id", rows.getString("seller_person_id"));
                results.add(content);
            }

            returnData.put("status", StatusCode.SUCCESS.code());
            returnData.put("results", results);

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return returnData;
    }

    // Add User
    // curl -X POST http://localhost:8080/user/ -H 'Content-Type: application/json' -H "x-access-tokens: ssmith339965530" -d '{"ename": "PETER", "job": "ANALYST", "sal": 100, "dname": "SALES"}'

    @PostMapping(value = "/user/", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> addUser(
            @RequestBody Map<String, Object> payload
    ) {

        logger.info("###              DEMO: POST /Add User           ###");
        Connection conn = RestServiceApplication.getConnection();

        logger.debug("---- new user  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> returnData = new HashMap<String, Object>();

        // validate all the required inputs and types, e.g.,
        if ((!payload.containsKey("name")) || (!payload.containsKey("address")) || (!payload.containsKey("phone"))  || (!payload.containsKey("username")) || (!payload.containsKey("password"))) {
            logger.warn("missing inputs");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing inputs");
            return returnData;
        }

        try {
            // get new empno - may generate duplicate keys, which will lead to an exception
            Statement stmt = conn.createStatement();
            ResultSet rows = stmt.executeQuery("select coalesce(max(id),1) as id from Person"); //was "select coalesce(max(empno),1) empno from emp"
            rows.next();
            int userID = rows.getInt("id") + 1;
            //Sets up the insertion of a new User
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Person (id, name, address, phone, username, password) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, userID);
            ps.setString(2, (String) payload.get("name"));
            ps.setString(3, (String) payload.get("address"));
            ps.setString(4, (String) payload.get("phone"));
            ps.setString(5, (String) payload.get("username"));
            ps.setString(6, (String) payload.get("password"));
            int affectedRows = ps.executeUpdate();
            //Checks to ensure it was successful
            if (affectedRows == 1) {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("id", userID);

                conn.commit();
            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "Could not insert");

                conn.rollback();
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }

            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return returnData;
    }

        // Edit auction by AID using JWT tokens

    @PutMapping(value = "/auctions/{aid}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> editAuctionById(
        @RequestHeader("x-access-tokens") String token, 
        @PathVariable("aid") Integer aid,
        @RequestBody Map<String, Object> payload) {
            
        Map<String, Object> returnData = new HashMap<String, Object>();

        //Token validation if using JWT
        if (!jwtUtil.validateTokenJWT(token))
            return invalidToken();
        //validate that payload contains
        if(!(payload.containsKey("description"))){
            logger.warn("missing inputs");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing inputs");
            return returnData;
        }
        logger.info("###              DEMO: PUT /editAuction              ### ");
            
        List<Map<String, Object>> results = new ArrayList<>();
    
        Connection conn = RestServiceApplication.getConnection();
    
        try {
            //chcks if AID is in auctions
            Statement stmt = conn.createStatement();
            PreparedStatement ps = conn.prepareStatement("SELECT aid FROM auction WHERE aid = ?");
            ps.setInt(1, aid);
            ResultSet rows = ps.executeQuery();
            logger.debug("---- auction  ----");

            if (rows.next()) {
                ps = conn.prepareStatement("UPDATE auctions SET description = ? WHERE aid = ?");
                ps.setString(1, (String) payload.get("description"));
                ps.setInt(2, aid);
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 1) {
                    returnData.put("Status",StatusCode.SUCCESS.code());
                    returnData.put("Results", aid);
                    conn.commit();
                } else {
                    returnData.put("status", StatusCode.API_ERROR.code());
                    returnData.put("results", "error updating auction");
                    conn.rollback();
                }
            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("results", "auction does not exist");
                conn.rollback();
            }
    
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
    
        return returnData;
    }

    /**
     * Method for creating a bid
     * @author Sergio
     * @param token
     * @param aid
     * @return
     */
    @GetMapping(value = "/bid/{aid}/{bid}", produces = "application/json")
    @ResponseBody
    public Map<String, Object> placeBid(
            @RequestHeader("x-access-tokens") String token,
            @PathVariable("aid") Integer aid,
            @PathVariable("bid") Float bid) {
        // Token validation if using JWT
        if (!jwtUtil.validateTokenJWT(token))
            return invalidToken();

        logger.info("###              DEMO: GET /placeBid              ### ");

        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        Connection conn = RestServiceApplication.getConnection();
        //get username
        String username = jwtUtil.getTokenUsername(token);
        try {
            Statement stmt = conn.createStatement();
            //gets auction_isbn, current_bid
            PreparedStatement ps = conn.prepareStatement("SELECT isbn, current_bid from AUCTION where aid = ?");
            ps.setInt(1, aid);
            ResultSet rows = ps.executeQuery();
            rows.next();
            int auction_isbn = rows.getInt("isbn");
            double auctionCurrentBid = rows.getFloat("current_bid");
            //Checks if users placed bid is higher than stored bid.
            if(auctionCurrentBid >= bid){
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("Error:", "Your bid is less than current bid");
                return returnData;
            }

            //Gets User_ID as bidder_id
            ps = conn.prepareStatement("SELECT id from Person where username = ?");
            ps.setString(1, username);
            rows = ps.executeQuery();
            rows.next();
            //does an SQL call to see if ID is already in buyer DB
            int bidder_id = rows.getInt("id");
            ps = conn.prepareStatement("SELECT person_id from Buyer WHERE person_id = ?");
            ps.setInt(1, bidder_id);
            rows = ps.executeQuery();
            //check IF user is a new bidder

            if (!(rows.next())) {
                //Adds User to Buyer
                ps = conn.prepareStatement("INSERT INTO Buyer (person_id, bids_placed, items_won) VALUES (?, 1, 0)");
                ps.setInt(1, bidder_id);
                ps.executeUpdate();
                conn.commit();
            } else { //Updates the buyers info if they have an existing record.
                ps = conn.prepareStatement("UPDATE Buyer set bids_placed = bids_placed + 1 WHERE person_id = ?");
                ps.setInt(1, bidder_id);
                ps.executeUpdate();
                conn.commit();
            }
            rows = stmt.executeQuery("select coalesce(max(bid_id), 1) as bid_id from bid");
            rows.next();
            //gets next bid id
            int bid_ID = rows.getInt("bid_id") + 1;


            logger.debug("---- adding bid to bid  ----");
            //adds bid into bid DB
            Map<String, Object> content = new HashMap<>();
            ps = conn.prepareStatement("INSERT INTO bid (bid_id, bid_amount, bid_time, auction_aid, auction_isbn, buyer_person_id) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, bid_ID);
            ps.setFloat(2, bid);
            ps.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
            ps.setInt(4, aid);
            ps.setInt(5, auction_isbn);
            ps.setInt(6, bidder_id);
            results.add(content);
            int affectedRows = ps.executeUpdate();
            //Checks to ensure it was successful
            if (affectedRows == 1) {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("bid inserted", bid_ID);

                conn.commit();
            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "Could not insert");

                conn.rollback();
            }

            //updates current bid in auction
            ps = conn.prepareStatement("UPDATE auction set current_bid = ? WHERE aid = ?");
            ps.setFloat(1, bid);
            ps.setInt(2, aid);
            affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("auction updated", aid);

                conn.commit();
            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "Could not insert");

                conn.rollback();
            }

        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());
        }
        finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }

        return returnData;
    }

    // Create Auction
    // curl -X POST http://localhost:8080/user/ -H 'Content-Type: application/json' -H "x-access-tokens: ssmith339965530" -d '{"ename": "PETER", "job": "ANALYST", "sal": 100, "dname": "SALES"}'

    @PostMapping(value = "/auction/", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> createAuction(
            @RequestBody Map<String, Object> payload,
            @RequestHeader("x-access-tokens") String token
    ) {
        if (!jwtUtil.validateTokenJWT(token))
            return invalidToken();

        //gets username of signed in user
        String username = jwtUtil.getTokenUsername(token);

        logger.info("###              DEMO: POST /Add Auction           ###");
        Connection conn = RestServiceApplication.getConnection();

        logger.debug("---- new auction  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> returnData = new HashMap<String, Object>();

        // validate all the required inputs and types, e.g.,
        if ((!payload.containsKey("isbn")) || (!payload.containsKey("minimumPrice")) || (!payload.containsKey("description"))  || (!payload.containsKey("start_date")) || (!payload.containsKey("end_date"))
        || (!payload.containsKey("title"))) {
            logger.warn("missing inputs");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing inputs");
            return returnData;
        }

        try {
            //Search for user
            Statement stmt = conn.createStatement();
            PreparedStatement ps = conn.prepareStatement("select id from person where username = ?");
            ps.setString(1, username);
            ResultSet rows = ps.executeQuery();
            rows.next();
            int sellerId = rows.getInt("id");

            // get new auction id - may generate duplicate keys, which will lead to an exception
            rows = stmt.executeQuery("select coalesce(max(aid),1) as aid from auction"); //was "select coalesce(max(empno),1) empno from emp"
            rows.next();
            int aID = rows.getInt("aid") + 1;

            //Searches to see if item is new or not
            ps = conn.prepareStatement("select isbn from item where isbn = ?");
            ps.setInt(1, (Integer) payload.get("isbn"));
            rows = ps.executeQuery();

            if (!(rows.next())){
                ps = conn.prepareStatement("INSERT INTO item (isbn, item_status, title, category_category_id) VALUES (?, ?, ?, ?)");
                ps.setInt(1, (Integer) payload.get("isbn"));
                ps.setBoolean(2, false);
                ps.setString(3, (String) payload.get("title"));
                ps.setInt(4, 1);
                ps.executeUpdate();
                conn.commit();
            }

            //Sets up the insertion of a new auction
            ps = conn.prepareStatement("INSERT INTO auction (aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, aID);
            ps.setInt(2, (Integer) payload.get("isbn"));
            ps.setDate(3, java.sql.Date.valueOf((String) payload.get("start_date")));
            ps.setDate(4, java.sql.Date.valueOf((String) payload.get("end_date")));
            ps.setString(5, (String) payload.get("current_bid"));
            ps.setString(6, (String) payload.get("description"));
            ps.setInt(7, (Integer) payload.get("isbn"));
            ps.setInt(8, sellerId);
            int affectedRows = ps.executeUpdate();
            //Checks to ensure it was successful
            if (affectedRows == 1) {
                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("auction_id", aID);

                conn.commit();
            } else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("errors", "Could not insert");

                conn.rollback();
            }
        } catch (SQLException ex) {
            logger.error("Error in DB", ex);
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                logger.warn("Couldn't rollback", ex);
            }

            returnData.put("status", StatusCode.INTERNAL_ERROR.code());
            returnData.put("errors", ex.getMessage());

        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.error("Error in DB", ex);
            }
        }
        return returnData;
    }
}
