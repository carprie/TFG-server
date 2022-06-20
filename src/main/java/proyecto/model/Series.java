package proyecto.model;

public class Series {
    public String name;
    public float value;

    public Series(String name, float value) {
        this.name = name;
        this.value = value > 0 ? value : 0;
    }
}
