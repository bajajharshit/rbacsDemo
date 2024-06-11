package perfios.rbacs.Repository.Redis;

import lombok.Data;

import java.io.Serializable;

@Data
public class Access implements Serializable {
    private boolean canView;
    private boolean canEdit;
}
