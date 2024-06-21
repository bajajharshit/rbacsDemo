package perfios.rbacs.Repository.FileRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import perfios.rbacs.Model.Users.UserSearch;

import java.util.List;

@Service
public interface FileServices {
    String getUserDetailsExcelFile();
    String getUserDetailsInXlsxFileBasedOnSearch(UserSearch userSearch);
    List<String > addUsersFromUploadedFile(MultipartFile multipartFile);
}
