package cr.ac.una.reservas.data;

import cr.ac.una.reservas.logic.Categoria;
import cr.ac.una.reservas.logic.EstadoReserva;
import cr.ac.una.reservas.logic.Reserva;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class XmlDataStoreIT {

    @TempDir
    Path tempDir;

    @Test
    void guardarYCargarPersistenciaXml() {
        Path xml = tempDir.resolve("datos.xml");
        XmlDataStore store = new XmlDataStore(xml.toString());
        DataContainer datos = store.cargarDatos();
        int original = datos.getCategorias().size();
        datos.getCategorias().add(new Categoria("99", "Categoria IT"));
        store.guardarDatos(datos);

        DataContainer recargado = store.cargarDatos();
        assertEquals(original + 1, recargado.getCategorias().size());
        assertFalse(recargado.getUsuarios().isEmpty());
    }


        @Test
        void reservaQuedaEnlazadaAlRecursoYCategoriaDelContenedor() {
            Path xml = tempDir.resolve("datos.xml");
            XmlDataStore store = new XmlDataStore(xml.toString());
            DataContainer datos = store.cargarDatos();

            Reserva reserva = new Reserva();
            reserva.setId("R-1");
            reserva.setActividad("Charla");
            reserva.setFecha(LocalDate.of(2026, 9, 20));
            reserva.setHoraInicio(LocalTime.of(8, 0));
            reserva.setHoraFin(LocalTime.of(10, 0));
            reserva.setEstado(EstadoReserva.ACTIVA);
            reserva.setFuncionario(datos.getFuncionarios().get(0));
            reserva.getRecursosAsignados().add(datos.getRecursos().get(0));
            reserva.getCategoriasSolicitadas().add(datos.getCategorias().get(0));
            datos.getReservas().add(reserva);
            store.guardarDatos(datos);

            DataContainer recargado = store.cargarDatos();
            Reserva cargada = recargado.getReservas().get(0);
            recargado.getRecursos().get(0).setDescripcion("Descripcion nueva");
            recargado.getCategorias().get(0).setDescripcion("Categoria nueva");

            assertEquals("Descripcion nueva", cargada.getRecursosAsignados().get(0).getDescripcion());
            assertEquals("Categoria nueva", cargada.getCategoriasSolicitadas().get(0).getDescripcion());
        }
    }

