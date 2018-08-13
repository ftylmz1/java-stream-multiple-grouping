public class Thing{

    public Thing(int number) {

        this.Id  = number;
        this.name = "Name " + number;
        this.client = "Client " + number;

    }


    int Id;
    String name;
    String client;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}