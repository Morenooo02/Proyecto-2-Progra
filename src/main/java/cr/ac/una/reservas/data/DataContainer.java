package cr.ac.una.reservas.data;

import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.Funcionario;
import cr.ac.una.reservas.logic.Recurso;
import cr.ac.una.reservas.logic.Reserva;
import cr.ac.una.reservas.logic.Usuario;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "datos")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataContainer {
    @XmlElementWrapper(name = "usuarios")
    @XmlElement(name = "usuario")
    private List<Usuario> usuarios = new ArrayList<>();

    @XmlElementWrapper(name = "funcionarios")
    @XmlElement(name = "funcionario")
    private List<Funcionario> funcionarios = new ArrayList<>();

    @XmlElementWrapper(name = "categorias")
    @XmlElement(name = "categoria")
    private List<Categoria> categorias = new ArrayList<>();

    @XmlElementWrapper(name = "recursos")
    @XmlElement(name = "recurso")
    private List<Recurso> recursos = new ArrayList<>();

    @XmlElementWrapper(name = "reservas")
    @XmlElement(name = "reserva")
    private List<Reserva> reservas = new ArrayList<>();

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }
}
