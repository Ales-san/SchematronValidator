package itworks.group.models;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class SchematronData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected UUID id;

    @Column(nullable = true)
    protected String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public SchematronData()
    {
    }

    public SchematronData(String data)
    {
        this.data = data;
    }
}
