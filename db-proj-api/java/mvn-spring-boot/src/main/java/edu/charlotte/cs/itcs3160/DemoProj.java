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

    // Login user using simple tokens
    // curl -X PUT http://localhost:8080/login -H "Content-Type: application/json" -d '{"username": "ssmith", "password": "ssmith_pass"}'

    @PutMapping("/login")
    public Map<String, Object> loginUser(@RequestBody Map<String, Object> payload) {
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

        try (PreparedStatement stmt = conn.prepareStatement("select 1 from users where username = ? and password = ?")) {
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
                int rnd=(int) (Math.random()*999999999+111111111);
                String token = username + rnd;

                PreparedStatement stmt1 = conn.prepareStatement("insert into tokens values( ?, ? , current_timestamp + (60 * interval '1 min'))");
                stmt1.setString(1, username);
                stmt1.setString(2, token);
                int affectedRows = stmt1.executeUpdate();

                returnData.put("status", StatusCode.SUCCESS.code());
                returnData.put("token", token);
            }

            conn.commit();
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

        try (PreparedStatement stmt = conn.prepareStatement("select 1 from users where username = ? and password = ?")) {
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

    // List departments using simpe tokens
    // To use JWT tokens just modify @token_required to @token_required_jwt
    // curl -X GET http://localhost:8080/departments -H "Content-Type: application/json" -H "x-access-tokens: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzc21pdGgiLCJleHAiOjE3MTMxMTYyMjh9.FFHTvCmiXWmAh5tKKMeC1HH4DLPAB9d9IqqFYO2U3JOMCKjMO1qHNUIMYmmP0nfXXw56UbTOsfZOrVeOnCwquA"
    // curl -X GET http://localhost:8080/departments -H "Content-Type: application/json" -H "x-access-tokens: ssmith339965530"

    @GetMapping(value = "/departments", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getAllDepartments(@RequestHeader("x-access-tokens") String token) {
        // Token validation if using JWT
        //if (!jwtUtil.validateTokenJWT(token))
        //    return invalidToken();
        // Simple token validation 
        if (!validateToken(token))
            return invalidToken();

        logger.info("###              DEMO: GET /departments              ### ");
        
        Map<String, Object> returnData = new HashMap<String, Object>();
        List<Map<String, Object>> results = new ArrayList<>();

        Connection conn = RestServiceApplication.getConnection();

        try (Statement stmt = conn.createStatement()) {
            ResultSet rows = stmt.executeQuery("SELECT deptno, dname, loc FROM dept");
            logger.debug("---- departments  ----");
            while (rows.next()) {
                Map<String, Object> content = new HashMap<>();

                content.put("deptno", rows.getString("deptno"));
                content.put("dname", rows.getString("dname"));
                content.put("loc", rows.getString("loc"));
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

    // Add empolyee
    // curl -X POST http://localhost:8080/emp/ -H 'Content-Type: application/json' -H "x-access-tokens: ssmith339965530" -d '{"ename": "PETER", "job": "ANALYST", "sal": 100, "dname": "SALES"}'

    @PostMapping(value = "/emp/", consumes = "application/json")
    @ResponseBody
    public Map<String, Object> addEmployee(
            @RequestHeader("x-access-tokens") String token,
            @RequestBody Map<String, Object> payload
    ) {
        // Token validation if using JWT
        //if (!jwtUtil.validateTokenJWT(token))
        //    return invalidToken();
        // Simple token validation 
        if (!validateToken(token))
            return invalidToken();

        logger.info("###              DEMO: POST /Add Employee           ###");
        Connection conn = RestServiceApplication.getConnection();

        logger.debug("---- new employee  ----");
        logger.debug("payload: {}", payload);

        Map<String, Object> returnData = new HashMap<String, Object>();

        // validate all the required inputs and types, e.g.,
        if ((!payload.containsKey("ename")) || (!payload.containsKey("job")) || (!payload.containsKey("sal"))  || (!payload.containsKey("dname"))) {
            logger.warn("missing inputs");
            returnData.put("status", StatusCode.API_ERROR.code());
            returnData.put("errors", "missing inputs");
            return returnData;
        }

        try {
            PreparedStatement ps = conn.prepareStatement("select deptno from dept where dname = ?");
            ps.setString(1, (String) payload.get("dname"));
            ResultSet rows = ps.executeQuery();

            if (rows.next()) {
                int deptno = rows.getInt("deptno");

                // get new empno - may generate duplicate keys, wich will lead to an exception
                Statement stmt = conn.createStatement();
                rows = stmt.executeQuery("select coalesce(max(empno),1) empno from emp");
                rows.next();
                int empno = rows.getInt("empno")+1;

                ps  = conn.prepareStatement("INSERT INTO emp (empno,ename,job,sal,deptno) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, empno);
                ps.setString(2, (String) payload.get("ename"));
                ps.setString(3, (String) payload.get("job"));
                ps.setInt(4, (int) payload.get("sal"));
                ps.setInt(5, deptno);
                int affectedRows = ps.executeUpdate();

                if (affectedRows==1) {
                    returnData.put("status", StatusCode.SUCCESS.code());
                    returnData.put("empno", empno);

                    conn.commit();
                }
                else {
                    returnData.put("status", StatusCode.API_ERROR.code());
                    returnData.put("errors", "Could not insert");

                    conn.rollback();
                }                
            }
            else {
                returnData.put("status", StatusCode.API_ERROR.code());
                returnData.put("results", "department does not exist");
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
