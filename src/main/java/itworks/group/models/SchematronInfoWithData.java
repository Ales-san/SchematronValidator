package itworks.group.models;

import java.time.LocalDateTime;

public class SchematronInfoWithData extends SchematronInfo {
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    protected String data;

    public SchematronInfoWithData(SchematronInfo schematronInfo, String data)
    {
        super(schematronInfo.medDocumentID,
                schematronInfo.commitHash,
                schematronInfo.link,
                schematronInfo.regexPattern,
                schematronInfo.dataId,
                schematronInfo.medDocumentUpdateDate);
        this.data = data;
    }
}
