package generate;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * blog
 * @author 
 */
@Data
public class Blog implements Serializable {
    private String id;

    private Date date;

    private String title;

    private String filename;

    private static final long serialVersionUID = 1L;
}