package servicios;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import modelos.Temperatura;

public class TemperaturaServicio {

    // Carga los datos desde el archivo CSV usando paradigma funcional
    public static List<Temperatura> getDatos(String rutaArchivo) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            Stream<String> lineas = Files.lines(Paths.get(rutaArchivo));
            return lineas.skip(1)
                    .map(linea -> linea.split(","))
                    .map(textos -> new Temperatura(
                            textos[0].trim(),
                            LocalDate.parse(textos[1].trim(), formato),
                            Double.parseDouble(textos[2].trim())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Obtiene la lista de ciudades únicas
    public static List<String> getCiudades(List<Temperatura> datos) {
        return datos.stream()
                .map(t -> t.getCiudad())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Filtra los datos por rango de fechas
    public static List<Temperatura> filtrarPorRango(List<Temperatura> datos,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        return datos.stream()
                .filter(t -> !t.getFecha().isBefore(fechaDesde)
                        && !t.getFecha().isAfter(fechaHasta))
                .collect(Collectors.toList());
    }

    // Calcula el promedio de temperatura por ciudad para un rango de fechas
    // Retorna un mapa Ciudad -> Promedio
    public static Map<String, Double> getPromedioPorCiudad(List<Temperatura> datos,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        return filtrarPorRango(datos, fechaDesde, fechaHasta)
                .stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCiudad(),
                        Collectors.averagingDouble(t -> t.getTemperatura())))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    // Obtiene la ciudad más calurosa para una fecha específica
    public static Optional<Temperatura> getCiudadMasCalurosa(List<Temperatura> datos, LocalDate fecha) {
        return datos.stream()
                .filter(t -> t.getFecha().equals(fecha))
                .max(Comparator.comparingDouble(t -> t.getTemperatura()));
    }

    // Obtiene la ciudad menos calurosa para una fecha específica
    public static Optional<Temperatura> getCiudadMenosCalurosa(List<Temperatura> datos, LocalDate fecha) {
        return datos.stream()
                .filter(t -> t.getFecha().equals(fecha))
                .min(Comparator.comparingDouble(t -> t.getTemperatura()));
    }
}
