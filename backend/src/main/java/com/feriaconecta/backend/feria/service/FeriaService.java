package com.feriaconecta.backend.feria.service;

import com.feriaconecta.backend.auth.model.Usuario;
import com.feriaconecta.backend.auth.service.UsuarioService;
import com.feriaconecta.backend.common.exception.BusinessRuleException;
import com.feriaconecta.backend.common.exception.ConflictException;
import com.feriaconecta.backend.common.exception.ResourceNotFoundException;
import com.feriaconecta.backend.common.exception.UnauthorizedException;
import com.feriaconecta.backend.config.security.CurrentUser;
import com.feriaconecta.backend.feria.dto.CambiarEstadoRequest;
import com.feriaconecta.backend.feria.dto.FeriaRequest;
import com.feriaconecta.backend.feria.dto.FeriaResponse;
import com.feriaconecta.backend.feria.dto.UbicacionRequest;
import com.feriaconecta.backend.feria.model.EstadoFeria;
import com.feriaconecta.backend.feria.model.Feria;
import com.feriaconecta.backend.feria.model.Ubicacion;
import com.feriaconecta.backend.feria.repository.FeriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio encargado de la gestión de ferias.
 * <p>
 * Centraliza las reglas de negocio, la persistencia
 * y la integración con los servicios existentes.
 */
@Service
@RequiredArgsConstructor
public class FeriaService {

    private final FeriaRepository feriaRepository;
    private final UsuarioService usuarioService;
    private final CurrentUser currentUser;

    /**
     * Crea una feria en estado BORRADOR con su ubicación.
     * <p>
     * Obtiene el organizador autenticado, verifica duplicados
     * y guarda la dirección seleccionada o ingresada manualmente.
     *
     * @param request datos de la feria y su ubicación
     * @return feria creada y aviso opcional sobre su ubicación
     * @throws ConflictException     si ya existe una feria con igual nombre y fecha
     * @throws UnauthorizedException si el usuario autenticado ya no existe
     * @throws BusinessRuleException si las coordenadas están incompletas
     */

    @Transactional
    public FeriaResponse crear(FeriaRequest request) {

        //obtiene id organizador autenticado
        Integer organizadorId = currentUser.getId();

        //se recupera el usuario
        Usuario organizador = usuarioService.buscarPorId(organizadorId)
                .orElseThrow(() -> new UnauthorizedException("Usuario no autenticado."));

        String nombre = request.nombre().trim();

        //se verifica que no exista una feria duplicada
        if (feriaRepository.existsByOrganizador_IdAndNombreAndFecha(organizadorId, nombre, request.fecha())) {
            throw new ConflictException("Ya existe una feria con ese nombre y fecha.");
        }

        UbicacionRequest direccion = request.ubicacion();

        // Verifica que ambas coordenadas estén presentes o ambas sean nulas.
        validarCoordenadas(direccion);

        //se construye la feria en borrador
        Feria feria = Feria.builder()
                .organizador(organizador)
                .nombre(nombre)
                .fecha(request.fecha())
                .categoriaPermitida(request.categoriaPermitida())
                .costoParticipacion(request.costoParticipacion())
                .build();

        //se construye la ubicacion
        Ubicacion ubicacion = Ubicacion.builder()
                .provincia(direccion.provincia())
                .municipio(direccion.municipio())
                .localidad(direccion.localidad())
                .calle(direccion.calle())
                .altura(direccion.altura())
                .latitud(direccion.latitud())
                .longitud(direccion.longitud())
                .build();

        //se asocia ubicacion a feria
        feria.asignarUbicacion(ubicacion);
        //se persiste
        Feria feriaSave = feriaRepository.saveAndFlush(feria);

        // Informa cuando la dirección se guardó sin coordenadas.
        String aviso = direccion.latitud() == null
                ? "La dirección se guardó sin coordenadas geográficas."
                : null;

        return mapToFeriaResponse(feriaSave, aviso);
    }


