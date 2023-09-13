package itworks.group.models;


import jakarta.persistence.*;

@Entity
public class SchematronInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private int medDocumentID;

    @Column(nullable = true)
    private String data;

    public SchematronInfo(int medDocumentID, String data) {
        this.medDocumentID = medDocumentID;
        this.data = data;
    }

    public SchematronInfo() {
    }

    public int getId() {
        return id;
    }

    public int getMedDocumentID() {
        return medDocumentID;
    }

    public String getData() {
        return data;
    }

    public void setMedDocumentID(int medDocumentID) {
        this.medDocumentID = medDocumentID;
    }

    public void setData(String data) {
        this.data = data;
    }
}
