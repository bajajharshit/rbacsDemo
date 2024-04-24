package perfios.rbacs.Repository.UserRepository;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserDashboard;
import perfios.rbacs.RbacsApplication;

import javax.annotation.processing.Generated;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImplementation implements UserService{

    public static String userDashboardQuery = "SELECT ud.user_id, ud.user_email, utr.role_id from user_details ud, user_to_role utr WHERE ud.user_id = utr.user_id;";
    public static String addUserDetailQuery = "INSERT INTO user_details (status, user_email, user_first_name, user_last_name, user_password, user_phone_number) VALUES ( ?, ?, ?, ?, ?, ?);";
    public static String checkExistingUserQuery = "select user_id from user_details where user_email = ?;";
    public static String addUserRoleQuery = "insert into user_to_role(user_id,role_id) values(?,?);";
    public static String getAllUsersQuery = "select ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email, ud.user_password, ud.status, ud.user_phone_number,utr.role_id from user_details ud, user_to_role utr where ud.user_id = utr.user_id; ";
    public static String deleteUserInUserDetailsQuery = "delete from user_details where user_id = ?; ";
    public static String deleteUserInUserToRoleQuery =  "delete from user_to_role where user_id = ?;";
    public static String deleteUserRoleQuery = "delete from user_to_role where user_id = ? and role_id = ?";
    public static String addNewRoleToExistingUserQuery = "insert into user_to_role(user_id,role_id) values (?,?)";
    public static String updateInUserDetailsQuery = "update user_details set status = ?, user_email = ?, user_first_name = ?, user_last_name = ?, user_password = ?, user_phone_number = ? where user_id = ?;";




    //datasource object for connection pooling with JDBC
    private final DataSource dataSource;

    public UserServiceImplementation(DataSource dataSource){
        this.dataSource = dataSource;
    }


    @Override
    public List<User> getAllUsers() {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getAllUsersQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> userList = new ArrayList<>();
            while(resultSet.next()){
                User newUser = new User();
                newUser.setUserId(resultSet.getInt("user_id"));
                newUser.setUserFirstName(resultSet.getString("user_first_name"));
                newUser.setUserLastName(resultSet.getString("user_last_name"));
                newUser.setUserEmail(resultSet.getString("user_email"));
                newUser.setUserPassword(resultSet.getString("user_password"));
                newUser.setUserStatus(resultSet.getString("status"));
                newUser.setUserPhoneNumber(resultSet.getString("user_phone_number"));
                newUser.setUserRoleId(resultSet.getInt("role_id"));
                userList.add(newUser);
            }
            return userList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    //this function is to add a row in user_to_role table.
    public int addUserToRole(int user_id, int role_id) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement statement2 = connection.prepareStatement(addUserRoleQuery);
            statement2.setInt(1, user_id);
            statement2.setInt(2, role_id);
            RbacsApplication.check1();
            RbacsApplication.check3(user_id,role_id);
            return statement2.executeUpdate();
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return 0;
    }



    //this function is for adding new and existing users..
    @Override
    public String addUser(User user) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkExistingUserQuery); //this statement is for checking user already
            //exist in table or not, if exist then we will update only in user to role talbe, else we will add user first in user table
            // and then add in user to role table accordingly.
            statement.setString(1,user.getUserEmail());
            ResultSet check = statement.executeQuery();

            if(check.next()) {  //this will to check whether user already exist or not. so if user already exit apan only change user_to_role table.
                RbacsApplication.printString("inside user found check block and user_id = " + check.getInt("user_id"));
                int rowsAffected = addUserToRole(check.getInt("user_id"),user.getUserRoleId());
                if(rowsAffected == 1) return "User added with new role successfully";
                else return "user adding with new role failed";
            }
            else{
                RbacsApplication.printString("count = 0, no such user exist already");
            }
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        try{  //this try block is adding user into user_details table, not in user to role. for that we have created a function in this.
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(addUserDetailQuery,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUserStatus()); // Status
            statement.setString(2, user.getUserEmail()); // userEmail
            statement.setString(3, user.getUserFirstName()); // userFirstName
            statement.setString(4, user.getUserLastName()); // userLastName
            statement.setString(5, user.getUserPassword()); // userPassword
            statement.setString(6, user.getUserPhoneNumber()); // userPhoneNumber
            int rowsAffected = statement.executeUpdate(); //adding details into user table, user_id is automatically generating here.
            //now user is added to user table, ab w.r.t role, adding in user to role table
            if(rowsAffected == 0) return "adding user failed";
            int autogeneratedUserId = 0;
            try{
                ResultSet generatedKeys = statement.getGeneratedKeys();  //this methord is pointing to generated keys column. not to the
                                                                         // exact row where key is located in database.
                generatedKeys.next();     //this statement will make it point to the cell where actually our key is present.
                autogeneratedUserId = generatedKeys.getInt(1);      //with help of this, we are retrive that key into our int variable.
                RbacsApplication.printString("autogerateduserid is " + autogeneratedUserId);  //checking whether we receive actual key in console
            }catch (SQLException e){
                System.err.println(e.getMessage() + "  --generated key = " + autogeneratedUserId);
            }
            //fetched user's auto generated user_id here and then adding it to user_id,role_id table to set its role there.
            int rowsAffected2 = addUserToRole(autogeneratedUserId,user.getUserRoleId()); //saving in user to role table.
            //now two rows has been added, one in user table and another in user_to_role
            if(rowsAffected + rowsAffected2>1) return "New user saved successfully";
            else return "New user did not saved";

        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "user adding into DB failed";
    }


    @Override
    public String unassignUserRole(int user_id, int role_id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(deleteUserRoleQuery);
            preparedStatement.setInt(1,user_id);
            preparedStatement.setInt(2,role_id);
            int rowsAffected = preparedStatement.executeUpdate();
            if(rowsAffected>0) return "user is unassigned with given role";
            else return "give correct user nd role";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "provide correct credentials";
    }



    @Override
    public String updateUser(User user, int id) {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(updateInUserDetailsQuery);
            statement.setString(1, user.getUserStatus()); // Status
            statement.setString(2, user.getUserEmail()); // userEmail
            statement.setString(3, user.getUserFirstName()); // userFirstName
            statement.setString(4, user.getUserLastName()); // userLastName
            statement.setString(5, user.getUserPassword()); // userPassword
            statement.setString(6, user.getUserPhoneNumber()); // userPhoneNumber
            statement.setInt(7,id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected>0) return "user updated successfully :) ";
            else return "user not exist, add user first then update :(";

        }catch (SQLException e){
            System.err.println(e.getMessage());
            return "error found, check foreign key, sql query, provided json object";
        }
    }


    @Override
    public String addNewRoleToExistingUser(int user_id, int role_id) {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(addNewRoleToExistingUserQuery);
            statement.setInt(1,user_id);
            statement.setInt(2,role_id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected>0) return "User has been assinged to new role";
            else return "user is already assinged to the given role";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "operation failed";
    }

    @Override
    public String deleteUser(int id) {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(deleteUserInUserToRoleQuery);
            statement.setInt(1,id);
            int rowsAffected = statement.executeUpdate();
            RbacsApplication.printString("deleted in user to role " + rowsAffected);
            statement = connection.prepareStatement(deleteUserInUserDetailsQuery);
            statement.setInt(1,id);
            int rowsAffected2 = statement.executeUpdate();
            RbacsApplication.printString("for user details " + rowsAffected2);
            if(rowsAffected + rowsAffected2>1) return "User deleted successfully";
            return "User didnot deleted";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "user did not deleted";
    }

    @Override
    public List<UserDashboard> getAllUserDashboard(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(userDashboardQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<UserDashboard> userDashboardList  = new ArrayList<>();
            while(resultSet.next()){
                UserDashboard userDashboard = new UserDashboard();
                userDashboard.setUserEmail(resultSet.getString("user_email"));
                userDashboard.setUserId(resultSet.getInt("user_id"));
                userDashboard.setRoleId(resultSet.getInt("role_id"));
                userDashboardList.add(userDashboard);
            }
            return userDashboardList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }

}
