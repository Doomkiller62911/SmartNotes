
package com.smartnotes.service;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import com.smartnotes.repository.BitacoraNotaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BitacoraService {

    private final BitacoraNotaRepository bitacoraNotaRepository;
    
    public BitacoraService(BitacoraNotaRepository bitacoraNotaRepository) {
        this.bitacoraNotaRepository = bitacoraNotaRepository;
    }
    
    @Transactional(readOnly = true)
    public List<NotaCorreccion> buscar(String nombreTeamMember, Integer idCategoria, LocalDate fecha){
        
        String nombre = ";";
        if (nombreTeamMember != null) {
            nombre = nombreTeamMember.trim();
        }
        
        return bitacoraNotaRepository.consultaBitacora(EstadoNota.FINALIZADA, nombre, idCategoria, fecha);
    
    }
    
    @Transactional(readOnly = true)
    public Optional<NotaCorreccion> buscarPorId(Integer idNota) {
        if (idNota == null) {
            return Optional.empty();
            
        }
        
        Optional<NotaCorreccion> nota = bitacoraNotaRepository.findById(idNota);
        if (nota.isPresent() && nota.get().getEstado() != EstadoNota.FINALIZADA) {
            return Optional.empty();
            
        }
        return nota;
        
    }
}
    
  