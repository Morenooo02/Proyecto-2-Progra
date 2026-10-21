package cr.ac.una.reservas.backend.migration;

import cr.ac.una.reservas.backend.data.Database;
import cr.ac.una.reservas.backend.security.PasswordUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Types;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class XmlToMySqlImporter {

    private static final Set<String> RESERVAS_OMITIDAS = Set.of(
            "RES-4-1788070850126",
            "RES-9-1788150838763",
            "RES-13-1788223647728"
    );

    private static final List<String> TABLAS = List.of(
            "usuarios",
            "funcionarios",
            "categorias",
            "recursos",
            "reservas",
            "reserva_recurso",
            "reserva_categoria"
    );

    private XmlToMySqlImporter() {
    }

    public static void main(String[] args) throws Exception {

        Path archivo = resolverArchivo(args);

        System.out.println("Archivo XML: " + archivo.toAbsolutePath());

        Document documento = cargarXml(archivo);
        Element raiz = documento.getDocumentElement();

        List<UsuarioXml> usuarios = leerUsuarios(raiz);
        List<FuncionarioXml> funcionarios = leerFuncionarios(raiz);
        List<CategoriaXml> categorias = leerCategorias(raiz);
        List<RecursoXml> recursos = leerRecursos(raiz);

        List<ReservaXml> reservasOriginales = leerReservas(raiz);

        List<ReservaXml> reservas = reservasOriginales.stream()
                .filter(r -> !RESERVAS_OMITIDAS.contains(r.id()))
                .toList();

        validarDatos(usuarios, funcionarios, categorias, recursos, reservas);

        System.out.println("Usuarios encontrados: " + usuarios.size());
        System.out.println("Funcionarios encontrados: " + funcionarios.size());
        System.out.println("Categorías encontradas: " + categorias.size());
        System.out.println("Recursos encontrados: " + recursos.size());
        System.out.println("Reservas que se importarán: " + reservas.size());
        System.out.println("Reservas omitidas: "
                + (reservasOriginales.size() - reservas.size()));

        migrar(usuarios, funcionarios, categorias, recursos, reservas);

        System.out.println();
        System.out.println("Migración completada correctamente.");
    }



    private static Path resolverArchivo(String[] args) {

        if (args.length > 0) {
            Path archivo = Path.of(args[0]).toAbsolutePath().normalize();

            if (!Files.isRegularFile(archivo)) {
                throw new IllegalArgumentException(
                        "No existe el archivo XML: " + archivo
                );
            }

            return archivo;
        }

        Path desdeRaiz = Path.of("data", "datos.xml");

        if (Files.isRegularFile(desdeRaiz)) {
            return desdeRaiz.toAbsolutePath().normalize();
        }

        Path desdeBackend = Path.of("..", "data", "datos.xml");

        if (Files.isRegularFile(desdeBackend)) {
            return desdeBackend.toAbsolutePath().normalize();
        }

        throw new IllegalArgumentException(
                "No se encontró data/datos.xml. "
                        + "Indica la ruta del archivo como argumento."
        );
    }

    private static Document cargarXml(Path archivo) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document documento =
                factory.newDocumentBuilder().parse(archivo.toFile());

        documento.getDocumentElement().normalize();

        return documento;
    }

    private static List<UsuarioXml> leerUsuarios(Element raiz) {

        List<UsuarioXml> resultado = new ArrayList<>();

        for (Element e : elementosDeGrupo(raiz, "usuarios", "usuario")) {

            resultado.add(new UsuarioXml(
                    texto(e, "id"),
                    texto(e, "clave"),
                    texto(e, "rol")
            ));
        }

        return resultado;
    }

    private static List<FuncionarioXml> leerFuncionarios(Element raiz) {

        List<FuncionarioXml> resultado = new ArrayList<>();

        for (Element e : elementosDeGrupo(
                raiz, "funcionarios", "funcionario")) {

            resultado.add(new FuncionarioXml(
                    texto(e, "id"),
                    texto(e, "nombre"),
                    textoOpcional(e, "telefono")
            ));
        }

        return resultado;
    }

    private static List<CategoriaXml> leerCategorias(Element raiz) {

        List<CategoriaXml> resultado = new ArrayList<>();

        for (Element e : elementosDeGrupo(
                raiz, "categorias", "categoria")) {

            resultado.add(new CategoriaXml(
                    texto(e, "id"),
                    texto(e, "descripcion")
            ));
        }

        return resultado;
    }

    private static List<RecursoXml> leerRecursos(Element raiz) {

        List<RecursoXml> resultado = new ArrayList<>();

        for (Element e : elementosDeGrupo(
                raiz, "recursos", "recurso")) {

            Element categoria = hijo(e, "categoria");

            if (categoria == null) {
                throw new IllegalArgumentException(
                        "Un recurso no tiene categoría."
                );
            }

            resultado.add(new RecursoXml(
                    texto(e, "id"),
                    texto(e, "descripcion"),
                    texto(categoria, "id")
            ));
        }

        return resultado;
    }

    private static List<ReservaXml> leerReservas(Element raiz) {

        List<ReservaXml> resultado = new ArrayList<>();

        for (Element e : elementosDeGrupo(
                raiz, "reservas", "reserva")) {

            Element funcionario = hijo(e, "funcionario");

            if (funcionario == null) {
                throw new IllegalArgumentException(
                        "Una reserva no tiene funcionario."
                );
            }

            List<String> recursos = hijos(e, "recursosAsignados")
                    .stream()
                    .map(r -> texto(r, "id"))
                    .toList();

            List<String> categorias = hijos(e, "categoriasSolicitadas")
                    .stream()
                    .map(c -> texto(c, "id"))
                    .toList();

            resultado.add(new ReservaXml(
                    texto(e, "id"),
                    texto(e, "actividad"),
                    LocalDate.parse(texto(e, "fecha")),
                    LocalTime.parse(texto(e, "horaInicio")),
                    LocalTime.parse(texto(e, "horaFin")),
                    texto(e, "estado"),
                    texto(funcionario, "id"),
                    recursos,
                    categorias
            ));
        }

        return resultado;
    }



    private static void validarDatos(
            List<UsuarioXml> usuarios,
            List<FuncionarioXml> funcionarios,
            List<CategoriaXml> categorias,
            List<RecursoXml> recursos,
            List<ReservaXml> reservas) {

        Map<String, UsuarioXml> usuariosPorId = new HashMap<>();
        Set<String> funcionariosIds = new HashSet<>();
        Set<String> categoriasIds = new HashSet<>();
        Set<String> recursosIds = new HashSet<>();
        Set<String> reservasIds = new HashSet<>();

        for (UsuarioXml u : usuarios) {

            validarIdentificador(u.id(), "usuario");

            if (u.clave() == null || u.clave().isBlank()) {
                throw new IllegalArgumentException(
                        "Un usuario tiene la clave vacía."
                );
            }

            if (!"ADMINISTRADOR".equals(u.rol())
                    && !"FUNCIONARIO".equals(u.rol())) {
                throw new IllegalArgumentException(
                        "Rol no válido para usuario: " + u.id()
                );
            }

            if (usuariosPorId.putIfAbsent(u.id(), u) != null) {
                throw new IllegalArgumentException(
                        "Identificador de usuario duplicado: " + u.id()
                );
            }
        }

        for (FuncionarioXml f : funcionarios) {

            validarIdentificador(f.id(), "funcionario");

            if (!funcionariosIds.add(f.id())) {
                throw new IllegalArgumentException(
                        "Funcionario duplicado: " + f.id()
                );
            }

            UsuarioXml usuario = usuariosPorId.get(f.id());

            if (usuario == null
                    || !"FUNCIONARIO".equals(usuario.rol())) {
                throw new IllegalArgumentException(
                        "El funcionario " + f.id()
                                + " no tiene cuenta compatible."
                );
            }

            if (f.nombre() == null || f.nombre().isBlank()
                    || f.nombre().length() > 120) {
                throw new IllegalArgumentException(
                        "Nombre inválido para funcionario: " + f.id()
                );
            }
        }

        for (UsuarioXml u : usuarios) {

            if ("FUNCIONARIO".equals(u.rol())
                    && !funcionariosIds.contains(u.id())) {
                throw new IllegalArgumentException(
                        "La cuenta FUNCIONARIO " + u.id()
                                + " no tiene funcionario asociado."
                );
            }
        }

        for (CategoriaXml c : categorias) {

            validarIdentificador(c.id(), "categoría");

            if (!categoriasIds.add(c.id())) {
                throw new IllegalArgumentException(
                        "Categoría duplicada: " + c.id()
                );
            }

            if (c.descripcion() == null || c.descripcion().isBlank()
                    || c.descripcion().length() > 150) {
                throw new IllegalArgumentException(
                        "Descripción inválida para categoría: " + c.id()
                );
            }
        }

        for (RecursoXml r : recursos) {

            validarIdentificador(r.id(), "recurso");

            if (!recursosIds.add(r.id())) {
                throw new IllegalArgumentException(
                        "Recurso duplicado: " + r.id()
                );
            }

            if (r.descripcion() == null || r.descripcion().isBlank()
                    || r.descripcion().length() > 150) {
                throw new IllegalArgumentException(
                        "Descripción inválida para recurso: " + r.id()
                );
            }

            if (!categoriasIds.contains(r.categoriaId())) {
                throw new IllegalArgumentException(
                        "El recurso " + r.id()
                                + " hace referencia a una categoría inexistente."
                );
            }
        }

        for (ReservaXml r : reservas) {

            validarIdentificador(r.id(), "reserva");

            if (!reservasIds.add(r.id())) {
                throw new IllegalArgumentException(
                        "Reserva duplicada: " + r.id()
                );
            }

            if (r.actividad() == null || r.actividad().isBlank()
                    || r.actividad().length() > 255) {
                throw new IllegalArgumentException(
                        "Actividad inválida en reserva: " + r.id()
                );
            }

            if (!funcionariosIds.contains(r.funcionarioId())) {
                throw new IllegalArgumentException(
                        "Funcionario inexistente en reserva: " + r.id()
                );
            }

            if (!"ACTIVA".equals(r.estado())
                    && !"CANCELADA".equals(r.estado())) {
                throw new IllegalArgumentException(
                        "Estado inválido en reserva: " + r.id()
                );
            }

            if (!r.horaFin().isAfter(r.horaInicio())) {
                throw new IllegalArgumentException(
                        "Horario inválido en reserva: " + r.id()
                );
            }

            validarSinDuplicados(r.recursos(), "recurso", r.id());
            validarSinDuplicados(r.categorias(), "categoría", r.id());

            for (String recursoId : r.recursos()) {
                if (!recursosIds.contains(recursoId)) {
                    throw new IllegalArgumentException(
                            "Recurso inexistente en reserva "
                                    + r.id() + ": " + recursoId
                    );
                }
            }

            for (String categoriaId : r.categorias()) {
                if (!categoriasIds.contains(categoriaId)) {
                    throw new IllegalArgumentException(
                            "Categoría inexistente en reserva "
                                    + r.id() + ": " + categoriaId
                    );
                }
            }
        }
    }

    private static void validarSinDuplicados(
            List<String> ids,
            String tipo,
            String reservaId) {

        if (new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException(
                    "La reserva " + reservaId
                            + " contiene " + tipo + " duplicados."
            );
        }
    }

    private static void validarIdentificador(String id, String tipo) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Identificador vacío para " + tipo + "."
            );
        }
    }



    private static void migrar(
            List<UsuarioXml> usuarios,
            List<FuncionarioXml> funcionarios,
            List<CategoriaXml> categorias,
            List<RecursoXml> recursos,
            List<ReservaXml> reservas) throws Exception {

        try (Connection connection = Database.getConnection()) {

            connection.setAutoCommit(false);

            try {
                comprobarBaseVacia(connection);

                insertarUsuarios(connection, usuarios);
                insertarFuncionarios(connection, funcionarios);
                insertarCategorias(connection, categorias);
                insertarRecursos(connection, recursos);
                insertarReservas(connection, reservas);

                connection.commit();

            } catch (Exception ex) {

                try {
                    connection.rollback();
                } catch (Exception rollbackError) {
                    ex.addSuppressed(rollbackError);
                }

                throw ex;
            }
        }
    }

    private static void comprobarBaseVacia(
            Connection connection) throws Exception {

        for (String tabla : TABLAS) {

            String sql = "SELECT COUNT(*) FROM " + tabla;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 var result = statement.executeQuery()) {

                result.next();

                long cantidad = result.getLong(1);

                if (cantidad > 0) {
                    throw new IllegalStateException(
                            "La tabla " + tabla + " ya tiene "
                                    + cantidad + " registros. "
                                    + "No se importó ningún dato. "
                                    + "Revisa la base antes de continuar."
                    );
                }
            }
        }
    }

    private static void insertarUsuarios(
            Connection connection,
            List<UsuarioXml> usuarios) throws Exception {

        String sql = """
                INSERT INTO usuarios (id, clave_hash, rol)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (UsuarioXml u : usuarios) {

                statement.setString(1, u.id());

                // La clave en texto del XML se transforma en un hash.
                statement.setString(2, PasswordUtil.hash(u.clave()));

                statement.setString(3, u.rol());
                statement.executeUpdate();
            }
        }
    }

    private static void insertarFuncionarios(
            Connection connection,
            List<FuncionarioXml> funcionarios) throws Exception {

        String sql = """
                INSERT INTO funcionarios (id, nombre, telefono)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (FuncionarioXml f : funcionarios) {

                statement.setString(1, f.id());
                statement.setString(2, f.nombre());

                if (f.telefono() == null) {
                    statement.setNull(3, Types.VARCHAR);
                } else {
                    statement.setString(3, f.telefono());
                }

                statement.executeUpdate();
            }
        }
    }

    private static void insertarCategorias(
            Connection connection,
            List<CategoriaXml> categorias) throws Exception {

        String sql = """
                INSERT INTO categorias (id, descripcion)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (CategoriaXml c : categorias) {

                statement.setString(1, c.id());
                statement.setString(2, c.descripcion());
                statement.executeUpdate();
            }
        }
    }

    private static void insertarRecursos(
            Connection connection,
            List<RecursoXml> recursos) throws Exception {

        String sql = """
                INSERT INTO recursos (id, descripcion, categoria_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (RecursoXml r : recursos) {

                statement.setString(1, r.id());
                statement.setString(2, r.descripcion());
                statement.setString(3, r.categoriaId());

                statement.executeUpdate();
            }
        }
    }

    private static void insertarReservas(
            Connection connection,
            List<ReservaXml> reservas) throws Exception {

        String sqlReserva = """
                INSERT INTO reservas
                    (id, actividad, fecha, hora_inicio, hora_fin,
                     estado, funcionario_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String sqlRecurso = """
                INSERT INTO reserva_recurso (reserva_id, recurso_id)
                VALUES (?, ?)
                """;

        String sqlCategoria = """
                INSERT INTO reserva_categoria (reserva_id, categoria_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement reservaStatement =
                     connection.prepareStatement(sqlReserva);
             PreparedStatement recursoStatement =
                     connection.prepareStatement(sqlRecurso);
             PreparedStatement categoriaStatement =
                     connection.prepareStatement(sqlCategoria)) {

            for (ReservaXml r : reservas) {

                reservaStatement.setString(1, r.id());
                reservaStatement.setString(2, r.actividad());
                reservaStatement.setDate(3, Date.valueOf(r.fecha()));
                reservaStatement.setTime(4, Time.valueOf(r.horaInicio()));
                reservaStatement.setTime(5, Time.valueOf(r.horaFin()));
                reservaStatement.setString(6, r.estado());
                reservaStatement.setString(7, r.funcionarioId());

                reservaStatement.executeUpdate();

                for (String recursoId : r.recursos()) {
                    recursoStatement.setString(1, r.id());
                    recursoStatement.setString(2, recursoId);
                    recursoStatement.executeUpdate();
                }

                for (String categoriaId : r.categorias()) {
                    categoriaStatement.setString(1, r.id());
                    categoriaStatement.setString(2, categoriaId);
                    categoriaStatement.executeUpdate();
                }
            }
        }
    }



    private static List<Element> elementosDeGrupo(
            Element raiz,
            String grupo,
            String elemento) {

        Element contenedor = hijo(raiz, grupo);

        if (contenedor == null) {
            throw new IllegalArgumentException(
                    "No se encontró el grupo XML: " + grupo
            );
        }

        return hijos(contenedor, elemento);
    }

    private static List<Element> hijos(
            Element padre,
            String nombre) {

        List<Element> resultado = new ArrayList<>();
        NodeList nodos = padre.getChildNodes();

        for (int i = 0; i < nodos.getLength(); i++) {

            Node nodo = nodos.item(i);

            if (nodo instanceof Element elemento
                    && nombre.equals(elemento.getTagName())) {

                resultado.add(elemento);
            }
        }

        return resultado;
    }

    private static Element hijo(Element padre, String nombre) {

        NodeList nodos = padre.getChildNodes();

        for (int i = 0; i < nodos.getLength(); i++) {

            Node nodo = nodos.item(i);

            if (nodo instanceof Element elemento
                    && nombre.equals(elemento.getTagName())) {

                return elemento;
            }
        }

        return null;
    }

    private static String texto(
            Element padre,
            String nombre) {

        Element elemento = hijo(padre, nombre);

        if (elemento == null) {
            throw new IllegalArgumentException(
                    "Falta el campo XML '" + nombre + "'."
            );
        }

        String valor = elemento.getTextContent().trim();

        if (valor.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo XML '" + nombre + "' está vacío."
            );
        }

        return valor;
    }

    private static String textoOpcional(
            Element padre,
            String nombre) {

        Element elemento = hijo(padre, nombre);

        if (elemento == null) {
            return null;
        }

        String valor = elemento.getTextContent().trim();

        return valor.isEmpty() ? null : valor;
    }



    private record UsuarioXml(
            String id,
            String clave,
            String rol) {
    }

    private record FuncionarioXml(
            String id,
            String nombre,
            String telefono) {
    }

    private record CategoriaXml(
            String id,
            String descripcion) {
    }

    private record RecursoXml(
            String id,
            String descripcion,
            String categoriaId) {
    }

    private record ReservaXml(
            String id,
            String actividad,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            String estado,
            String funcionarioId,
            List<String> recursos,
            List<String> categorias) {
    }
}