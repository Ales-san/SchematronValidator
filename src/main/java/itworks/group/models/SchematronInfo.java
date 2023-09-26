package itworks.group.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class SchematronInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String medDocumentID;

    @Column(nullable = true)
    private LocalDateTime medDocumentUpdateDate;

    @Column(nullable = true)
    private String data;

    public SchematronInfo(String medDocumentID, String data) {
        this.medDocumentID = medDocumentID;
        this.data = data;
        this.medDocumentUpdateDate = LocalDateTime.now();
    }

    public SchematronInfo() {
    }

    public int getId() {
        return id;
    }

    public String getMedDocumentID() {
        return medDocumentID;
    }

    public String getData() {
        return data;
    }

    public void setMedDocumentID(String medDocumentID) {
        this.medDocumentID = medDocumentID;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getMedDocumentUpdateDate() {
        return medDocumentUpdateDate;
    }

    public void setMedDocumentUpdateDate(LocalDateTime medDocumentUpdateDate) {
        this.medDocumentUpdateDate = medDocumentUpdateDate;
    }
}
