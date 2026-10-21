package cr.ac.una.reservas.logic;

import cr.ac.una.reservas.data.DataContainer;
import cr.ac.una.reservas.data.PdfReportGenerator;
import cr.ac.una.reservas.data.XmlDataStore;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cr.ac.una.reservas.logic.ai.ReservaExtraccion;
import cr.ac.una.reservas.logic.ai.ReservaExtractorService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.stream.Collectors;

public class ServiceModel {
    private static final int HORA_INICIO_MATRIZ = 0;
    private static final int HORA_FIN_MATRIZ = 23;
    private static ServiceModel instance;
    private DataContainer dataContainer;
    private XmlDataStore xmlStore;
    private PdfReportGenerator pdfGenerator;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private ServiceModel() {
        xmlStore = new XmlDataStore();
        pdfGenerator = new PdfReportGenerator();
        dataContainer = xmlStore.cargarDatos();
    }

    public static ServiceModel getInstance() {
        if (instance == null) {
            instance = new ServiceModel();
        }
        return instance;
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public Usuario login(String id, String clave) {
        if (id == null || clave == null) {
            return null;
        }
        for (Usuario u : dataContainer.getUsuarios()) {
            if (id.equals(u.getId()) && clave.equals(u.getClave())) {
                return u;
            }
        }
        return null;
    }

    public boolean cambiarClave(String id, String claveActual, String claveNueva) {
        Usuario u = buscarUsuario(id);
        if (u == null || claveNueva == null || claveNueva.isBlank()) {
            return false;
        }
        if (!u.getClave().equals(claveActual)) {
            return false;
        }
        u.setClave(claveNueva);
        guardar();
        return true;
    }

    public boolean agregarFuncionario(Funcionario f) {
        if (f == null || vacio(f.getId()) || vacio(f.getNombre()) || vacio(f.getTelefono())) {
            return false;
        }
        if (buscarUsuario(f.getId()) != null || buscarFuncionario(f.getId()) != null) {
            return false;
        }
        dataContainer.getUsuarios().add(new Usuario(f.getId(), f.getId(), Rol.FUNCIONARIO));
        dataContainer.getFuncionarios().add(f);
        guardar();
        pcs.firePropertyChange("funcionarios", null, dataContainer.getFuncionarios());
        return true;
    }

    public boolean modificarFuncionario(Funcionario f) {
        Funcionario actual = buscarFuncionario(f.getId());
        if (actual == null || vacio(f.getNombre()) || vacio(f.getTelefono())) {
            return false;
        }
        actual.setNombre(f.getNombre());
        actual.setTelefono(f.getTelefono());
        guardar();
        pcs.firePropertyChange("funcionarios", null, dataContainer.getFuncionarios());
        return true;
    }

    public boolean eliminarFuncionario(String id) {
        Funcionario f = buscarFuncionario(id);
        if (f == null) {
            return false;
        }
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getEstado() == EstadoReserva.ACTIVA && r.getFuncionario() != null
                    && id.equals(r.getFuncionario().getId()) && r.getFecha() != null
                    && !r.getFecha().isBefore(LocalDate.now())) {
                return false;
            }
        }
        dataContainer.getFuncionarios().remove(f);
        Usuario u = buscarUsuario(id);
        if (u != null) {
            dataContainer.getUsuarios().remove(u);
        }
        guardar();
        pcs.firePropertyChange("funcionarios", null, dataContainer.getFuncionarios());
        return true;
    }

    public List<Funcionario> buscarFuncionarios(String id, String nombre) {
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : dataContainer.getFuncionarios()) {
            boolean okId = vacio(id) || f.getId().toLowerCase().contains(id.toLowerCase());
            boolean okNombre = vacio(nombre) || (f.getNombre() != null
                    && f.getNombre().toLowerCase().contains(nombre.toLowerCase()));
            if (okId && okNombre) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    public boolean agregarCategoria(Categoria c) {
        if (c == null || vacio(c.getDescripcion())) {
            return false;
        }
        c.setId(siguienteIdCategoria());
        dataContainer.getCategorias().add(c);
        guardar();
        pcs.firePropertyChange("categorias", null, dataContainer.getCategorias());
        return true;
    }

    public boolean modificarCategoria(Categoria c) {
        Categoria actual = buscarCategoria(c.getId());
        if (actual == null || vacio(c.getDescripcion())) {
            return false;
        }
        actual.setDescripcion(c.getDescripcion());
        guardar();
        pcs.firePropertyChange("categorias", null, dataContainer.getCategorias());


        return true;
    }

    public boolean eliminarCategoria(String id) {
        Categoria c = buscarCategoria(id);
        if (c == null) {
            return false;
        }
        for (Recurso r : dataContainer.getRecursos()) {
            if (r.getCategoria() != null && id.equals(r.getCategoria().getId())) {
                return false;
            }
        }
        dataContainer.getCategorias().remove(c);
        guardar();
        pcs.firePropertyChange("categorias", null, dataContainer.getCategorias());
        return true;
    }

    public List<Categoria> buscarCategorias(String descripcion) {
        List<Categoria> resultado = new ArrayList<>();
        for (Categoria c : dataContainer.getCategorias()) {
            if (vacio(descripcion) || (c.getDescripcion() != null
                    && c.getDescripcion().toLowerCase().contains(descripcion.toLowerCase()))) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public boolean agregarRecurso(Recurso r) {
        if (r == null || vacio(r.getId()) || vacio(r.getDescripcion()) || r.getCategoria() == null) {
            return false;
        }
        if (buscarRecurso(r.getId()) != null) {
            return false;
        }
        Categoria cat = buscarCategoria(r.getCategoria().getId());
        if (cat == null) {
            return false;
        }
        r.setCategoria(cat);
        dataContainer.getRecursos().add(r);
        guardar();
        pcs.firePropertyChange("recursos", null, dataContainer.getRecursos());
        return true;
    }

    public boolean modificarRecurso(Recurso r) {
        Recurso actual = buscarRecurso(r.getId());
        if (actual == null || vacio(r.getDescripcion()) || r.getCategoria() == null) {
            return false;
        }
        Categoria cat = buscarCategoria(r.getCategoria().getId());
        if (cat == null) {
            return false;
        }
        actual.setDescripcion(r.getDescripcion());
        actual.setCategoria(cat);
        guardar();
        pcs.firePropertyChange("recursos", null, dataContainer.getRecursos());
        return true;
    }

    public boolean eliminarRecurso(String id) {
        Recurso rec = buscarRecurso(id);
        if (rec == null) {
            return false;
        }
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getEstado() == EstadoReserva.ACTIVA && r.getRecursosAsignados() != null) {
                for (Recurso as : r.getRecursosAsignados()) {
                    if (id.equals(as.getId())) {
                        return false;
                    }
                }
            }
        }
        dataContainer.getRecursos().remove(rec);
        guardar();
        pcs.firePropertyChange("recursos", null, dataContainer.getRecursos());
        return true;
    }

    public List<Recurso> buscarRecursosPorCategoria(String catId) {
        List<Recurso> resultado = new ArrayList<>();
        for (Recurso r : dataContainer.getRecursos()) {
            if (vacio(catId) || (r.getCategoria() != null && catId.equals(r.getCategoria().getId()))) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public ReservaExtraccion extraerReserva(String frase) {
        OpenAiChatModel aiModel = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();

        ReservaExtractorService aiService =
                AiServices.create(ReservaExtractorService.class, aiModel);

        String listaCategorias = formatearCategorias(
                dataContainer.getCategorias()
                        .stream()
                        .map(Categoria::getDescripcion)
                        .collect(Collectors.toList())
        );

        return aiService.extraer(
                frase,
                listaCategorias,
                LocalDate.now().toString()
        );
    }

    public boolean registrarReserva(Reserva r, List<String> catIds) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        if (r == null || vacio(r.getActividad()) || r.getFecha() == null
                || r.getHoraInicio() == null || r.getHoraFin() == null
                || r.getFecha().isBefore(hoy)
                || (r.getFecha().equals(hoy) && !r.getHoraInicio().isAfter(ahora))
                || r.getHoraFin().isBefore(r.getHoraInicio()) || r.getHoraFin().equals(r.getHoraInicio())
                || catIds == null || catIds.isEmpty() || r.getFuncionario() == null) {
            return false;
        }
        List<Recurso> asignados = new ArrayList<>();
        List<Categoria> solicitadas = new ArrayList<>();
        for (String catId : catIds) {
            Categoria cat = buscarCategoria(catId);
            if (cat == null) {
                return false;
            }
            solicitadas.add(cat);
            Recurso libre = primerRecursoLibre(catId, r.getFecha(), r.getHoraInicio(), r.getHoraFin());
            if (libre == null) {
                return false;
            }
            asignados.add(libre);
        }
        r.setId("RES-" + (dataContainer.getReservas().size() + 1) + "-" + System.currentTimeMillis());
        r.setEstado(EstadoReserva.ACTIVA);
        r.setRecursosAsignados(asignados);
        r.setCategoriasSolicitadas(solicitadas);
        dataContainer.getReservas().add(r);
        guardar();
        pcs.firePropertyChange("reservas", null, dataContainer.getReservas());
        return true;
    }

    public List<String> categoriasNoDisponibles(LocalDate fecha, LocalTime inicio, LocalTime fin, List<String> catIds) {
        List<String> noDisp = new ArrayList<>();
        if (fecha == null || inicio == null || fin == null || catIds == null) {
            return noDisp;
        }
        for (String catId : catIds) {
            if (primerRecursoLibre(catId, fecha, inicio, fin) == null) {
                Categoria c = buscarCategoria(catId);
                noDisp.add(c == null ? catId : c.getDescripcion());
            }
        }
        return noDisp;
    }

    public boolean cancelarReserva(String reservaId) {
        Reserva r = buscarReserva(reservaId);
        if (r == null || r.getEstado() != EstadoReserva.ACTIVA) {
            return false;
        }
        if (r.getFecha() == null || r.getFecha().isBefore(LocalDate.now())) {
            return false;
        }
        r.setEstado(EstadoReserva.CANCELADA);
        guardar();
        pcs.firePropertyChange("reservas", null, dataContainer.getReservas());
        return true;
    }

    public List<Reserva> obtenerReservasPorFuncionario(String funcId) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getFuncionario() != null && funcId.equals(r.getFuncionario().getId())) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public Object[][] obtenerMatrizCalendarizacion(LocalDate fecha, String catId) {
        List<Recurso> recursos = buscarRecursosPorCategoria(catId);
        int filas = (HORA_FIN_MATRIZ - HORA_INICIO_MATRIZ) + 1;
        Object[][] matriz = new Object[filas][recursos.size() + 1];
        for (int i = 0; i < filas; i++) {
            int hora = HORA_INICIO_MATRIZ + i;
            matriz[i][0] = String.format("%02d:00", hora);
            LocalTime slot = LocalTime.of(hora, 0);
            LocalTime slotFin = hora == HORA_FIN_MATRIZ ? LocalTime.MAX : slot.plusHours(1);
            for (int j = 0; j < recursos.size(); j++) {
                Recurso rec = recursos.get(j);
                matriz[i][j + 1] = textoReservaEnSlot(rec, fecha, slot, slotFin);
            }
        }
        return matriz;
    }

    public List<Recurso> recursosDeCategoria(String catId) {
        return buscarRecursosPorCategoria(catId);
    }

    public Object[][] obtenerMatrizActividades(LocalDate semanaRef) {
        LocalDate lunes = semanaRef.with(DayOfWeek.MONDAY);
        int filas = (HORA_FIN_MATRIZ - HORA_INICIO_MATRIZ) + 1;
        Object[][] matriz = new Object[filas][8];
        for (int i = 0; i < filas; i++) {
            int hora = HORA_INICIO_MATRIZ + i;
            matriz[i][0] = String.format("%02d:00", hora);
            LocalTime slot = LocalTime.of(hora, 0);
            LocalTime slotFin = hora == HORA_FIN_MATRIZ ? LocalTime.MAX : slot.plusHours(1);
            for (int d = 0; d < 7; d++) {
                LocalDate dia = lunes.plusDays(d);
                matriz[i][d + 1] = textoActividadesEnSlot(dia, slot, slotFin);
            }
        }
        return matriz;
    }

    public Map<String, Integer> obtenerEstadisticasRecursos(LocalDate desde, LocalDate hasta) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Reserva r : dataContainer.getReservas()) {
            if (!reservaEnRango(r, desde, hasta)) {
                continue;
            }
            if (r.getRecursosAsignados() == null) {
                continue;
            }
            for (Recurso rec : r.getRecursosAsignados()) {
                String desc = rec.getCategoria() != null ? rec.getCategoria().getDescripcion() : "Sin categoria";
                mapa.put(desc, mapa.getOrDefault(desc, 0) + 1);
            }
        }
        return mapa;
    }

    public Map<String, Integer> obtenerEstadisticasActividades(LocalDate desde, LocalDate hasta) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            return mapa;
        }
        WeekFields wf = WeekFields.ISO;


        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            mapa.putIfAbsent(claveSemana(cursor, wf), 0);
            cursor = cursor.plusWeeks(1);
        }
        mapa.putIfAbsent(claveSemana(hasta, wf), 0); // asegura que la última semana quede incluida


        for (Reserva r : dataContainer.getReservas()) {
            if (!reservaEnRango(r, desde, hasta) || r.getFecha() == null) {
                continue;
            }
            String clave = claveSemana(r.getFecha(), wf);
            mapa.put(clave, mapa.getOrDefault(clave, 0) + 1);
        }
        return mapa;
    }

    private String claveSemana(LocalDate fecha, WeekFields wf) {
        int semana = fecha.get(wf.weekOfWeekBasedYear());
        int anio = fecha.get(wf.weekBasedYear());
        return "Semana " + semana + " - " + anio;
    }

    public void exportarPDF(String titulo, String[] columnas, Object[][] datos, String ruta) {
        pdfGenerator.generarReportePDF(titulo, columnas, datos, ruta);
    }
    public void exportarPdfEstadisticas(Map<String, Integer> recursos, byte[] graficoRecursos,
                                        Map<String, Integer> actividades, byte[] graficoActividades,
                                        String ruta) {
        List<PdfReportGenerator.Seccion> secciones = new ArrayList<>();
        secciones.add(new PdfReportGenerator.Seccion("Uso por categoria de recurso",
                new String[]{"Categoria", "Cantidad"}, mapaADatos(recursos), graficoRecursos));
        secciones.add(new PdfReportGenerator.Seccion("Actividades por semana",
                new String[]{"Semana", "Cantidad"}, mapaADatos(actividades), graficoActividades));
        pdfGenerator.generarReporteConSecciones("Estadisticas", secciones, ruta);
    }

    private Object[][] mapaADatos(Map<String, Integer> mapa) {
        Object[][] datos = new Object[mapa.size()][2];
        int i = 0;
        for (Map.Entry<String, Integer> e : mapa.entrySet()) {
            datos[i][0] = e.getKey();
            datos[i][1] = e.getValue();
            i++;
        }
        return datos;
    }

    public Funcionario buscarFuncionario(String id) {
        for (Funcionario f : dataContainer.getFuncionarios()) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    public Categoria buscarCategoria(String id) {
        for (Categoria c : dataContainer.getCategorias()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private Usuario buscarUsuario(String id) {
        for (Usuario u : dataContainer.getUsuarios()) {
            if (u.getId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    private Recurso buscarRecurso(String id) {
        for (Recurso r : dataContainer.getRecursos()) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    private Reserva buscarReserva(String id) {
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

    private Recurso primerRecursoLibre(String catId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        for (Recurso rec : dataContainer.getRecursos()) {
            if (rec.getCategoria() == null || !catId.equals(rec.getCategoria().getId())) {
                continue;
            }
            if (estaLibre(rec, fecha, inicio, fin)) {
                return rec;
            }
        }
        return null;
    }

    private boolean estaLibre(Recurso rec, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getEstado() != EstadoReserva.ACTIVA) {
                continue;
            }
            if (!tieneRecurso(r, rec.getId())) {
                continue;
            }
            if (r.solapaCon(fecha, inicio, fin)) {
                return false;
            }
        }
        return true;
    }

    private boolean tieneRecurso(Reserva r, String recId) {
        if (r.getRecursosAsignados() == null) {
            return false;
        }
        for (Recurso rec : r.getRecursosAsignados()) {
            if (recId.equals(rec.getId())) {
                return true;
            }
        }
        return false;
    }

    private String textoReservaEnSlot(Recurso rec, LocalDate fecha, LocalTime slot, LocalTime slotFin) {
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getEstado() != EstadoReserva.ACTIVA) {
                continue;
            }
            if (!tieneRecurso(r, rec.getId())) {
                continue;
            }
            if (r.solapaCon(fecha, slot, slotFin)) {
                String nombre = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "";
                return r.getActividad() + " - " + nombre;
            }
        }
        return "";
    }

    private String textoActividadesEnSlot(LocalDate fecha, LocalTime slot, LocalTime slotFin) {
        StringBuilder sb = new StringBuilder();
        for (Reserva r : dataContainer.getReservas()) {
            if (r.getEstado() != EstadoReserva.ACTIVA) {
                continue;
            }
            if (r.solapaCon(fecha, slot, slotFin)) {
                if (sb.length() > 0) {
                    sb.append(" / ");
                }
                String nombre = r.getFuncionario() != null ? r.getFuncionario().getNombre() : "";
                sb.append(r.getActividad()).append(" - ").append(nombre);
            }
        }
        return sb.toString();
    }

    private boolean reservaEnRango(Reserva r, LocalDate desde, LocalDate hasta) {
        if (r.getEstado() != EstadoReserva.ACTIVA || r.getFecha() == null) {
            return false;
        }
        if (desde != null && r.getFecha().isBefore(desde)) {
            return false;
        }
        if (hasta != null && r.getFecha().isAfter(hasta)) {
            return false;
        }
        return true;
    }

    private String siguienteIdCategoria() {
        int max = 0;
        for (Categoria c : dataContainer.getCategorias()) {
            try {
                max = Math.max(max, Integer.parseInt(c.getId()));
            } catch (NumberFormatException ignored) {
            }
        }
        return String.valueOf(max + 1);
    }

    private String normalizarHora(String hora) {
        String[] p = hora.split(":");
        return String.format("%02d:%02d", Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }

    private String formatearCategorias(List<String> categorias) {
        return String.join(", ", categorias);
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private void guardar() {
        xmlStore.guardarDatos(dataContainer);
    }
}
