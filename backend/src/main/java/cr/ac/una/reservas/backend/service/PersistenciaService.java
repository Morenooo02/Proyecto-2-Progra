package cr.ac.una.reservas.backend.service;

import cr.ac.una.reservas.backend.dao.CategoriaDAO;
import cr.ac.una.reservas.backend.dao.FuncionarioDAO;
import cr.ac.una.reservas.backend.dao.RecursoDAO;
import cr.ac.una.reservas.backend.dao.ReservaDAO;
import cr.ac.una.reservas.backend.dao.UsuarioDAO;

import cr.ac.una.reservas.backend.model.Categoria;
import cr.ac.una.reservas.backend.model.Funcionario;
import cr.ac.una.reservas.backend.model.Recurso;
import cr.ac.una.reservas.backend.model.Reserva;
import cr.ac.una.reservas.backend.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PersistenciaService {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final RecursoDAO recursoDAO = new RecursoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    // =========================
    // CATEGORÍAS
    // =========================

    public void registrarCategoria(Categoria categoria)
            throws SQLException {
        categoriaDAO.create(categoria);
    }

    public Optional<Categoria> buscarCategoria(String id)
            throws SQLException {
        return categoriaDAO.findById(id);
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.findAll();
    }

    public boolean modificarCategoria(Categoria categoria)
            throws SQLException {
        return categoriaDAO.update(categoria);
    }

    public boolean eliminarCategoria(String id) throws SQLException {
        return categoriaDAO.delete(id);
    }

    // =========================
    // RECURSOS
    // =========================

    public void registrarRecurso(Recurso recurso) throws SQLException {
        validarCategoriaDelRecurso(recurso);
        recursoDAO.create(recurso);
    }

    public Optional<Recurso> buscarRecurso(String id)
            throws SQLException {
        return recursoDAO.findById(id);
    }

    public List<Recurso> listarRecursos() throws SQLException {
        return recursoDAO.findAll();
    }

    public boolean modificarRecurso(Recurso recurso)
            throws SQLException {
        validarCategoriaDelRecurso(recurso);
        return recursoDAO.update(recurso);
    }

    public boolean eliminarRecurso(String id) throws SQLException {
        return recursoDAO.delete(id);
    }

    private void validarCategoriaDelRecurso(Recurso recurso)
            throws SQLException {

        if (recurso == null || recurso.getCategoria() == null) {
            throw new IllegalArgumentException(
                    "El recurso debe tener una categoría."
            );
        }

        String categoriaId = recurso.getCategoria().getId();

        if (categoriaId == null || categoriaId.isBlank()) {
            throw new IllegalArgumentException(
                    "El identificador de la categoría es obligatorio."
            );
        }

        if (categoriaDAO.findById(categoriaId).isEmpty()) {
            throw new IllegalArgumentException(
                    "La categoría indicada no existe."
            );
        }
    }

    // =========================
    // USUARIOS: LOGIN Y CLAVE
    // =========================

    public void registrarAdministrador(String id, String clave)
            throws SQLException {
        usuarioDAO.create(id, clave, "ADMINISTRADOR");
    }

    public Optional<Usuario> iniciarSesion(String id, String clave)
            throws SQLException {
        return usuarioDAO.authenticate(id, clave);
    }

    public boolean cambiarClave(
            String id,
            String claveActual,
            String claveNueva
    ) throws SQLException {
        return usuarioDAO.changePassword(
                id,
                claveActual,
                claveNueva
        );
    }

    public Optional<Usuario> buscarUsuario(String id)
            throws SQLException {
        return usuarioDAO.findById(id);
    }

    public boolean existeUsuario(String id) throws SQLException {
        return usuarioDAO.existsById(id);
    }

    // =========================
    // FUNCIONARIOS
    // =========================

    public void registrarFuncionario(
            Funcionario funcionario,
            String claveInicial
    ) throws SQLException {
        funcionarioDAO.create(funcionario, claveInicial);
    }

    public Optional<Funcionario> buscarFuncionario(String id)
            throws SQLException {
        return funcionarioDAO.findById(id);
    }

    public List<Funcionario> listarFuncionarios()
            throws SQLException {
        return funcionarioDAO.findAll();
    }

    public boolean modificarFuncionario(Funcionario funcionario)
            throws SQLException {
        return funcionarioDAO.update(funcionario);
    }

    public boolean eliminarFuncionario(String id)
            throws SQLException {
        return funcionarioDAO.delete(id);
    }

    public boolean existeFuncionario(String id)
            throws SQLException {
        return funcionarioDAO.existsById(id);
    }

    // =========================
    // RESERVAS
    // =========================

    public void registrarReserva(Reserva reserva) throws SQLException {
        validarReferenciasReserva(reserva);
        reservaDAO.create(reserva);
    }

    public Optional<Reserva> buscarReserva(String id)
            throws SQLException {
        return reservaDAO.findById(id);
    }

    public List<Reserva> listarReservas() throws SQLException {
        return reservaDAO.findAll();
    }

    public boolean modificarReserva(Reserva reserva)
            throws SQLException {
        validarReferenciasReserva(reserva);
        return reservaDAO.update(reserva);
    }

    public boolean eliminarReserva(String id) throws SQLException {
        return reservaDAO.delete(id);
    }

    public boolean cancelarReserva(String id) throws SQLException {
        return reservaDAO.cancel(id);
    }

    private void validarReferenciasReserva(Reserva reserva)
            throws SQLException {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "La reserva no puede ser nula."
            );
        }

        if (reserva.getFuncionario() == null
                || reserva.getFuncionario().getId() == null
                || reserva.getFuncionario().getId().isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva debe tener un funcionario válido."
            );
        }

        String funcionarioId = reserva.getFuncionario().getId();

        if (funcionarioDAO.findById(funcionarioId).isEmpty()) {
            throw new IllegalArgumentException(
                    "El funcionario indicado no existe."
            );
        }

        if (reserva.getRecursosAsignados() == null
                || reserva.getCategoriasSolicitadas() == null) {
            throw new IllegalArgumentException(
                    "Las listas de recursos y categorías son obligatorias."
            );
        }

        for (Recurso recurso : reserva.getRecursosAsignados()) {
            if (recurso == null
                    || recurso.getId() == null
                    || recurso.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "La reserva contiene un recurso inválido."
                );
            }

            if (recursoDAO.findById(recurso.getId()).isEmpty()) {
                throw new IllegalArgumentException(
                        "Uno de los recursos asignados no existe."
                );
            }
        }

        for (Categoria categoria : reserva.getCategoriasSolicitadas()) {
            if (categoria == null
                    || categoria.getId() == null
                    || categoria.getId().isBlank()) {
                throw new IllegalArgumentException(
                        "La reserva contiene una categoría inválida."
                );
            }

            if (categoriaDAO.findById(categoria.getId()).isEmpty()) {
                throw new IllegalArgumentException(
                        "Una de las categorías solicitadas no existe."
                );
            }
        }
    }
}