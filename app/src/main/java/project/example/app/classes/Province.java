package project.example.app.classes;

public class Province {
    private String id;
    private String name;

    // Default constructor required for calls to DataSnapshot.getValue(Province.class)
    public Province() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
