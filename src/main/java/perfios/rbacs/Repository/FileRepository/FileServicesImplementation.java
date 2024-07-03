package perfios.rbacs.Repository.FileRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartFile;
import perfios.rbacs.Model.Users.User;
import perfios.rbacs.Model.Users.UserSearch;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.UserRepository.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class FileServicesImplementation implements FileServices{

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;



    @Autowired
    UserService userService;



    public List<String> addUserFromCSVFile(MultipartFile file) {

        List<String> headers = new ArrayList<>();
        headers.add("userFirstName");
        headers.add("userLastName");
        headers.add("userPassword");
        headers.add("userPhoneNumber");
        headers.add("userStatus");
        headers.add("userEmail");
        headers.add("userRoleId");
        headers.add("enabled");
        headers.add("isSuperAdmin");
        headers.add("shouldLoanAutoApply");
        headers.add("alternateUsername");

        // Convert the list to a String array
        String[] headerArray = headers.toArray(new String[0]);
        String actualHeadersRequired = "userFirstName | userLastName | userPassword | userPhoneNumber | userStatus | userEmail | userRoleId | enabled | isSuperAdmin | shouldLoanAutoApply | alternateUsername | ";
        String headerReceivedFromFile = "";

        try{
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            CSVParser parser =  CSVFormat.Builder.create(CSVFormat.EXCEL)
                    .setHeader(headerArray)
                    .build().parse(reader);

            Iterator<CSVRecord> record = parser.iterator();
            for(String header : record.next().values()) {
                headerReceivedFromFile += header + " | ";
            }
//            RbacsApplication.printString(actualHeadersRequired );
//            RbacsApplication.printString(headerReceivedFromFile);
            List<String> headerCheck = headersChecker(actualHeadersRequired,headerReceivedFromFile);
            if(headerCheck != null) return headerCheck;

            List<String> verificationList = new ArrayList<>();
            while(record.hasNext()) {
                User user = new User();
                try {
                    Boolean check = user.setFeildsFromMapForCsvFile(record.next().toMap());
//                    RbacsApplication.printString(user.toString());
                    if(check) verificationList.add(user.getUserEmail() + " -> " + userService.addNewUser(user));
                    else verificationList.add(user.getUserEmail() + " -> " + "INVALID VALUES PASSED");

                } catch (java.lang.Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            return verificationList;
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
       return null;
    }


    @Override
    public List<String > addUsersFromUploadedFile(MultipartFile multipartFile){
        if(multipartFile.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")){
            List<String> response = addUserFromXlsxFile(multipartFile);
            if(response == null || response.isEmpty()) {
                response = new ArrayList<>(1);
                response.add("Upload Failed, file is corrupt         " +
                        "Follow these steps to upload your file Successfully : -    " +
                        "1. Open your file in any Excel Viewer     " +
                        "2. Go to File -> Save As, select Excel 2007-365(.xlsx) option while saving     " +
                        "3. Try re-uploading your new saved as file. Thank you :)");
            }
            return response;
        }
        if(multipartFile.getContentType().equals("text/csv")){
//            RbacsApplication.printString("file is csv");
            List<String> response =  addUserFromCSVFile(multipartFile);
            if(response == null  || response.isEmpty()) {
                response = new ArrayList<>(1);
                response.add("Upload Failed, file is corrupt");
            }
            return response;
        }
        List<String > invalidFile = new ArrayList<>(1);
        invalidFile.add("Invalid File uploaded. Only .csv and .xlsx files are supported");
                return invalidFile;
    }

    public List<String> headersChecker(String actualHeadersRequired, String checkCorrectHeaders) {
        if (!checkCorrectHeaders.equals(actualHeadersRequired)) {
            String nonMatchingString = null;

            // Split headers into arrays
            String[] headers1 = checkCorrectHeaders.split("\\|");
            String[] headers2 = actualHeadersRequired.split("\\|");

            // Trim whitespaces from headers
            for (int i = 0; i < headers1.length; i++) {
                headers1[i] = headers1[i].trim();
            }
            for (int i = 0; i < headers2.length; i++) {
                headers2[i] = headers2[i].trim();
            }

            // Find non-matching headers
            StringBuilder nonMatching = new StringBuilder();
            for (String header1 : headers1) {
                boolean foundMatch = false;
                for (String header2 : headers2) {
                    if (header1.equals(header2)) {
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    if (nonMatching.length() > 0) {
                        nonMatching.append(" | ");
                    }
                    nonMatching.append(header1);
                }
            }

            nonMatchingString = nonMatching.toString();
            List<String> responseList = new ArrayList<>(1);
            responseList.add("FIRST ROW SHOULD BE A HEADER ROW WITH ONLY STRING VALUES IN FORMAT: " + actualHeadersRequired + " YOUR FILE HAS " + checkCorrectHeaders + "  CHANGE HEADER NEAR :- " + nonMatchingString);
            return responseList;
        }
        return null;
    }
    public List<String> addUserFromXlsxFile(MultipartFile multipartFile) {

        InputStream inputStream;
        Workbook workbook = null;
        try {
            inputStream = multipartFile.getInputStream();
            workbook = new XSSFWorkbook(inputStream);

        }catch (IOException e){
            System.err.println(e.getMessage());
        }
        if(workbook == null)return null;
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); //assuming first row is header
//let format for excel sheet is :-
//User Id | User First Name | User Last Name | User Password | User Phone Number | Alternate Username |
//User Status | User Email | User Role Name | User Role Id | Enabled | Is Super Admin | Should Loan Auto Apply |
//create a map for user and then make a user object and sent it to servic layer.

        String actualHeadersRequired = "userFirstName | userLastName | userPassword | userPhoneNumber | userStatus | userEmail | userRoleId | enabled | isSuperAdmin | shouldLoanAutoApply | alternateUsername | ";

        List<String> verificationList = new ArrayList<>();
        Row headerRow = sheet.getRow(0);
        if(headerRow == null) {
            List<String > responseList = new ArrayList<>(1);
            responseList.add("FIRST ROW SHOULD BE A HEADER ROW WITH ONLY STRING VALUES IN FORMAT: " + actualHeadersRequired);
            return responseList;
        }
        Iterator<Cell> headerName = headerRow.cellIterator();
        String checkCorrectHeaders = "";
        while(headerName.hasNext()){
            checkCorrectHeaders += headerName.next().getStringCellValue() + " | ";
        }

        List<String> headerCheck = headersChecker(actualHeadersRequired,checkCorrectHeaders);
        if(headerCheck != null) return headerCheck;
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            if(row == null) continue;
            Map<String,String> userMap = new HashMap<>();
            headerName = headerRow.cellIterator();
            Boolean check = true;
            for(Cell cell : row){
                String key = headerName.next().getStringCellValue();
                CellType cellType = cell.getCellType();
                if(cellType == CellType.BLANK || cellType == CellType._NONE){
                    check = false;
                    verificationList.add("Row " + row.getRowNum() + " is Invalid row");
                    break;
                }
                switch(cellType){
                    case NUMERIC -> {
                        userMap.put(key,String.valueOf(((long)cell.getNumericCellValue())));
                    }case STRING -> {
                        if(cell.getStringCellValue() == null || cell.getStringCellValue().isEmpty()){
                            check = false;
                            break;
                        }
                        userMap.put(key,cell.getStringCellValue());
                    }
                    case BOOLEAN -> {
                        userMap.put(key,String.valueOf(cell.getBooleanCellValue()));
                    }
                }
            }
            if(check) {
                User user = new User();
                Boolean toAdd = user.setFeildsFromMapForCsvFile(userMap);
                if(toAdd) {
                    verificationList.add(user.getUserEmail() + " -> " + userService.addNewUser(user));
                }else{
                     verificationList.add("Row number = " + row.getRowNum() + " failed due to invalid values passed");
                }
            }
        }
        return verificationList;
    }










    public String downloadUsersInXlsxFile(List<User> allUsers){


        Workbook workbook  = new XSSFWorkbook();
        Sheet sheet  = workbook.createSheet("All Users");


        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        //creating headers
        List<String> headerList = new ArrayList<>();
        headerList.add("User Id");
        headerList.add("User First Name");
        headerList.add("User Last Name");
        headerList.add("User Password");
        headerList.add("User Phone Number");
        headerList.add("Alternate Username");
        headerList.add("User Status");
        headerList.add("User Email");
        headerList.add("User Role Name");
        headerList.add("User Role Id");
        headerList.add("Enabled");
        headerList.add("Is Super Admin");
        headerList.add("Should Loan Auto Apply");



        int columnCount = 0;
        for(String headerCellValue : headerList){
            sheet.setColumnWidth(columnCount, 6000);
            Cell headerCell = header.createCell(columnCount++);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerCellValue);
        }


        int rowCount = 1;

        for(User user : allUsers){
            Row userRow = sheet.createRow(rowCount++);
            Map<Integer, Object> userMap = fillUserMap(user);
            columnCount = 0;
            while(userMap.containsKey(columnCount)){
                Cell userDataCell = userRow.createCell(columnCount);
                userDataCell.setCellValue(String.valueOf(userMap.get(columnCount)));
                columnCount++;
            }

        }

        String homeDestination = System.getProperty("user.home");
        homeDestination = System.getenv("HOME");

        String filePath = homeDestination + "/Desktop/SampleFile/AllUsers/";

        Path path = Paths.get(filePath);

// Check if directory exists, create it if not
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Directory created: " + path);
            } catch (Exception e) {
                System.err.println("Failed to create directory: " + path);
                e.printStackTrace();
            }
        }


        String fileName = "AllUserFile" + System.currentTimeMillis()/10000 + ".xlsx";
        File file = new File(filePath ,fileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        }catch (IOException e){
            return  "failed " + e.getMessage();
        }

        return "saved at " + file;
    }


    @Override
    public String getUserDetailsExcelFile() {
        return downloadUsersInXlsxFile(userService.getAllUsers());
    }


    @Override
    public String getUserDetailsInXlsxFileBasedOnSearch(UserSearch userSearch) {
        return downloadUsersInXlsxFile(userService.findUserByDifferentFeilds(userSearch));
    }



    @Override
    public byte[] getUserDetailsExcelFileInOtherSystem() throws IOException {
        // Create workbook and generate Excel file
//        Workbook workbook = createWorkbookWithData(); // Method to create your workbook with data
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        workbook.write(outputStream);
//        workbook.close();

//        return outputStream.toByteArray();

        return null;
    }

    public static Map<Integer,Object> fillUserMap(User user){
        int cc = 0;
        Map<Integer, Object> userMap = new HashMap<>();
        userMap.put(cc++, user.getUserId());
        userMap.put(cc++, user.getUserFirstName());
        userMap.put(cc++, user.getUserLastName());
        userMap.put(cc++, user.getUserPassword());
        userMap.put(cc++, user.getUserPhoneNumber());
        userMap.put(cc++, user.getAlternateUsername());
        userMap.put(cc++, user.getUserStatus());
        userMap.put(cc++, user.getUserEmail());
        userMap.put(cc++, user.getUserRoleName());
        userMap.put(cc++, user.getUserRoleId());
        userMap.put(cc++, user.getEnabled());
        userMap.put(cc++, user.getIsSuperAdmin());
        userMap.put(cc++, user.getShouldLoanAutoApply());

        return userMap;
    }




    //methord to send file in other person's system;
    @Override
    public File generateExcelFile(){
        Workbook workbook  = new XSSFWorkbook();
        Sheet sheet  = workbook.createSheet("All Users");


        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());

        //creating headers
        List<String> headerList = new ArrayList<>();
        headerList.add("User Id");
        headerList.add("User First Name");
        headerList.add("User Last Name");
        headerList.add("User Password");
        headerList.add("User Phone Number");
        headerList.add("Alternate Username");
        headerList.add("User Status");
        headerList.add("User Email");
        headerList.add("User Role Name");
        headerList.add("User Role Id");
        headerList.add("Enabled");
        headerList.add("Is Super Admin");
        headerList.add("Should Loan Auto Apply");



        int columnCount = 0;
        for(String headerCellValue : headerList){
            sheet.setColumnWidth(columnCount, 6000);
            Cell headerCell = header.createCell(columnCount++);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headerCellValue);
        }


        int rowCount = 1;

        List<User> allUsers = userService.getAllUsers();


        for(User user : allUsers){
            Row userRow = sheet.createRow(rowCount++);
            Map<Integer, Object> userMap = fillUserMap(user);
            columnCount = 0;
            while(userMap.containsKey(columnCount)){
                Cell userDataCell = userRow.createCell(columnCount);
                userDataCell.setCellValue(String.valueOf(userMap.get(columnCount)));
                columnCount++;
            }

        }

//        String homeDestination = System.getProperty("user.home");
//        homeDestination = System.getenv("HOME");
//
//        String filePath = homeDestination + "/Desktop/SampleFile/AllUsers/";
//
//        Path path = Paths.get(filePath);

// Check if directory exists, create it if not
//        if (!Files.exists(path)) {
//            try {
//                Files.createDirectories(path);
//                System.out.println("Directory created: " + path);
//            } catch (Exception e) {
//                System.err.println("Failed to create directory: " + path);
//                e.printStackTrace();
//            }
//        }


//        String fileName = "AllUserFile" + System.currentTimeMillis()/10000 + ".xlsx";
//        File file = new File(filePath ,fileName);
        String fileName = "AllUserFile" + System.currentTimeMillis()/10000 + ".xlsx";
        File file = new File(fileName);


        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        }catch (IOException e){
            return  null;
        }

        return file;
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body("An error occurred: " + ex.getMessage());
    }
}