    /**
     * Obtiene las ferias pertenecientes al organizador autenticado.
     * <p>
     * Las ferias se devuelven ordenadas por fecha descendente,
     * sin incluir información de otros organizadores.
     *
     * @return listado de ferias del organizador
     */
    @Transactional(readOnly = true)
    public List<FeriaResponse> listarMisFerias() {
        Integer organizadorId = currentUser.getId();

        return feriaRepository.findByOrganizador_IdOrderByFechaDesc(organizadorId)
                .stream()
                .map(feria -> mapToFeriaResponse(feria, null))
                .toList();
    }


    /**
     * Obtiene el detalle de una feria del organizador autenticado.
     * <p>
     * Verifica que la feria exista y pertenezca al usuario actual.
     * Si no existe o pertenece a otro organizador, devuelve un error 404.
     *
     * @param feriaId identificador de la feria
     * @return datos de la feria solicitada
     * @throws ResourceNotFoundException si la feria no existe
     *                                   o no pertenece al organizador
     */
    @Transactional(readOnly = true)
    public FeriaResponse obtenerDetalle(Integer feriaId) {

        Integer organizadorId = currentUser.getId();

        // Busca la feria por su identificador.
        Feria feria = feriaRepository.findById(feriaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "la feria", feriaId
                ));

        // Verifica que la feria pertenezca al organizador autenticado.
        if (!feria.getOrganizador().getId().equals(organizadorId)) {
            throw new ResourceNotFoundException("la feria", feriaId);
        }

