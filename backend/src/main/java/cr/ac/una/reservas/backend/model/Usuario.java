package cr.ac.una.reservas.backend.model;

public class Usuario {

    private String id;
    private String rol;

    public Usuario() {
    }

    public Usuario(String id, String rol) {
        this.id = id;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}