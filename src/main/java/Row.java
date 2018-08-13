public class Row {

    String content;
    String client;
    String location;
    int consumption;

    public  Row(){

    }


    public Row(String content, String client, String location, int consumption){
        this.content = content;
        this.client = client;
        this.location = location;
        this.consumption = consumption;

    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getConsumption() {
        return consumption;
    }

    public void setConsumption(int consumption) {
        this.consumption = consumption;
    }
}