        return mapToFeriaResponse(feria, null);
    }

    /**
     * Actualiza los datos de una feria del organizador autenticado.
     * <p>
     * Permite editar ferias en BORRADOR o PUBLICADA cuya fecha
     * no haya pasado. Una vez publicada, no permite modificar
     * la categoría ni el costo de participación.
     *
     * @param feriaId identificador de la feria
     * @param request nuevos datos de la feria y su ubicación
     * @return feria actualizada
     * @throws ResourceNotFoundException si la feria no existe o es ajena
     * @throws BusinessRuleException     si la edición no está permitida
     * @throws ConflictException         si existe otra feria con igual nombre y fecha
     */
    @Transactional
    public FeriaResponse editar(Integer feriaId, FeriaRequest request) {

        Integer organizadorId = currentUser.getId();

        // Busca la feria y verifica que pertenezca al organizador.
        Feria feria = feriaRepository.findById(feriaId)
                .orElseThrow(() -> new ResourceNotFoundException("la feria", feriaId));

        if (!feria.getOrganizador().getId().equals(organizadorId)) {
            throw new ResourceNotFoundException("la feria", feriaId);
        }

        // Solo permite editar ferias en BORRADOR o PUBLICADA.
        if (feria.getEstado() != EstadoFeria.BORRADOR
                && feria.getEstado() != EstadoFeria.PUBLICADA) {
            throw new BusinessRuleException(
                    "No se puede editar una feria en este estado."
            );
        }

        // No permite editar ferias cuya fecha ya pasó.
        if (feria.getFecha().isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "No se puede editar una feria cuya fecha ya pasó."
            );
        }

        // Una feria publicada no puede cambiar categoría ni costo.
        if (feria.getEstado() == EstadoFeria.PUBLICADA
                && (feria.getCategoriaPermitida() != request.categoriaPermitida()
                || feria.getCostoParticipacion()
                .compareTo(request.costoParticipacion()) != 0)) {
            throw new BusinessRuleException(
                    "No se puede modificar la categoría ni el costo de una feria publicada."
            );
        }

        String nombre = request.nombre().trim();

        // Verifica duplicados excluyendo la feria actual.
        if (feriaRepository.existsByOrganizador_IdAndNombreAndFechaAndIdNot(
                organizadorId, nombre, request.fecha(), feriaId)) {
            throw new ConflictException(
                    "Ya existe otra feria con ese nombre y fecha."
            );
        }

        UbicacionRequest direccion = request.ubicacion();

        validarCoordenadas(direccion);

        // Actualiza los datos de la feria.
        feria.actualizarDatos(
                nombre,
                request.fecha(),
                request.categoriaPermitida(),
                request.costoParticipacion()
        );

        // Actualiza la ubicación sin modificar su relación con Feria.
        feria.getUbicacion().actualizarDatos(
                direccion.provincia(),
                direccion.municipio(),
                direccion.localidad(),
                direccion.calle(),
                direccion.altura(),
                direccion.latitud(),
                direccion.longitud()
        );

        String aviso = direccion.latitud() == null
                ? "La dirección se guardó sin coordenadas geográficas."
                : null;

        return mapToFeriaResponse(feria, aviso);
    }


    /**
     * Cambia el estado de una feria del organizador autenticado.
     * <p>
     * Verifica que la feria pertenezca al usuario actual
     * y que la transición solicitada esté permitida.
     *
     * @param feriaId identificador de la feria
     * @param request estado solicitado
     * @return feria con su estado actualizado
     * @throws ResourceNotFoundException si la feria no existe o es ajena
     * @throws BusinessRuleException     si la transición no está permitida
     */
    @Transactional
    public FeriaResponse cambiarEstado(Integer feriaId, CambiarEstadoRequest request) {

        Integer organizadorId = currentUser.getId();

        // Busca la feria y comprueba que pertenezca al organizador.
        Feria feria = feriaRepository.findById(feriaId)
                .orElseThrow(() -> new ResourceNotFoundException("la feria", feriaId));

        if (!feria.getOrganizador().getId().equals(organizadorId)) {
            throw new ResourceNotFoundException("la feria", feriaId);
        }

        EstadoFeria estadoActual = feria.getEstado();
        EstadoFeria nuevoEstado = request.estado();

        // Comprueba que la transición solicitada esté permitida.
        boolean transicionValida = switch (estadoActual) {
            case BORRADOR -> nuevoEstado == EstadoFeria.PUBLICADA
                    || nuevoEstado == EstadoFeria.CANCELADA;

            case PUBLICADA -> nuevoEstado == EstadoFeria.EN_CURSO
                    || nuevoEstado == EstadoFeria.CANCELADA;

            case EN_CURSO -> nuevoEstado == EstadoFeria.FINALIZADA
                    || nuevoEstado == EstadoFeria.CANCELADA;

            case FINALIZADA, CANCELADA -> false;
        };

        if (!transicionValida) {
            throw new BusinessRuleException(
                    "No se permite cambiar de " + estadoActual + " a " + nuevoEstado + "."
            );
        }

        // La publicación requiere validar que exista al menos un puesto.
        if (nuevoEstado == EstadoFeria.PUBLICADA) {
            throw new BusinessRuleException(
                    "No se puede publicar la feria hasta validar que tenga al menos un puesto."
            );
        }

        // Actualiza el estado de la feria.
        feria.cambiarEstado(nuevoEstado);

        return mapToFeriaResponse(feria, null);
    }


    /**
     * Construye la respuesta con los datos de una feria y su ubicación.
     *
     * @param feria entidad de feria
     * @param aviso aviso opcional sobre la ubicación
     * @return datos de la feria para el cliente
     */
    private FeriaResponse mapToFeriaResponse(Feria feria, String aviso) {
        Ubicacion ubicacion = feria.getUbicacion();

        return new FeriaResponse(
                feria.getId(),
                feria.getNombre(),
                feria.getFecha(),
                feria.getCategoriaPermitida(),
                feria.getCostoParticipacion(),
                feria.getEstado(),
                new FeriaResponse.UbicacionDetalle(
                        ubicacion.getProvincia(),
                        ubicacion.getMunicipio(),
                        ubicacion.getLocalidad(),
                        ubicacion.getCalle(),
                        ubicacion.getAltura(),
                        ubicacion.getLatitud(),
                        ubicacion.getLongitud()
                ),
                aviso
        );
    }


    /**
     * Verifica que las coordenadas se reciban juntas.
     * <p>
     * Se permite informar ambas o ninguna.
     *
     * @param direccion ubicación recibida
     * @throws BusinessRuleException si falta una de las coordenadas
     */
    private void validarCoordenadas(UbicacionRequest direccion) {
        boolean tieneLatitud = direccion.latitud() != null;
        boolean tieneLongitud = direccion.longitud() != null;

        if (tieneLatitud != tieneLongitud) {
            throw new BusinessRuleException(
                    "La latitud y la longitud deben informarse juntas"
            );
        }
    }
}
