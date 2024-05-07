package perfios.rbacs.Repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
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
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImplementation implements UserService{


    private static final String userDashboardQuery = "SELECT ud.user_id, ud.user_email, utr.role_id from user_details ud, user_to_role utr WHERE ud.user_id = utr.user_id;";
    private static final String addUserDetailQuery = "INSERT INTO user_details (status, user_email, user_first_name, user_last_name, user_password, user_phone_number) VALUES ( ?, ?, ?, ?, ?, ?);";
    private static final String checkExistingUserQuery = "select user_id from user_details where user_email = ?;";
    private static final String addUserRoleQuery = "insert into user_to_role(user_id,role_id) values(?,?);";
    private static final String getAllUsersQuery = "select ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email, ud.user_password, ud.status, ud.user_phone_number,utr.role_id from user_details ud, user_to_role utr where ud.user_id = utr.user_id; ";
    private static final String deleteUserInUserDetailsQuery = "delete from user_details where user_id = ?; ";
    private static final String deleteUserInUserToRoleQuery =  "delete from user_to_role where user_id = ?;";
    private static final String deleteUserRoleQuery = "delete from user_to_role where user_id = ? and role_id = ?";
    private static final String addNewRoleToExistingUserQuery = "insert into user_to_role(user_id,role_id) values (?,?)";
    private static final String updateInUserDetailsQuery = "update user_details set status = ?, user_email = ?, user_first_name = ?, user_last_name = ?, user_password = ?, user_phone_number = ? where user_id = ?;";
    private static final String checkNumberOfRolesAssociatedWithUserQuery = "select count(*) from user_to_role where user_id = ?;";
    private static final String fetchRoleIdAndRoleNameQuery = "select role_id,role_name from role_details";
    private static final String fetchRoleIdFromRoleNameQuery = "select role_id from role_details where role_name = ?;";
    private static final String getExistingUserDetailsQuery = "SELECT ud.user_id, ud.user_first_name, ud.user_last_name, ud.user_email, ud.user_password, ud.status, ud.user_phone_number, utr.role_id FROM user_details ud, user_to_role utr WHERE ud.user_id = utr.user_id AND ud.user_id = ?;";
    private static final String fetchRoleNameFromRoleIdQuery = "select role_name from role_details where role_id = ?;";
    private static final String addNewUserQuery = "insert into user_detail(status,user_email,user_first_name,user_last_name,user_password,user_phone_number,role_id) values(?,?,?,?,?,?,?);";
    private static final String getAllRolesIdAssociatedWithUserQuery = "select role_id from user_to_role where user_id = ?;";



    //datasource object for connection pooling with JDBC
    private final DataSource dataSource;

    //this constructor injects DatasSource object into above created datasource with help of springBoot IOC container.
    public UserServiceImplementation(DataSource dataSource){
        this.dataSource = dataSource;
    }


    //function to display list of users with their roles, if one user have multiple role then it will return separate row for that.
    //one row with first role, another row with second role and so on.
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
                PreparedStatement userRoleFetched = connection.prepareStatement(fetchRoleNameFromRoleIdQuery);
                userRoleFetched.setInt(1,resultSet.getInt("role_id"));
                ResultSet roleNameFetched = userRoleFetched.executeQuery();
                roleNameFetched.next();
                newUser.setUserRoleName(roleNameFetched.getString("role_name"));
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
            RbacsApplication.check3(user_id,role_id);
            return statement2.executeUpdate();  //this returns int, if 1 => means user with that role saved, else check inputs.
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return 0;
    }


    


    //this function is for adding new and existing users with new role..
    //if an user is already added with role 1, second time it will only update in user_to_role table.
    //so there will be no duplicate entry for same user in user_detals table.
    @Override
    public String addUser(User user) {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkExistingUserQuery); //this statement is for checking user
            // already exist in table or not, if exist then we will update only in user to role talbe, else we will add user first
            // in user table and then add in user to role table accordingly.

            //if a user is already exist in user_details table, and now we are adding it again with a different role,
            //so with help of user's email, we are cheking it. if email id is already there in user table then only
            //we can assign it new role and make a new entry in user_to_role table.
            //if we add new user with same email, it won't allow to add more than one user with same email.

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
            //now user is added to user table, ab w.r.t role, adding in user_to_role table
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
            user.setUserId(autogeneratedUserId);
            RbacsApplication.check2(user);

            statement = connection.prepareStatement(fetchRoleIdFromRoleNameQuery);
            statement.setString(1,user.getUserRoleName());
            ResultSet roleName = statement.executeQuery();
            roleName.next();
            //fetched user's auto generated user_id here and then adding it to user_id,role_id table to set its role there.
            int rowsAffected2 = addUserToRole(autogeneratedUserId,roleName.getInt("role_id")); //saving in user to role table.
            //now two rows has been added, one in user table and another in user_to_role
            if(rowsAffected + rowsAffected2>1) return "New user saved successfully";
            else return "New user did not saved";

        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "user adding into DB failed";
    }


    public int checkNumberOfRolesAssociatedWithUser(int user_id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(checkNumberOfRolesAssociatedWithUserQuery);
            statement.setInt(1,user_id);
            ResultSet countOfRoles = statement.executeQuery();
            countOfRoles.next();
            int rowsAffected = countOfRoles.getInt("count(*)");
            return rowsAffected;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return 0;
    }


    @Override
    public String unassignUserRole(int user_id, int role_id){
        int checkNumberOfRolesUserHad = checkNumberOfRolesAssociatedWithUser(user_id);
        if(checkNumberOfRolesUserHad == 0) return "user did not exist";
        if(checkNumberOfRolesUserHad == 1) return "user must have atleast one role, cannot unassign given role to user";
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





    public String updateUser2(User user, int id){
        try{
            Connection connection  = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(updateInUserDetailsQuery);
 //status = ?, user_email = ?, user_first_name = ?, user_last_name = ?, user_password = ?, user_phone_number = ? where user_id
            statement.setString(1, user.getUserStatus()); // Status
            statement.setString(2, user.getUserEmail()); // userEmail
            statement.setString(3, user.getUserFirstName()); // userFirstName
            statement.setString(4, user.getUserLastName()); // userLastName
            statement.setString(5, user.getUserPassword()); // userPassword
            statement.setString(6, user.getUserPhoneNumber()); // userPhoneNumber
            statement.setInt(7,id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return "updating user failed, invalid user_id in url";
            if(rowsAffected == 1) RbacsApplication.printString("user details table updated successfully");
            //till here, user details table has been updated successfully, now for user_to_role table:-
            //first we will fetch role_id from role_details with the help of role_name passed to us..
            statement = connection.prepareStatement(fetchRoleIdFromRoleNameQuery);
            statement.setString(1,user.getUserRoleName());
            ResultSet roleIdFetched = statement.executeQuery();
            if(!roleIdFetched.next()) return "invalid role entered";
            int roleIdToAdd = roleIdFetched.getInt("role_id");
            RbacsApplication.printString(user.getUserRoleName() +" id = " + roleIdToAdd);
            //fetched role id from passed role name, now updating in user_to_role table;
            statement = connection.prepareStatement(addNewRoleToExistingUserQuery);
            statement.setInt(1,id);
            statement.setInt(2,roleIdToAdd);
            int rowsAffected2 = 0;
            try{
                rowsAffected2 = statement.executeUpdate();
            }catch (SQLException e){
                System.err.println(e.getMessage());
            }
            if(rowsAffected2 == 0) return "user updated successfully with the same role";
            if(rowsAffected2 + rowsAffected > 1) return "user with new role updated successfully";
        }catch (SQLException e){   //main catch block
            System.err.println(e.getMessage());
        }
        return "User adding failed, check Query, Connection with DB and place holders.";
    }


    @Override
    public String updateUser(User user, int id) {
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statementForOldUser = connection.prepareStatement(getExistingUserDetailsQuery);
            statementForOldUser.setInt(1,id);
            User existingUser = new User();
            ResultSet existingUserFetched = statementForOldUser.executeQuery();
            existingUserFetched.next();
            existingUser.setUserId(id);
            existingUser.setUserPhoneNumber(existingUserFetched.getString("user_phone_number"));
//            existingUser.setUserEmail(existingUserFetched.getString("user_email"));
            existingUser.setUserStatus(existingUserFetched.getString("status"));
            existingUser.setUserFirstName(existingUserFetched.getString("user_first_name"));
            existingUser.setUserPassword(existingUserFetched.getString("user_password"));
            existingUser.setUserRoleId(existingUserFetched.getInt("role_id"));
            existingUser.setUserLastName(existingUserFetched.getString("user_last_name"));
            PreparedStatement statement = connection.prepareStatement(updateInUserDetailsQuery);
            RbacsApplication.printString("jo user frontend se pass hua");
            RbacsApplication.check2(user);
            RbacsApplication.printString("jo user existing tha with id = "+id);
            RbacsApplication.check2(existingUser);
            RbacsApplication.printString("id = "+id);
            //check whether passed user has all that details or not and then send
            String userStatus = user.getUserStatus();
            if(userStatus == null) userStatus = existingUser.getUserStatus();
            String userPhoneNumber = user.getUserPhoneNumber();
            if(userPhoneNumber == null) userPhoneNumber = existingUser.getUserPhoneNumber();
            String userFirstName = user.getUserFirstName();
            if(userFirstName == null) userFirstName = existingUser.getUserFirstName();
            String userLastName = user.getUserLastName();
            if(userLastName == null) userLastName = existingUser.getUserLastName();
            String userEmail = user.getUserEmail();
            String userPassword = user.getUserPassword();
            if(userPassword == null) userPassword = existingUser.getUserPassword();
            statement.setString(1, userStatus); // Status
            statement.setString(2, userEmail); // userEmail
            statement.setString(3, userFirstName); // userFirstName
            statement.setString(4, userLastName); // userLastName
            statement.setString(5, userPassword); // userPassword
            statement.setString(6, userPhoneNumber); // userPhoneNumber
            statement.setInt(7,id);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected == 0) return " User not exist to update, add user first then update.";
            RbacsApplication.printString("rowsaff1 = " + rowsAffected);
            statement = connection.prepareStatement(fetchRoleIdFromRoleNameQuery);
            statement.setString(1,user.getUserRoleName());
            RbacsApplication.printString(user.getUserRoleName());
            ResultSet fetchedRoleId = statement.executeQuery();
            RbacsApplication.printString("roleid fetching query executed" + fetchedRoleId);
            if(!fetchedRoleId.next()) return "invalid role name Passed. check role_name from role_details table!!";
            int roleIdToAdd = fetchedRoleId.getInt("role_id");
            RbacsApplication.printString("roleIDTOADD = "+roleIdToAdd);
            try{
                statement = connection.prepareStatement(addUserRoleQuery);
            }catch (SQLException e){
                System.err.println(e.getMessage());
                return "User details saved successfully and role_name passed is already assinged to user";
            }
            statement.setInt(1,id);
            statement.setInt(2,roleIdToAdd);
            RbacsApplication.printString("adding role to user with id = "+id  + " and role_id = " + roleIdToAdd);
            int rowsAffected2 = statement.executeUpdate();
            if(rowsAffected + rowsAffected2>0) return "user updated successfully :) ";
            else return "user not update :(";

        }catch (SQLException e){
            System.err.println(e.getMessage());
            return "error found, check foreign key, sql query, provided json object";
        }
    }



    @Override
    public User getParticularUserById(int id){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statementForOldUser = connection.prepareStatement(getExistingUserDetailsQuery);
            statementForOldUser.setInt(1,id);
            User existingUser = new User();
            ResultSet existingUserFetched = statementForOldUser.executeQuery();
            existingUserFetched.next();
            if(existingUserFetched == null) return null;
            existingUser.setUserId(id);
            existingUser.setUserPhoneNumber(existingUserFetched.getString("user_phone_number"));
            existingUser.setUserEmail(existingUserFetched.getString("user_email"));
            existingUser.setUserStatus(existingUserFetched.getString("status"));
            existingUser.setUserFirstName(existingUserFetched.getString("user_first_name"));
            existingUser.setUserLastName(existingUserFetched.getString("user_last_name"));
            existingUser.setUserPassword(existingUserFetched.getString("user_password"));
//          //got everything except role array.
            HashMap<Integer,String > roleDetails = new HashMap<>();
            PreparedStatement roleTableStatement = connection.prepareStatement(fetchRoleIdAndRoleNameQuery);
            ResultSet roleDetailsSet = roleTableStatement.executeQuery();
            while(roleDetailsSet.next()){
                roleDetails.put(roleDetailsSet.getInt("role_id"),roleDetailsSet.getString("role_name"));
            }
            PreparedStatement userRoleStatment = connection.prepareStatement(getAllRolesIdAssociatedWithUserQuery);
            userRoleStatment.setInt(1,id);
            ResultSet userRoleSet = userRoleStatment.executeQuery();
            List<String > userRoleNameList = new ArrayList<>();
            while(userRoleSet.next()){
                userRoleNameList.add(roleDetails.get(userRoleSet.getInt("role_id")));
            }
            existingUser.setUserRoleNameList(userRoleNameList);
            return existingUser;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
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


    //this function is to delete user
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


    //this functino is for return user list for the dashborad.
    //a hashmap is created here whenever the query is fired, it adds role_id and role_name in it
    // and then returns role_name from hashmap. so we dont want to read from table(roles) for every entry.
    @Override
    public List<UserDashboard> getAllUserDashboard(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(userDashboardQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            preparedStatement = connection.prepareStatement(fetchRoleIdAndRoleNameQuery);
            ResultSet roleIdRoleName = preparedStatement.executeQuery();
            HashMap<Integer,String> roleDetails = new HashMap<>();
            while (roleIdRoleName.next()){
                roleDetails.put(roleIdRoleName.getInt("role_id"),roleIdRoleName.getString("role_name"));
            }
            List<UserDashboard> userDashboardList  = new ArrayList<>();
            while(resultSet.next()){
                UserDashboard userDashboard = new UserDashboard();
                userDashboard.setUserEmail(resultSet.getString("user_email"));
                userDashboard.setUserId(resultSet.getInt("user_id"));
                userDashboard.setRoleName(roleDetails.get(resultSet.getInt("role_id")));
                userDashboardList.add(userDashboard);
            }
            return userDashboardList;
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return null;
    }




//addUser2 is for many to one relationship. multiple user can have one role like that.
    @Override
    public String addUser2(User user){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement  = connection.prepareStatement(fetchRoleIdFromRoleNameQuery);
            statement.setString(1,user.getUserRoleName());
            ResultSet roleIdResultSet = statement.executeQuery();
            if(!roleIdResultSet.next()) return "cannot add user because role passed is invalid. [check spelling]";
            int userRoleId = roleIdResultSet.getInt("role_id");
            statement = connection.prepareStatement(addNewUserQuery);
//status,user_email,user_first_name,user_last_name,user_password,user_phone_number,role_id
            statement.setString(1,user.getUserStatus());
            statement.setString(2,user.getUserEmail());
            statement.setString(3,user.getUserFirstName());
            statement.setString(4,user.getUserLastName());
            statement.setString(5,user.getUserPassword());
            statement.setString(6,user.getUserPhoneNumber());
            statement.setInt(7,userRoleId);
            int rowsAffected = statement.executeUpdate();
            if(rowsAffected > 0) return "user added successfully";
        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return "user did not added";
    }




}
