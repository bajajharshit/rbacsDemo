package perfios.rbacs.Controller.FileController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import perfios.rbacs.Model.Users.UserSearch;
import perfios.rbacs.RbacsApplication;
import perfios.rbacs.Repository.FileRepository.FileServices;

import javax.swing.plaf.ToolBarUI;
import java.io.File;
import java.io.FileInputStream;

@RestController
@CrossOrigin
public class FileController {

    @Autowired
    FileServices fileServices;



    @GetMapping("/file/upload")
    public ModelAndView fileUpload(){
        return new ModelAndView("fileupload");
    }


    @GetMapping("/all-users-download")
    public String getUserDetailsExcel(){
        return fileServices.getUserDetailsExcelFile();
        }

    @PostMapping("/file/upload")
    public ResponseEntity uploadNewUserXlxsFile(@RequestParam("file") MultipartFile file){
        if(file == null || file.isEmpty()) {
            return ResponseEntity.unprocessableEntity().body("File did not uploaded. please Re-upload!");
        }
        return ResponseEntity.ok(fileServices.addUsersFromUploadedFile(file));
    }

    @GetMapping("/download-users-based-on-search")
    public String getUserDetailsExcelBasedOnFilters(@RequestBody UserSearch userSearch){
        return fileServices.getUserDetailsInXlsxFileBasedOnSearch(userSearch);
    }



    @GetMapping("/file-download")
    public ResponseEntity<?> getUserDetailsExcelFile() {

        try {
            File file = fileServices.generateExcelFile();
            if(file == null) RbacsApplication.printString("file is null");
            if (file != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                FileSystemResource resource = new FileSystemResource(file);

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(file.length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
