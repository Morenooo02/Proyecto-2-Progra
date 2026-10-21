package cr.ac.una.reservas.logic;

import cr.ac.una.reservas.data.LocalDateAdapter;
import cr.ac.una.reservas.data.LocalTimeAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Reserva extends ModeloBase{
    private String id;
    private String actividad;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fecha;
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaInicio;
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaFin;
    private EstadoReserva estado;
    private Funcionario funcionario;
    private List<Recurso> recursosAsignados = new ArrayList<>();
    private List<Categoria> categoriasSolicitadas = new ArrayList<>();

    public Reserva() {
    }

    public boolean solapaCon(LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (this.fecha == null || fecha == null || !this.fecha.equals(fecha)) {
            return false;
        }
        if (horaInicio == null || horaFin == null || inicio == null || fin == null) {
            return false;
        }
        return inicio.isBefore(horaFin) && fin.isAfter(horaInicio);
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

    public void setRecursosAsignados(List<Recurso> recursos) {
        this.recursosAsignados = recursos;
    }

    public List<Categoria> getCategoriasSolicitadas() {
        return categoriasSolicitadas;
    }

    public void setCategoriasSolicitadas(List<Categoria> categorias) {
        this.categoriasSolicitadas = categorias;
    }
}
