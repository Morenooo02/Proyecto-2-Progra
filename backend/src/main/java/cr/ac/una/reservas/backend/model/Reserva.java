package cr.ac.una.reservas.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva {

    private String id;
    private String actividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoReserva estado;
    private Funcionario funcionario;
    private List<Recurso> recursosAsignados;
    private List<Categoria> categoriasSolicitadas;

    public Reserva() {
        this.estado = EstadoReserva.ACTIVA;
        this.recursosAsignados = new ArrayList<>();
        this.categoriasSolicitadas = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<Recurso> getRecursosAsignados() {
        return recursosAsignados;
    }

    public void setRecursosAsignados(List<Recurso> recursosAsignados) {
        this.recursosAsignados = recursosAsignados == null
                ? new ArrayList<>()
                : new ArrayList<>(recursosAsignados);
    }

    public List<Categoria> getCategoriasSolicitadas() {
        return categoriasSolicitadas;
    }

    public void setCategoriasSolicitadas(
            List<Categoria> categoriasSolicitadas) {

        this.categoriasSolicitadas = categoriasSolicitadas == null
                ? new ArrayList<>()
                : new ArrayList<>(categoriasSolicitadas);
    }
}