package itworks.group.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
public class SchematronInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected UUID id;

    @Column(nullable = false)
    protected String medDocumentID;

    @Column(nullable = true)
    protected String commitHash;

    @Column(nullable = true)
    protected String link;

    @Column(nullable = true)
    protected String regexPattern;

    @Column(nullable = true)
    protected LocalDateTime medDocumentUpdateDate;

    @Column(nullable = true)
    protected UUID dataId;

    public SchematronInfo(String medDocumentID, UUID dataId, LocalDateTime dateTime) {
        this.medDocumentID = medDocumentID;
        this.dataId = dataId;
        this.medDocumentUpdateDate = dateTime;
    }

    public SchematronInfo(String medDocumentID, UUID dataId)
    {
        this(medDocumentID, dataId, LocalDateTime.now());
    }

    public SchematronInfo(String medDocumentID,
                          String commitHash,
                          String link,
                          String regexPattern,
                          UUID dataId,
                          LocalDateTime dateTime) {
        this(medDocumentID, dataId, dateTime);
        this.commitHash = commitHash;
        this.link = link;
        this.regexPattern = regexPattern;
    }

    public SchematronInfo(String medDocumentID,
                          String commitHash,
                          String link,
                          String regexPattern,
                          UUID dataId) {
        this(medDocumentID, commitHash, link, regexPattern, dataId, LocalDateTime.now());
    }

    public SchematronInfo() {
    }

    public UUID getId() {
        return id;
    }

    public String getMedDocumentID() {
        return medDocumentID;
    }

    public UUID getDataId() {
        return dataId;
    }

    public void setMedDocumentID(String medDocumentID) {
        this.medDocumentID = medDocumentID;
    }

    public void setDataId(UUID dataId) {
        this.dataId = dataId;
    }

    public LocalDateTime getMedDocumentUpdateDate() {
        return medDocumentUpdateDate;
    }

    public void setMedDocumentUpdateDate(LocalDateTime medDocumentUpdateDate) {
        this.medDocumentUpdateDate = medDocumentUpdateDate;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }
}
