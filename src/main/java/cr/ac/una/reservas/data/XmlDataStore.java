package cr.ac.una.reservas.data;

import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.Funcionario;
import cr.ac.una.reservas.logic.Recurso;
import cr.ac.una.reservas.logic.Rol;
import cr.ac.una.reservas.logic.Usuario;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class XmlDataStore {
    public static final String RUTA_POR_DEFECTO = "data/datos.xml";
    /** Permite redirigir el XML (lo usan las pruebas para no tocar el archivo real). */
    public static final String PROPIEDAD_RUTA = "reservas.data.file";

    private String FILE_PATH;

    public XmlDataStore() {
        this(System.getProperty(PROPIEDAD_RUTA, RUTA_POR_DEFECTO));
    }

    public XmlDataStore(String filePath) {
        this.FILE_PATH = filePath;
    }

    public DataContainer cargarDatos() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                DataContainer inicial = crearDatosIniciales();
                guardarDatos(inicial);
                return inicial;
            }
            JAXBContext context = JAXBContext.newInstance(DataContainer.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            DataContainer container = (DataContainer) unmarshaller.unmarshal(file);
            enlazarReferencias(container);
            return container;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el XML: " + e.getMessage(), e);
        }
    }

    public void guardarDatos(DataContainer container) {
        try {
            File file = new File(FILE_PATH);
            Path parent = file.toPath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            JAXBContext context = JAXBContext.newInstance(DataContainer.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(container, file);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el XML: " + e.getMessage(), e);
        }
    }

    private void enlazarReferencias(DataContainer container) {
        for (Recurso recurso : container.getRecursos()) {
            if (recurso.getCategoria() != null) {
                Categoria encontrada = buscarCategoria(container, recurso.getCategoria().getId());
                if (encontrada != null) {
                    recurso.setCategoria(encontrada);
                }
            }
        }
        for (var reserva : container.getReservas()) {
            if (reserva.getFuncionario() != null) {
                Funcionario f = buscarFuncionario(container, reserva.getFuncionario().getId());
                if (f != null) {
                    reserva.setFuncionario(f);
                }
            }
            enlazarRecursos(container, reserva.getRecursosAsignados());
            enlazarCategorias(container, reserva.getCategoriasSolicitadas());
        }
    }

    private void enlazarRecursos(DataContainer container, List<Recurso> lista) {
        for (int i = 0; i < lista.size(); i++) {
            Recurso encontrado = buscarRecurso(container, lista.get(i).getId());
            if (encontrado != null) {
                lista.set(i, encontrado);
            }
        }
    }

    private void enlazarCategorias(DataContainer container, List<Categoria> lista) {
        for (int i = 0; i < lista.size(); i++) {
            Categoria encontrada = buscarCategoria(container, lista.get(i).getId());
            if (encontrada != null) {
                lista.set(i, encontrada);
            }
        }
    }

    private Recurso buscarRecurso(DataContainer container, String id) {
        for (Recurso r : container.getRecursos()) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }
    private Categoria buscarCategoria(DataContainer container, String id) {
        for (Categoria c : container.getCategorias()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private Funcionario buscarFuncionario(DataContainer container, String id) {
        for (Funcionario f : container.getFuncionarios()) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    private DataContainer crearDatosIniciales() {
        DataContainer datos = new DataContainer();
        datos.getUsuarios().add(new Usuario("admin", "admin", Rol.ADMINISTRADOR));
        datos.getUsuarios().add(new Usuario("100", "100", Rol.FUNCIONARIO));
        datos.getFuncionarios().add(new Funcionario("100", "Ana Perez", "8888-1111"));

        Categoria salas = new Categoria("1", "Sala para 10 personas");
        Categoria laptops = new Categoria("2", "Laptop windows 11");
        Categoria proyectores = new Categoria("3", "Proyector");
        datos.getCategorias().add(salas);
        datos.getCategorias().add(laptops);
        datos.getCategorias().add(proyectores);

        datos.getRecursos().add(new Recurso("SALA-1", "Sala 1 primer piso", salas));
        datos.getRecursos().add(new Recurso("SALA-2", "Sala 2 segundo piso", salas));
        datos.getRecursos().add(new Recurso("LT-238715", "Laptop #238715", laptops));
        datos.getRecursos().add(new Recurso("LT-238716", "Laptop #238716", laptops));
        datos.getRecursos().add(new Recurso("PR-01", "Proyector sala magna", proyectores));
        return datos;
    }

    public String getFilePath() {
        return FILE_PATH;
    }
}
