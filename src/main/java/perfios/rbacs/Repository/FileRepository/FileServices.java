package perfios.rbacs.Repository.FileRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import perfios.rbacs.Model.Users.UserSearch;

import java.util.List;

@Service
public interface FileServices {
    List<String> addUserFromCSVFile(MultipartFile file);
    String getUserDetailsExcelFile();
    List<String> addUserFromXlxsFile(MultipartFile file);
    String getUserDetailsInXlsxFileBasedOnSearch(UserSearch userSearch);
}
